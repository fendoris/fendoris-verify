package me.kc1508.fendorisVerify.commands;

import me.kc1508.fendorisVerify.service.ApplicationService;
import me.kc1508.fendorisVerify.service.MessageService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class ApplyDenyCommand implements CommandExecutor {
    private final ApplicationService applications;
    private final MessageService messages;

    public ApplyDenyCommand(ApplicationService applications, MessageService messages) {
        this.applications = applications;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!sender.isOp()) {
            messages.send(sender, "no_permission");
            return true;
        }
        if (args.length != 1) {
            messages.send(sender, "apply_deny_usage");
            return true;
        }
        String target = args[0];
        applications.deny(target);
        messages.send(sender, "apply_denied_operator");
        if (sender instanceof org.bukkit.entity.Player p && applications.hasReviewSession(p)) {
            applications.sendNextInReview(p);
        }
        return true;
    }
}
