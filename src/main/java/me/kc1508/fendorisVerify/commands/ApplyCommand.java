package me.kc1508.fendorisVerify.commands;

import me.kc1508.fendorisVerify.service.ApplicationService;
import me.kc1508.fendorisVerify.service.MessageService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public final class ApplyCommand implements CommandExecutor, TabCompleter {
    private final ApplicationService applications;
    private final MessageService messages;

    public ApplyCommand(ApplicationService applications, MessageService messages) {
        this.applications = applications;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player p)) {
            messages.send(sender, "not_player");
            return true;
        }
        applications.startApplication(p);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String @NotNull [] args) {
        return Collections.emptyList(); // no suggestions, no player names
    }
}
