package org.zvbj.tslneko;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.yic.xconomy.api.XConomyAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public final class TSLnekoManager {

    private final Plugin plugin;
    private boolean foodRestriction;
    private double threshold;
    private String suffix;
    private long period;

    private final Set<Material> allowedFoods;
    private List<String> applyCommands;
    private List<String> removeCommands;

    private final AtomicReference<ScheduledTask> loop = new AtomicReference<>();
    private final Map<UUID, Boolean> catgirls = new ConcurrentHashMap<>();
    private final Set<UUID> forced = Collections.synchronizedSet(new HashSet<>());

    private final XConomyAPI xConomy = new XConomyAPI();

    private static final String MSG_MONEY   = "§d[TSL喵] §r你包里的TSLC太少，被变成了猫娘喵~";
    private static final String MSG_FORCED  = "§d[TSL喵] §r竟然主动变成了猫娘，真奇怪喵~";
    private static final String MSG_NORMAL  = "§d[TSL喵] §r你现在不再是猫娘了~";

    public TSLnekoManager(@NotNull Plugin plugin,
                           double threshold,
                           String suffix,
                           long periodTicks,
                           Set<Material> allowedFoods,
                           List<String> applyCommands,
                           boolean foodRestriction,
                           List<String> removeCommands) {
        this.plugin = plugin;
        this.threshold = threshold;
        this.suffix = suffix;
        this.period = periodTicks;
        this.allowedFoods = ConcurrentHashMap.newKeySet();
        this.allowedFoods.addAll(allowedFoods);
        this.applyCommands = List.copyOf(applyCommands);
        this.removeCommands = List.copyOf(removeCommands);
        this.foodRestriction = foodRestriction;
    }

    public void startBalanceLoop() {
        ScheduledTask task = plugin.getServer().getGlobalRegionScheduler()
                .runAtFixedRate(plugin,
                        t -> Bukkit.getOnlinePlayers().forEach(this::evaluate),
                        1L, Math.max(1L, period));
        loop.set(task);
    }

    public void shutdown() {
        Optional.ofNullable(loop.getAndSet(null)).ifPresent(ScheduledTask::cancel);
        catgirls.clear();
    }

    public void reload() {
        loadConfigValues();
        shutdown();
        startBalanceLoop();
    }

    public String getSuffix() { return suffix; }

    public boolean canEat(Material m) {
        if (m == Material.POTION || m == Material.MILK_BUCKET) {
            return true;
        }
        if (allowedFoods.isEmpty()) {
            return m == Material.COD || m == Material.SALMON;
        }
        return allowedFoods.contains(m);
    }

    public boolean toggleForced(Player p) {
        UUID id = p.getUniqueId();
        if (forced.remove(id)) {
            removeCatgirl(p);
            p.sendMessage(MSG_NORMAL);
            return false;
        }
        forced.add(id);
        applyCatgirl(p);
        p.sendMessage(MSG_FORCED);
        return true;
    }

    public boolean isCatgirl(Player p) {
        return forced.contains(p.getUniqueId()) || getBalance(p) <= threshold;
    }

    public void evaluate(Player p) {
        UUID id = p.getUniqueId();
        boolean shouldBe = isCatgirl(p);
        boolean isNow = catgirls.containsKey(id);

        if (shouldBe && !isNow) {
            applyCatgirl(p);
            if (!forced.contains(id)) {
                p.sendMessage(MSG_MONEY);
            }
        } else if (!shouldBe && isNow) {
            removeCatgirl(p);
            p.sendMessage(MSG_NORMAL);
        }
    }

    private double getBalance(Player p) {
        try {
            var data = xConomy.getPlayerData(p.getUniqueId());
            return data == null ? 0.0 : data.getBalance().doubleValue();
        } catch (Throwable t) {
            plugin.getLogger().warning("XConomyAPI error – treat balance as 0 for " + p.getName());
            return 0.0;
        }
    }

    private void applyCatgirl(Player p) {
        catgirls.put(p.getUniqueId(), true);
        dispatchCommands(applyCommands, p);
    }

    private void removeCatgirl(Player p) {
        catgirls.remove(p.getUniqueId());
        dispatchCommands(removeCommands, p);
    }

    private void dispatchCommands(List<String> cmds, Player p) {
        if (cmds.isEmpty()) return;

        plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
            for (String raw : cmds) {
                String cmd = raw.replace("%player%", p.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            }
        });
    }

    public void loadConfigValues() {
        FileConfiguration cfg = plugin.getConfig();

        threshold = cfg.getDouble("threshold", 5.0);
        suffix = ChatColor.translateAlternateColorCodes('&',
                cfg.getString("chat_suffix", "喵~"));
        period = Math.max(1L, cfg.getLong("checkPeriodTicks", 40L));

        allowedFoods.clear();
        for (String s : cfg.getStringList("allowed_foods")) {
            Material m = Material.matchMaterial(s.toUpperCase());
            if (m != null && m.isEdible()) allowedFoods.add(m);
        }
        if (allowedFoods.isEmpty()) {
            allowedFoods.add(Material.COD);
            allowedFoods.add(Material.SALMON);
        }

        applyCommands = List.copyOf(cfg.getStringList("apply_commands"));
        removeCommands = List.copyOf(cfg.getStringList("remove_commands"));
        foodRestriction = cfg.getBoolean("food_restriction", true);
    }

    public boolean hasFoodRestriction() {
        return foodRestriction;
    }
}
