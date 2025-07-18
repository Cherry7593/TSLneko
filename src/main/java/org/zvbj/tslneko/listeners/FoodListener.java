package org.zvbj.tslneko.listeners;

import org.zvbj.tslneko.TSLnekoManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public class FoodListener implements Listener {
    private final TSLnekoManager manager;

    public FoodListener(TSLnekoManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEat(PlayerItemConsumeEvent e) {
        var p = e.getPlayer();
        if (!manager.isCatgirl(p) || !manager.hasFoodRestriction()) return;
        var mat = e.getItem().getType();
        if (!manager.canEat(mat)) {
            e.setCancelled(true);
            p.sendMessage("你现在是猫娘，只能吃鱼喵~");
        }
    }
}
