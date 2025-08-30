package me.kc1508.fendorisVerify.listener;

import com.destroystokyo.paper.event.player.PlayerStartSpectatingEntityEvent;
import me.kc1508.fendorisVerify.service.MessageService;
import me.kc1508.fendorisVerify.service.VerifyService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public final class SpectateGuardListener implements Listener {
    private final VerifyService verify;
    private final MessageService messages;

    public SpectateGuardListener(VerifyService verify, MessageService messages) {
        this.verify = verify;
        this.messages = messages;
    }

    // Block spectator menu teleports
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpectateTeleport(PlayerTeleportEvent e) {
        if (!verify.isVerified(e.getPlayer()) && e.getCause() == PlayerTeleportEvent.TeleportCause.SPECTATE) {
            e.setCancelled(true);
            messages.send(e.getPlayer(), "spectate_blocked");
        }
    }

    // Block starting POV spectating of any entity
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onStartSpectating(PlayerStartSpectatingEntityEvent e) {
        if (!verify.isVerified(e.getPlayer())) {
            e.setCancelled(true);
            messages.send(e.getPlayer(), "spectate_blocked");
        }
    }
}
