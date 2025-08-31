package me.kc1508.fendorisVerify.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.kc1508.fendorisVerify.service.ApplicationService;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public final class ChatApplyListener implements Listener {
    private final ApplicationService applications;
    private final Plugin plugin;

    public ChatApplyListener(ApplicationService applications, Plugin plugin) {
        this.applications = applications;
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncChatEvent e) {
        Player sender = e.getPlayer();

        // Applicants cannot send chat; treat as application input.
        if (applications.isApplying(sender)) {
            e.setCancelled(true);
            String msg = PlainTextComponentSerializer.plainText().serialize(e.message());
            plugin.getServer().getScheduler().runTask(plugin, () -> applications.handleChat(sender, msg));
            return;
        }

        // Applicants do not receive chat.
        e.viewers().removeIf(aud -> aud instanceof Player p && applications.isApplying(p));
    }
}
