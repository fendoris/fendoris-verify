package me.kc1508.fendorisVerify.commands;

import me.kc1508.fendorisVerify.service.ApplicationService;
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
    private final ApplicationService applications;

    public VerifyCommand(VerifyService verify, MessageService messages, ApplicationService applications) {
        this.verify = verify;
        this.messages = messages;
        this.applications = applications;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player p)) {
            messages.send(sender, "not_player");
            return true;
        }

        if (verify.isVerified(p)) {
            messages.send(p, "verify_already");
            return true;
        }

        messages.send(p, "verify_info");
        messages.send(p, "verify_next_section");
        applications.markSeenRules(p);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String @NotNull [] args) {
        return Collections.emptyList(); // no suggestions, no player names
    }
}
