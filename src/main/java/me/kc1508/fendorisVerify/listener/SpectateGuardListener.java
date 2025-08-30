package me.kc1508.fendorisVerify.listener;

import com.destroystokyo.paper.event.player.PlayerStartSpectatingEntityEvent;
import me.kc1508.fendorisVerify.service.ConfigService;
import me.kc1508.fendorisVerify.service.MessageService;
import me.kc1508.fendorisVerify.service.VerifyService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SpectateGuardListener implements Listener {
    private final ConfigService config;
    private final VerifyService verify;
    private final MessageService messages;

    private static final Map<UUID, Long> LAST_SPECTATE_MSG = new ConcurrentHashMap<>();

    public SpectateGuardListener(ConfigService config, VerifyService verify, MessageService messages) {
        this.config = config;
        this.verify = verify;
        this.messages = messages;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpectateTeleport(PlayerTeleportEvent e) {
        if (!verify.isVerified(e.getPlayer()) && e.getCause() == PlayerTeleportEvent.TeleportCause.SPECTATE) {
            e.setCancelled(true);
            if (shouldSend(e.getPlayer().getUniqueId())) {
                messages.send(e.getPlayer(), "spectate_blocked");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onStartSpectating(PlayerStartSpectatingEntityEvent e) {
        if (!verify.isVerified(e.getPlayer())) {
            e.setCancelled(true);
            if (shouldSend(e.getPlayer().getUniqueId())) {
                messages.send(e.getPlayer(), "spectate_blocked");
            }
        }
    }

    private boolean shouldSend(UUID id) {
        int cd = Math.max(0, config.spectateBlockedCooldownSeconds());
        if (cd == 0) return true;
        long now = System.currentTimeMillis();
        long last = LAST_SPECTATE_MSG.getOrDefault(id, 0L);
        if ((now - last) >= cd * 1000L) {
            LAST_SPECTATE_MSG.put(id, now);
            return true;
        }
        return false;
    }
}
