package org.zvbj.tslneko.listeners;

import org.zvbj.tslneko.TSLnekoManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {
    private final TSLnekoManager manager;

    public ChatListener(TSLnekoManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (!manager.isCatgirl(p)) return;
        String suf = manager.getSuffix();
        String msg = e.getMessage();
        if (!msg.endsWith(suf)) {
            e.setMessage(msg + suf);
        }
    }
}
