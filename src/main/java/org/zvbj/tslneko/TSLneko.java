package org.zvbj.tslneko;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.zvbj.tslneko.listeners.ChatListener;
import org.zvbj.tslneko.listeners.FoodListener;
import org.zvbj.tslneko.listeners.DeathListener;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class TSLneko extends JavaPlugin {

    private static TSLneko INSTANCE;
    private TSLnekoManager manager;

    public static TSLneko get() { return INSTANCE; }
    public TSLnekoManager getManager() { return manager; }

    @Override
    public void onEnable() {
        INSTANCE = this;
        saveDefaultConfig();

        /* ---------- 从 config.yml 读取初始参数 ---------- */
        FileConfiguration cfg  = getConfig();
        double threshold       = cfg.getDouble("threshold", 5.0);
        String suffix          = cfg.getString("chat_suffix", "喵~");
        long   period          = cfg.getLong("checkPeriodTicks", 40L);

        Set<Material> foods = cfg.getStringList("allowed_foods").stream()
                .map(s -> Material.matchMaterial(s.toUpperCase()))
                .filter(m -> m != null && m.isEdible())
                .collect(Collectors.toSet());
        if (foods.isEmpty()) foods = Set.of(Material.COD, Material.SALMON);

        List<String> applyCmds  = cfg.getStringList("apply_commands");
        List<String> removeCmds = cfg.getStringList("remove_commands");

        /* ---------- 创建管理器 ---------- */
        manager = new TSLnekoManager(this,
                threshold,                                    // double threshold
                suffix,                                      // String suffix
                period,                                      // long periodTicks
                foods,                                       // Set<Material> allowedFoods
                applyCmds,                                   // List<String> applyCommands
                cfg.getBoolean("food_restriction", true),    // boolean foodRestriction
                removeCmds);                                 // List<String> removeCommands

        /* ---------- 指令 ---------- */
        NekoCommand nekoCmd = new NekoCommand(this, manager);
        Objects.requireNonNull(getCommand("tslneko")).setExecutor(nekoCmd);
        getCommand("tslneko").setTabCompleter(nekoCmd);

        /* ---------- 监听器 ---------- */
        var pm = Bukkit.getPluginManager();
        pm.registerEvents(new ChatListener(manager),  this);
        pm.registerEvents(new FoodListener(manager),  this);
        pm.registerEvents(new DeathListener(manager), this);

        manager.startBalanceLoop();
        getLogger().info("TSLneko " + getDescription().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        if (manager != null) manager.shutdown();
    }
}
