package me.kc1508.fendorisVerify.commands;

import me.kc1508.fendorisVerify.service.MessageService;
import me.kc1508.fendorisVerify.service.VerifyService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public final class UnverifyCommand implements CommandExecutor {
    private final VerifyService verify;
    private final MessageService messages;

    public UnverifyCommand(VerifyService verify, MessageService messages) {
        this.verify = verify;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!sender.hasPermission("fendoris.verify.operator")) {
            messages.send(sender, "no_permission");
            return true;
        }
        if (args.length != 1) {
            messages.send(sender, "unverify_usage");
            return true;
        }
        String targetName = args[0];

        Player target = Bukkit.getPlayerExact(targetName);
        if (target != null) {
            Location before = target.getLocation().clone();
            verify.setVerified(target, false);
            verify.enforceState(target);
            verify.teleportToSpectatorSpawn(target);

            var ph = new HashMap<String, String>();
            ph.put("target", target.getName());
            ph.put("x", String.format("%.1f", before.getX()));
            ph.put("y", String.format("%.1f", before.getY()));
            ph.put("z", String.format("%.1f", before.getZ()));
            ph.put("world", before.getWorld().getName());
            messages.send(sender, "unverify_done_operator", ph);
            messages.send(target, "unverify_notified_player");
        } else {
            if (!verify.hasStoredRecord(targetName)) {
                var phNF = new java.util.HashMap<String, String>();
                phNF.put("target", targetName);
                messages.send(sender, "unverify_not_found", phNF);
                return true;
            }
            verify.setVerified(targetName, false);
            var ph = new HashMap<String, String>();
            ph.put("target", targetName);
            ph.put("x", "n/a");
            ph.put("y", "n/a");
            ph.put("z", "n/a");
            ph.put("world", "n/a");
            messages.send(sender, "unverify_done_operator", ph);
        }
        return true;
    }
}