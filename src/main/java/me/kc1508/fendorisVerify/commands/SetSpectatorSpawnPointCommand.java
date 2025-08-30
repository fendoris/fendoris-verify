package me.kc1508.fendorisVerify.commands;

import me.kc1508.fendorisVerify.service.ConfigService;
import me.kc1508.fendorisVerify.service.MessageService;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public final class SetSpectatorSpawnPointCommand implements CommandExecutor {
    private final ConfigService config;
    private final MessageService messages;

    public SetSpectatorSpawnPointCommand(ConfigService config, MessageService messages) {
        this.config = config;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player p)) {
            messages.send(sender, "not_player");
            return true;
        }
        if (!sender.hasPermission("fendoris.verify.admin")) {
            messages.send(sender, "no_permission");
            return true;
        }
        Location l = p.getLocation();
        config.setSpectatorSpawn(l);

        var ph = new HashMap<String, String>();
        ph.put("x", String.format("%.1f", l.getX()));
        ph.put("y", String.format("%.1f", l.getY()));
        ph.put("z", String.format("%.1f", l.getZ()));
        ph.put("world", l.getWorld().getName());
        messages.send(sender, "spawn_set", ph);
        return true;
    }
}