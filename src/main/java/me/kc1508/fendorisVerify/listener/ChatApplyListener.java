package me.kc1508.fendorisVerify.listener;

import me.kc1508.fendorisVerify.service.ApplicationService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public final class ChatApplyListener implements Listener {
    private final ApplicationService applications;

    public ChatApplyListener(ApplicationService applications) {
        this.applications = applications;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        if (!applications.isApplying(e.getPlayer())) return;
        e.setCancelled(true);
        e.getPlayer().getServer().getScheduler().runTask(e.getPlayer().getServer().getPluginManager().getPlugin("fendoris-verify"),
                () -> applications.handleChat(e.getPlayer(), e.getMessage()));
    }
}
