package me.kc1508.fendorisVerify.commands;

import me.kc1508.fendorisVerify.service.ApplicationService;
import me.kc1508.fendorisVerify.service.MessageService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class ApplySeeOfflineCommand implements CommandExecutor {
    private final ApplicationService applications;
    private final MessageService messages;

    public ApplySeeOfflineCommand(ApplicationService applications, MessageService messages) {
        this.applications = applications;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player p)) {
            messages.send(sender, "not_player");
            return true;
        }
        if (!p.isOp()) {
            messages.send(p, "no_permission");
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("stop")) {
            applications.stopReviewSession(p);
            return true;
        }
        applications.startReviewSession(p);
        return true;
    }
}
