package me.kc1508.fendorisVerify.listener;

import me.kc1508.fendorisVerify.service.MessageService;
import me.kc1508.fendorisVerify.service.VerifyService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public final class CommandBlockListener implements Listener {
    private final VerifyService verify;
    private final MessageService messages;

    public CommandBlockListener(VerifyService verify, MessageService messages) {
        this.verify = verify;
        this.messages = messages;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        if (verify.isVerified(e.getPlayer())) return;
        String msg = e.getMessage().trim().toLowerCase();
        if (msg.equals("/verify") || msg.equals("/apply")) return;
        e.setCancelled(true);
        messages.send(e.getPlayer(), "commands_blocked");
    }
}
