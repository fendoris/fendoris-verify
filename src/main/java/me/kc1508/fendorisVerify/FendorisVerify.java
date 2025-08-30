package me.kc1508.fendorisVerify;

import me.kc1508.fendorisVerify.commands.ReloadCommand;
import me.kc1508.fendorisVerify.commands.SetSpectatorSpawnPointCommand;
import me.kc1508.fendorisVerify.commands.UnverifyCommand;
import me.kc1508.fendorisVerify.commands.VerifyCommand;
import me.kc1508.fendorisVerify.listener.CommandBlockListener;
import me.kc1508.fendorisVerify.listener.GameModeGuardListener;
import me.kc1508.fendorisVerify.listener.JoinListener;
import me.kc1508.fendorisVerify.listener.MoveLimitListener;
import me.kc1508.fendorisVerify.listener.SpectateGuardListener;
import me.kc1508.fendorisVerify.service.ConfigService;
import me.kc1508.fendorisVerify.service.MessageService;
import me.kc1508.fendorisVerify.service.VerifyService;
import me.kc1508.fendorisVerify.store.VerifyStorage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.PluginCommand;

public final class FendorisVerify extends JavaPlugin {
    private MessageService messages;
    private VerifyStorage storage;
    private VerifyService verifyService;
    private ConfigService configService;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.configService = new ConfigService(this);
        this.messages = new MessageService(this);
        this.storage = new VerifyStorage(this);
        this.verifyService = new VerifyService(storage, configService, messages);

        registerCommands();
        registerListeners();

        for (Player p : Bukkit.getOnlinePlayers()) {
            verifyService.enforceState(p);
        }
    }

    private void registerCommands() {
        VerifyCommand verifyCmd = new VerifyCommand(verifyService, messages);
        PluginCommand verify = getCommand("verify");
        if (verify != null) {
            verify.setExecutor(verifyCmd);
        } else {
            getLogger().severe("Command 'verify' missing from plugin.yml");
        }

        PluginCommand unverify = getCommand("unverify");
        if (unverify != null) {
            unverify.setExecutor(new UnverifyCommand(verifyService, messages));
        } else {
            getLogger().severe("Command 'unverify' missing from plugin.yml");
        }

        PluginCommand setSpawn = getCommand("setspectatorspawnpoint");
        if (setSpawn != null) {
            setSpawn.setExecutor(new SetSpectatorSpawnPointCommand(configService, messages));
        } else {
            getLogger().severe("Command 'setspectatorspawnpoint' missing from plugin.yml");
        }

        PluginCommand reload = getCommand("fendorisverifyreload");
        if (reload != null) {
            reload.setExecutor(new ReloadCommand(configService, storage, verifyService, messages));
        } else {
            getLogger().severe("Command 'fendorisverifyreload' missing from plugin.yml");
        }
    }

    private void registerListeners() {
        var pm = getServer().getPluginManager();
        pm.registerEvents(new JoinListener(this, verifyService, messages), this);
        pm.registerEvents(new MoveLimitListener(configService, verifyService, messages), this);
        pm.registerEvents(new CommandBlockListener(verifyService, messages), this);
        pm.registerEvents(new GameModeGuardListener(verifyService), this);
        pm.registerEvents(new SpectateGuardListener(verifyService, messages), this);
    }

    @Override
    public void onDisable() {
        if (storage != null) storage.save();
    }
}
