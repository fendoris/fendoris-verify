package me.kc1508.fendorisVerify.listener;

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

    public JoinListener(org.bukkit.plugin.java.JavaPlugin plugin, VerifyService verify, MessageService messages) {
        this.plugin = plugin;
        this.verify = verify;
        this.messages = messages;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {
        if (!verify.isVerified(e.getPlayer())) {
            // Delay ~1 tick (~50ms). Bukkit does not support 10ms granularity.
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> messages.send(e.getPlayer(), "unverified_join"), 1L);
        }
        verify.enforceState(e.getPlayer());
    }
}