package me.kc1508.fendorisVerify.listener;

import me.kc1508.fendorisVerify.service.ApplicationService;
import me.kc1508.fendorisVerify.service.MessageService;
import me.kc1508.fendorisVerify.service.VerifyService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public final class CommandBlockListener implements Listener {
    private final VerifyService verify;
    private final MessageService messages;
    private final ApplicationService applications;

    public CommandBlockListener(VerifyService verify, MessageService messages, ApplicationService applications) {
        this.verify = verify;
        this.messages = messages;
        this.applications = applications;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        if (applications.isApplying(e.getPlayer())) {
            e.setCancelled(true);
            messages.send(e.getPlayer(), "apply_in_progress");
            return;
        }
        if (verify.isVerified(e.getPlayer())) return;
        String msg = e.getMessage().trim().toLowerCase();
        if (msg.equals("/verify") || msg.equals("/apply")) return;
        e.setCancelled(true);
        messages.send(e.getPlayer(), "commands_blocked");
    }
}
