package me.kc1508.fendorisVerify.service;

import me.kc1508.fendorisVerify.store.VerifyStorage;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class VerifyService {
    private final VerifyStorage storage;
    private final ConfigService config;

    public VerifyService(VerifyStorage storage, ConfigService config) {
        this.storage = storage;
        this.config = config;
    }

    public boolean isVerified(Player p) {
        return storage.isVerified(p.getName());
    }

    public boolean hasStoredRecord(String username) {
        return storage.contains(username);
    }

    public void setVerified(Player p, boolean verified) {
        storage.setVerified(p.getName(), verified);
    }

    public void setVerified(String username, boolean verified) {
        storage.setVerified(username, verified);
    }

    public void enforceState(Player p) {
        boolean opBypass = p.isOp();
        if (!isVerified(p)) {
            if (!opBypass && p.getGameMode() != GameMode.SPECTATOR) {
                p.setGameMode(GameMode.SPECTATOR);
            }
        } else {
            if (!opBypass && p.getGameMode() != GameMode.SURVIVAL) {
                p.setGameMode(GameMode.SURVIVAL);
            }
        }
    }

    public void teleportToSpectatorSpawn(Player p) {
        Location spawn = config.spectatorSpawn();
        if (spawn != null) p.teleport(spawn);
    }
}
