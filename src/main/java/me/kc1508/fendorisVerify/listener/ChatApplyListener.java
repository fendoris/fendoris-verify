package me.kc1508.fendorisVerify.listener;

import me.kc1508.fendorisVerify.service.ApplicationService;
import org.bukkit.entity.Player;
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
        Player sender = e.getPlayer();
        if (applications.isApplying(sender)) {
            e.setCancelled(true);
            sender.getServer().getScheduler().runTask(sender.getServer().getPluginManager().getPlugin("fendoris-verify"),
                    () -> applications.handleChat(sender, e.getMessage()));
            return;
        }
        e.getRecipients().removeIf(applications::isApplying);
    }
}
