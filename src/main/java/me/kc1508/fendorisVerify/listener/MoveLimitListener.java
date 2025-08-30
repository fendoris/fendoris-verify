package me.kc1508.fendorisVerify.listener;

import me.kc1508.fendorisVerify.service.ConfigService;
import me.kc1508.fendorisVerify.service.MessageService;
import me.kc1508.fendorisVerify.service.VerifyService;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public final class MoveLimitListener implements Listener {
    private final ConfigService config;
    private final VerifyService verify;
    private final MessageService messages;

    public MoveLimitListener(ConfigService config, VerifyService verify, MessageService messages) {
        this.config = config;
        this.verify = verify;
        this.messages = messages;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        e.getTo();
        if (verify.isVerified(e.getPlayer())) return;

        var spawn = config.spectatorSpawn();
        if (spawn == null) return;
        if (!e.getPlayer().getWorld().equals(spawn.getWorld())) return;

        int radius = config.spectatorRadius();
        if (radius <= 0) return;

        Location to = e.getTo();
        if (to.distanceSquared(spawn) > (double) radius * radius) {
            e.getPlayer().teleport(spawn);
            messages.send(e.getPlayer(), "spectator_boundary_hit");
        }
    }
}