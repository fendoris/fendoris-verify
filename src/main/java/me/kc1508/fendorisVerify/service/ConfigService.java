package me.kc1508.fendorisVerify.service;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

public final class ConfigService {
    private final JavaPlugin plugin;

    public ConfigService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public int spectatorRadius() {
        return plugin.getConfig().getInt("spectator-radius", 100);
    }

    public Location spectatorSpawn() {
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("spectator-spawn");
        if (sec == null) return null;
        String worldName = sec.getString("world", "world");
        World w = Bukkit.getWorld(worldName);
        if (w == null) return null;
        double x = sec.getDouble("x", 0.0);
        double y = sec.getDouble("y", 100.0);
        double z = sec.getDouble("z", 0.0);
        float yaw = (float) sec.getDouble("yaw", 0.0);
        float pitch = (float) sec.getDouble("pitch", 0.0);
        return new Location(w, x, y, z, yaw, pitch);
    }

    public void setSpectatorSpawn(Location loc) {
        plugin.getConfig().set("spectator-spawn.world", loc.getWorld().getName());
        plugin.getConfig().set("spectator-spawn.x", loc.getX());
        plugin.getConfig().set("spectator-spawn.y", loc.getY());
        plugin.getConfig().set("spectator-spawn.z", loc.getZ());
        plugin.getConfig().set("spectator-spawn.yaw", loc.getYaw());
        plugin.getConfig().set("spectator-spawn.pitch", loc.getPitch());
        plugin.saveConfig();
    }

    public void reload() {
        plugin.reloadConfig();
    }
}