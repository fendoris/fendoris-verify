package me.kc1508.fendorisVerify.commands;

import me.kc1508.fendorisVerify.service.ConfigService;
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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class VerifyCommand implements CommandExecutor, TabCompleter {
    private final VerifyService verify;
    private final MessageService messages;
    private final ConfigService config;

    private static final Map<UUID, Long> LAST_INFO = new ConcurrentHashMap<>();

    public VerifyCommand(VerifyService verify, MessageService messages, ConfigService config) {
        this.verify = verify;
        this.messages = messages;
        this.config = config;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player p)) {
            messages.send(sender, "not_player");
            return true;
        }

        if (args.length == 0) {
            int cd = Math.max(0, config.verifyInfoCooldownSeconds());
            long now = System.currentTimeMillis();
            long last = LAST_INFO.getOrDefault(p.getUniqueId(), 0L);
            if (cd > 0 && (now - last) < cd * 1000L) {
                messages.send(p, "verify_info_cooldown_text");
                return true;
            }
            LAST_INFO.put(p.getUniqueId(), now);

            if (verify.isVerified(p)) {
                messages.send(p, "verify_already");
            } else {
                messages.send(p, "verify_info");
            }
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("me")) {
            if (verify.isVerified(p)) {
                messages.send(p, "verify_cannot_reverify");
                return true;
            }
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
        return Collections.emptyList();
    }
}
