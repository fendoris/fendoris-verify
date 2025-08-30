package me.kc1508.fendorisVerify;

import me.kc1508.fendorisVerify.commands.*;
import me.kc1508.fendorisVerify.listener.ChatApplyListener;
import me.kc1508.fendorisVerify.listener.CommandBlockListener;
import me.kc1508.fendorisVerify.listener.GameModeGuardListener;
import me.kc1508.fendorisVerify.listener.JoinListener;
import me.kc1508.fendorisVerify.listener.QuitApplyListener;
import me.kc1508.fendorisVerify.service.*;
import me.kc1508.fendorisVerify.store.ApplicationsStorage;
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
    private ApplicationsStorage applicationsStorage;
    private ApplicationService applicationService;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.configService = new ConfigService(this);
        this.messages = new MessageService(this);
        this.storage = new VerifyStorage(this);
        this.verifyService = new VerifyService(storage, configService, messages);
        this.applicationsStorage = new ApplicationsStorage(this);
        this.applicationService = new ApplicationService(applicationsStorage, verifyService, messages);

        registerCommands();
        registerListeners();

        for (Player p : Bukkit.getOnlinePlayers()) {
            verifyService.enforceState(p);
        }
    }

    private void registerCommands() {
        VerifyCommand verifyCmd = new VerifyCommand(verifyService, messages, applicationService);
        PluginCommand verify = getCommand("verify");
        if (verify != null) { verify.setExecutor(verifyCmd); verify.setTabCompleter(verifyCmd); } else getLogger().severe("Command 'verify' missing from plugin.yml");

        ApplyCommand applyCmd = new ApplyCommand(applicationService, messages);
        PluginCommand apply = getCommand("apply");
        if (apply != null) { apply.setExecutor(applyCmd); apply.setTabCompleter(applyCmd); } else getLogger().severe("Command 'apply' missing from plugin.yml");

        PluginCommand see = getCommand("applyseeoffline");
        if (see != null) see.setExecutor(new ApplySeeOfflineCommand(applicationService, messages)); else getLogger().severe("Command 'applyseeoffline' missing from plugin.yml");

        PluginCommand accept = getCommand("applyaccept");
        if (accept != null) accept.setExecutor(new ApplyAcceptCommand(applicationService, messages)); else getLogger().severe("Command 'applyaccept' missing from plugin.yml");

        PluginCommand deny = getCommand("applydeny");
        if (deny != null) deny.setExecutor(new ApplyDenyCommand(applicationService, messages)); else getLogger().severe("Command 'applydeny' missing from plugin.yml");

        PluginCommand reset = getCommand("applyreset");
        if (reset != null) reset.setExecutor(new ApplyResetCommand(applicationsStorage, messages)); else getLogger().severe("Command 'applyreset' missing from plugin.yml");

        PluginCommand unverify = getCommand("unverify");
        if (unverify != null) unverify.setExecutor(new UnverifyCommand(verifyService, messages)); else getLogger().severe("Command 'unverify' missing from plugin.yml");

        PluginCommand setSpawn = getCommand("setspectatorspawnpoint");
        if (setSpawn != null) setSpawn.setExecutor(new SetSpectatorSpawnPointCommand(configService, messages)); else getLogger().severe("Command 'setspectatorspawnpoint' missing from plugin.yml");

        PluginCommand reload = getCommand("fendorisverifyreload");
        if (reload != null) reload.setExecutor(new ReloadCommand(configService, storage, verifyService, messages)); else getLogger().severe("Command 'fendorisverifyreload' missing from plugin.yml");
    }

    private void registerListeners() {
        var pm = getServer().getPluginManager();
        pm.registerEvents(new JoinListener(this, verifyService, messages, applicationService), this);
        pm.registerEvents(new CommandBlockListener(verifyService, messages, applicationService), this);
        pm.registerEvents(new GameModeGuardListener(verifyService), this);
        pm.registerEvents(new ChatApplyListener(applicationService), this);
        pm.registerEvents(new QuitApplyListener(applicationService), this);
    }

    @Override
    public void onDisable() {
        if (storage != null) storage.save();
    }
}
