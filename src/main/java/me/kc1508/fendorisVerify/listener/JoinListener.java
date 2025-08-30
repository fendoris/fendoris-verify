package me.kc1508.fendorisVerify.listener;

import me.kc1508.fendorisVerify.service.MessageService;
import me.kc1508.fendorisVerify.service.VerifyService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class JoinListener implements Listener {
    private final VerifyService verify;
    private final MessageService messages;

    public JoinListener(VerifyService verify, MessageService messages) {
        this.verify = verify;
        this.messages = messages;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {
        if (!verify.isVerified(e.getPlayer())) {
            messages.send(e.getPlayer(), "unverified_join");
        }
        verify.enforceState(e.getPlayer());
    }
}