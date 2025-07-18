package org.zvbj.tslneko.listeners;

import org.bukkit.entity.Player;
import org.zvbj.tslneko.TSLnekoManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class DeathListener implements Listener {
    private final TSLnekoManager manager;

    public DeathListener(TSLnekoManager manager) {
        this.manager = manager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        // Folia: schedule on global scheduler next tick to ensure economy balance is already deducted
        JavaPlugin.getProvidingPlugin(getClass()).getServer().getGlobalRegionScheduler()
                .execute(JavaPlugin.getProvidingPlugin(getClass()), () -> manager.evaluate(player));
    }
}
