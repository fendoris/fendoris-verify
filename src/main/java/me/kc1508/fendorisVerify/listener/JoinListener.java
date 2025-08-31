package me.kc1508.fendorisVerify.listener;

import me.kc1508.fendorisVerify.service.ApplicationService;
import me.kc1508.fendorisVerify.service.MessageService;
import me.kc1508.fendorisVerify.service.VerifyService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class JoinListener implements Listener {
    private final org.bukkit.plugin.java.JavaPlugin plugin;
    private final VerifyService verify;
    private final MessageService messages;
    private final ApplicationService applications;

    public JoinListener(org.bukkit.plugin.java.JavaPlugin plugin, VerifyService verify, MessageService messages, ApplicationService applications) {
        this.plugin = plugin;
        this.verify = verify;
        this.messages = messages;
        this.applications = applications;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {
        // Operator bypass: auto-verify on join
        if (e.getPlayer().isOp() && !verify.isVerified(e.getPlayer())) {
            verify.setVerified(e.getPlayer(), true);
            verify.enforceState(e.getPlayer());
            return;
        }

        // Non-ops: ensure spectator if unverified and show message after a tick
        if (!verify.isVerified(e.getPlayer())) {
            plugin.getServer().getScheduler().runTask(plugin, () -> verify.enforceState(e.getPlayer()));
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> messages.send(e.getPlayer(), "unverified_join"), 1L);
        } else {
            // Verified: keep survival unless operator doing staff work
            verify.enforceState(e.getPlayer());
        }

        // Prompt ops for offline apps
        applications.maybePromptOpsOnJoin(e.getPlayer());
    }
}