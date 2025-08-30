package me.kc1508.fendorisVerify.store;

import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class VerifyStorage {
    private final JavaPlugin plugin;
    private final File file;
    private final Yaml yaml = new Yaml();
    private Map<String, Boolean> data = new LinkedHashMap<>();

    public VerifyStorage(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "verified.yml");
        load();
    }

    public synchronized void load() {
        File dir = file.getParentFile();
        if (!dir.exists()) {
            boolean ok = dir.mkdirs();
            if (!ok) {
                plugin.getLogger().warning("Could not create plugin data folder: " + dir.getAbsolutePath());
            }
        }
        if (!file.exists()) {
            try {
                boolean created = file.createNewFile();
                if (!created) {
                    plugin.getLogger().warning("verified.yml already existed when creating.");
                }
                try (var w = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                    w.write("# Is player Verified?\n");
                }
            } catch (IOException e) {
                plugin.getLogger().warning("Failed creating verified.yml: " + e.getMessage());
            }
        }

        try (var r = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            Object obj = yaml.load(r);
            if (obj instanceof Map<?, ?> raw) {
                data = normalize(raw);
            } else {
                data = new LinkedHashMap<>();
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Failed reading verified.yml: " + e.getMessage());
            data = new LinkedHashMap<>();
        }
    }

    public synchronized void save() {
        try (var w = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            w.write("# Is player Verified?\n");
            yaml.dump(data, w);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed saving verified.yml: " + e.getMessage());
        }
    }

    public synchronized void reload() {
        load();
    }

    public synchronized boolean isVerified(String username) {
        return data.getOrDefault(username.toLowerCase(Locale.ROOT), false);
    }

    public synchronized boolean contains(String username) {
        return data.containsKey(username.toLowerCase(Locale.ROOT));
    }

    public synchronized void setVerified(String username, boolean verified) {
        data.put(username.toLowerCase(Locale.ROOT), verified);
        save();
    }

    private Map<String, Boolean> normalize(Map<?, ?> raw) {
        Map<String, Boolean> tmp = new LinkedHashMap<>();
        for (var e : raw.entrySet()) {
            String key = String.valueOf(e.getKey());
            Object v = e.getValue();
            boolean bool = false;
            if (v instanceof Boolean) bool = (Boolean) v;
            else if (v != null) bool = Boolean.parseBoolean(v.toString());
            tmp.put(key.toLowerCase(Locale.ROOT), bool);
        }
        return tmp;
    }
}