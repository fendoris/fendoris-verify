package me.kc1508.fendorisVerify.commands;

import me.kc1508.fendorisVerify.service.MessageService;
import me.kc1508.fendorisVerify.store.ApplicationsStorage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class ApplyResetCommand implements CommandExecutor, TabCompleter {
    private final ApplicationsStorage storage;
    private final MessageService messages;

    public ApplyResetCommand(ApplicationsStorage storage, MessageService messages) {
        this.storage = storage;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!sender.isOp()) {
            messages.send(sender, "no_permission");
            return true;
        }
        if (args.length != 1) {
            messages.send(sender, "apply_reset_usage");
            return true;
        }
        String name = args[0];
        UUID id = storage.findDeniedUuidByName(name);
        if (id == null) {
            OfflinePlayer op = Bukkit.getOfflinePlayerIfCached(name);
            if (op != null) id = op.getUniqueId();
        }
        if (id == null || !storage.isDenied(id)) {
            messages.send(sender, "apply_reset_not_denied");
            return true;
        }
        storage.clearDenied(id);
        messages.send(sender, "apply_reset_done_operator");
        org.bukkit.entity.Player online = Bukkit.getPlayer(id);
        if (online != null) {
            messages.send(online, "apply_reset_done_player");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String @NotNull [] args) {
        if (!sender.isOp()) return List.of();
        if (args.length != 1) return List.of();
        String prefix = args[0].toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();
        for (String n : storage.listDeniedNames()) if (n.toLowerCase(Locale.ROOT).startsWith(prefix)) out.add(n);
        for (var p : Bukkit.getOnlinePlayers()) {
            String n = p.getName();
            if (n.toLowerCase(Locale.ROOT).startsWith(prefix) && !out.contains(n)) out.add(n);
        }
        return out;
    }
}
