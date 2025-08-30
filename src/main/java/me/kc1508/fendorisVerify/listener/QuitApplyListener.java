package me.kc1508.fendorisVerify.listener;

import me.kc1508.fendorisVerify.service.ApplicationService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public final class QuitApplyListener implements Listener {
    private final ApplicationService applications;

    public QuitApplyListener(ApplicationService applications) {
        this.applications = applications;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        applications.abortIfApplying(e.getPlayer());
    }
}
