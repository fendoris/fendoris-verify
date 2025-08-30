package me.kc1508.fendorisVerify.commands;

import me.kc1508.fendorisVerify.service.ConfigService;
import me.kc1508.fendorisVerify.service.MessageService;
import me.kc1508.fendorisVerify.service.VerifyService;
import me.kc1508.fendorisVerify.store.VerifyStorage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class ReloadCommand implements CommandExecutor {
    private final ConfigService config;
    private final VerifyStorage storage;
    private final VerifyService verify;
    private final MessageService messages;

    public ReloadCommand(ConfigService config, VerifyStorage storage, VerifyService verify, MessageService messages) {
        this.config = config;
        this.storage = storage;
        this.verify = verify;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!sender.hasPermission("fendoris.verify.admin")) {
            messages.send(sender, "no_permission");
            return true;
        }
        config.reload();
        storage.reload();
        for (Player p : Bukkit.getOnlinePlayers()) {
            verify.enforceState(p);
        }
        messages.send(sender, "reload_done");
        return true;
    }
}