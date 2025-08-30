package me.kc1508.fendorisVerify.commands;

import me.kc1508.fendorisVerify.service.MessageService;
import me.kc1508.fendorisVerify.service.VerifyService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public final class VerifyCommand implements CommandExecutor, TabCompleter {
    private final VerifyService verify;
    private final MessageService messages;

    public VerifyCommand(VerifyService verify, MessageService messages) {
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
            verify.teleportToSpectatorSpawn(p);
            return true;
        }
        messages.send(p, "verify_incorrect");
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String @NotNull [] args) {
        // Empty list disables any suggestions, including player names.
        return Collections.emptyList();
    }
}
