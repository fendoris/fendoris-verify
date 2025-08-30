package me.kc1508.fendorisVerify.listener;

import me.kc1508.fendorisVerify.service.VerifyService;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;

public final class GameModeGuardListener implements Listener {
    private final VerifyService verify;

    public GameModeGuardListener(VerifyService verify) {
        this.verify = verify;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGameModeChange(PlayerGameModeChangeEvent e) {
        if (verify.isVerified(e.getPlayer())) return;
        if (e.getPlayer().isOp()) return; // operator bypass
        // Force Spectator if unverified
        if (e.getNewGameMode() != GameMode.SPECTATOR) {
            e.setCancelled(true);
        }
    }
}