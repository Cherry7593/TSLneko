package org.zvbj.tslneko;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public final class NekoCommand implements CommandExecutor, TabCompleter {

    private final TSLneko plugin;
    private final TSLnekoManager manager;

    public NekoCommand(TSLneko plugin, TSLnekoManager manager) {
        this.plugin  = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command cmd,
                             @NotNull String label,
                             @NotNull String[] args) {

        if (!sender.hasPermission("tslneko.admin")) {
            sender.sendMessage("§c[错误] 你没有权限！");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage("§e用法: /" + label + " reload | make <玩家>");
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "reload" -> {
                plugin.reloadConfig();
                manager.reload();
                sender.sendMessage("§aTSLneko 配置已重载！");
            }
            case "make" -> {
                if (args.length < 2) {
                    sender.sendMessage("§e用法: /" + label + " make <玩家>");
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    sender.sendMessage("§c找不到在线玩家 " + args[1]);
                    return true;
                }
                boolean forced = manager.toggleForced(target);
                sender.sendMessage("§b" + target.getName() +
                        (forced ? " 已被强制猫娘！" : " 已恢复正常！"));
            }
            default -> sender.sendMessage("§e未知子命令！");
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command cmd,
                                                @NotNull String alias,
                                                @NotNull String[] args) {
        if (!sender.hasPermission("tslneko.admin")) return List.of();

        return switch (args.length) {
            case 1 -> List.of("reload", "make").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
            case 2 -> args[0].equalsIgnoreCase("make")
                    ? Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                    .toList()
                    : List.of();
            default -> List.of();
        };
    }
}
