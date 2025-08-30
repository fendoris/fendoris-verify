package me.kc1508.fendorisVerify.commands;

import me.kc1508.fendorisVerify.service.MessageService;
import me.kc1508.fendorisVerify.service.VerifyService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class VerifyCommand implements CommandExecutor, TabCompleter {
    private final org.bukkit.plugin.java.JavaPlugin plugin;
    private final VerifyService verify;
    private final MessageService messages;

    public VerifyCommand(org.bukkit.plugin.java.JavaPlugin plugin, VerifyService verify, MessageService messages) {
        this.plugin = plugin;
        this.verify = verify;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player p)) {
            messages.send(sender, "not_player");
            return true;
        }

        if (args.length == 0) {
            messages.send(p, "verify_info");
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("me")) {
            verify.setVerified(p, true);
            messages.send(p, "verify_success");
            verify.enforceState(p);
            return true;
        }
        messages.send(p, "verify_incorrect");
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) list.add("me");
        return list;
    }
}