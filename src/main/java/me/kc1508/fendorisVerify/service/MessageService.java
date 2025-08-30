package me.kc1508.fendorisVerify.service;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public final class MessageService {
    private final JavaPlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public MessageService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public String raw(String key) {
        return plugin.getConfig().getString("messages." + key, "");
    }

    public void send(CommandSender to, String key) {
        var raw = raw(key);
        if (raw.isEmpty()) return;
        Component c = mm.deserialize(raw);
        to.sendMessage(c);
    }

    public void send(CommandSender to, String key, Map<String, String> placeholders) {
        var raw = raw(key);
        if (raw.isEmpty()) return;
        for (var e : placeholders.entrySet()) {
            raw = raw.replace("%" + e.getKey() + "%", e.getValue());
        }
        Component c = mm.deserialize(raw);
        to.sendMessage(c);
    }

    public void sendPlayerOnly(Player p, String key) {
        send(p, key);
    }
}