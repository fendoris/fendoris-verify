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

        if (!verify.isVerified(e.getPlayer())) {
            // Always reset position for unverified/denied when they rejoin
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                verify.enforceState(e.getPlayer());
                verify.teleportToSpectatorSpawn(e.getPlayer());
            });
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> messages.send(e.getPlayer(), "unverified_join"), 1L);
        } else {
            // Verified: enforce survival and handle deferred teleport + notices
            verify.enforceState(e.getPlayer());
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (applications.consumeTeleportOnJoin(e.getPlayer().getUniqueId())) {
                    verify.teleportToSpectatorSpawn(e.getPlayer());
                }
                String notice = applications.consumeNotifyOnJoin(e.getPlayer().getUniqueId());
                if ("accepted".equalsIgnoreCase(notice)) {
                    messages.send(e.getPlayer(), "apply_offline_accepted_notice");
                } else if ("denied".equalsIgnoreCase(notice)) {
                    messages.send(e.getPlayer(), "apply_offline_denied_notice");
                }
            });
        }

        // Prompt ops for any pending applications
        applications.maybePromptOpsOnJoin(e.getPlayer());
    }
}
