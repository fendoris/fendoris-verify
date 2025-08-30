package me.kc1508.fendorisVerify.store;

import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@SuppressWarnings("unchecked")
public final class ApplicationsStorage {
    private final JavaPlugin plugin;
    private final File file;
    private final Yaml yaml = new Yaml();

    private Map<String, Object> root = new LinkedHashMap<>();

    public ApplicationsStorage(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "applications.yml");
        load();
    }

    public synchronized void load() {
        File dir = file.getParentFile();
        if (!dir.exists() && !dir.mkdirs()) {
            plugin.getLogger().warning("Could not create data folder: " + dir.getAbsolutePath());
        }
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    try (var w = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                        w.write("# Pending applications\n");
                        w.write("pending: {}\n");
                        w.write("awaiting_review: []\n");
                        w.write("denied: {}\n");
                    }
                }
            } catch (IOException e) {
                plugin.getLogger().warning("Failed creating applications.yml: " + e.getMessage());
            }
        }
        try (var r = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            Object obj = yaml.load(r);
            if (obj instanceof Map) {
                root = (Map<String, Object>) obj;
            } else {
                root = new LinkedHashMap<>();
            }
            root.putIfAbsent("pending", new LinkedHashMap<>());
            root.putIfAbsent("awaiting_review", new ArrayList<>());
            root.putIfAbsent("denied", new LinkedHashMap<>());
        } catch (IOException e) {
            plugin.getLogger().warning("Failed reading applications.yml: " + e.getMessage());
            root = new LinkedHashMap<>();
            root.put("pending", new LinkedHashMap<>());
            root.put("awaiting_review", new ArrayList<>());
            root.put("denied", new LinkedHashMap<>());
        }
    }

    public synchronized void save() {
        try (var w = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            yaml.dump(root, w);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed saving applications.yml: " + e.getMessage());
        }
    }

    public synchronized void reload() { load(); }

    public synchronized boolean hasPending(UUID uuid) {
        Map<String, Object> pending = (Map<String, Object>) root.get("pending");
        return pending.containsKey(uuid.toString());
    }

    public synchronized void putPending(UUID uuid, String name, Map<String, String> answers, long submittedAt, boolean queueForOfflineReview) {
        Map<String, Object> pending = (Map<String, Object>) root.get("pending");
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("name", name);
        entry.put("submitted_at", submittedAt);
        entry.put("answers", new LinkedHashMap<>(answers));
        pending.put(uuid.toString(), entry);

        if (queueForOfflineReview) {
            List<String> awaiting = (List<String>) root.get("awaiting_review");
            if (!awaiting.contains(uuid.toString())) awaiting.add(uuid.toString());
        }
        save();
    }

    public synchronized Map<String, Object> getPending(UUID uuid) {
        Map<String, Object> pending = (Map<String, Object>) root.get("pending");
        if (!pending.containsKey(uuid.toString())) return null;
        return (Map<String, Object>) pending.get(uuid.toString());
    }

    public synchronized List<Map<String, Object>> listPendingOrdered() {
        Map<String, Object> pending = (Map<String, Object>) root.get("pending");
        List<Map<String, Object>> list = new ArrayList<>();
        for (var e : pending.entrySet()) {
            Map<String, Object> entry = (Map<String, Object>) e.getValue();
            entry = new LinkedHashMap<>(entry);
            entry.put("_uuid", e.getKey());
            list.add(entry);
        }
        list.sort(Comparator.comparingLong(m -> ((Number) m.getOrDefault("submitted_at", 0L)).longValue()));
        return list;
    }

    public synchronized void removePending(UUID uuid) {
        Map<String, Object> pending = (Map<String, Object>) root.get("pending");
        pending.remove(uuid.toString());
        List<String> awaiting = (List<String>) root.get("awaiting_review");
        awaiting.remove(uuid.toString());
        save();
    }

    public synchronized void markDenied(UUID uuid) {
        Map<String, Object> denied = (Map<String, Object>) root.get("denied");
        denied.put(uuid.toString(), Boolean.TRUE);
        save();
    }

    public synchronized boolean isDenied(UUID uuid) {
        Map<String, Object> denied = (Map<String, Object>) root.get("denied");
        return Boolean.TRUE.equals(denied.get(uuid.toString()));
    }

    public synchronized int awaitingCount() {
        List<String> awaiting = (List<String>) root.get("awaiting_review");
        return awaiting.size();
    }

    public synchronized boolean hasAwaiting() {
        return awaitingCount() > 0;
    }
}
