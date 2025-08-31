package me.kc1508.fendorisVerify.service;

import me.kc1508.fendorisVerify.store.ApplicationsStorage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class ApplicationService {
    public static final int TOTAL_QUESTIONS = 5;

    private final ApplicationsStorage storage;
    private final VerifyService verifyService;
    private final MessageService messages;

    private final Set<UUID> seenRules = ConcurrentHashMap.newKeySet();

    public static final class Session {
        public int idx = 0;
        public final String[] answers = new String[TOTAL_QUESTIONS];
    }

    private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> reviewIndex = new ConcurrentHashMap<>();

    public ApplicationService(ApplicationsStorage storage, VerifyService verifyService, MessageService messages) {
        this.storage = storage;
        this.verifyService = verifyService;
        this.messages = messages;
    }

    // expose for listeners
    public boolean consumeTeleportOnJoin(UUID uuid) {
        return storage.consumeTeleportOnJoin(uuid);
    }

    public String consumeNotifyOnJoin(UUID uuid) {
        return storage.consumeNotifyOnJoin(uuid);
    }

    public void markSeenRules(Player p) {
        seenRules.add(p.getUniqueId());
    }

    public boolean canApply(Player p) {
        return seenRules.contains(p.getUniqueId());
    }

    public boolean isApplying(Player p) {
        return sessions.containsKey(p.getUniqueId());
    }

    public boolean hasReviewSession(Player op) {
        return reviewIndex.containsKey(op.getUniqueId());
    }

    public void abortIfApplying(Player p) {
        sessions.remove(p.getUniqueId());
    }

    public void startApplication(Player p) {
        if (verifyService.isVerified(p)) {
            messages.send(p, "apply_already_verified");
            return;
        }
        if (!canApply(p)) {
            messages.send(p, "apply_must_view_rules_first");
            return;
        }
        UUID id = p.getUniqueId();
        if (storage.hasPending(id)) {
            messages.send(p, "apply_already_submitted");
            return;
        }
        if (storage.isDenied(id)) {
            messages.send(p, "apply_denied_cannot_resubmit");
            return;
        }
        sessions.put(id, new Session());
        messages.send(p, "apply_nav");
        sendPrompt(p);
    }

    public void cancelApplication(Player p) {
        sessions.remove(p.getUniqueId());
        messages.send(p, "apply_cancelled");
    }

    public void goBack(Player p) {
        Session s = sessions.get(p.getUniqueId());
        if (s == null) return;
        if (s.idx > 0) s.idx--;
        s.answers[s.idx] = null;
        sendPrompt(p);
    }

    public void handleChat(Player p, String msg) {
        Session s = sessions.get(p.getUniqueId());
        if (s == null) return;
        String t = msg.trim();
        if (t.equalsIgnoreCase("stop") || t.equalsIgnoreCase("cancel")) {
            cancelApplication(p);
            return;
        }
        if (t.equalsIgnoreCase("last")) {
            goBack(p);
            return;
        }
        s.answers[s.idx] = msg;
        s.idx++;
        if (s.idx >= TOTAL_QUESTIONS) {
            Map<String, String> answers = new LinkedHashMap<>();
            answers.put("q1_age", s.answers[0]);
            answers.put("q2_goal", s.answers[1]);
            answers.put("q3_agree_rules", s.answers[2]);
            answers.put("q4_banned_before", s.answers[3]);
            answers.put("q5_password", s.answers[4]);
            boolean anyOpOnline = Bukkit.getOnlinePlayers().stream().anyMatch(Player::isOp);
            storage.putPending(p.getUniqueId(), p.getName(), answers, System.currentTimeMillis(), !anyOpOnline);
            sessions.remove(p.getUniqueId());
            messages.send(p, anyOpOnline ? "apply_submitted" : "apply_no_ops_online");
            if (anyOpOnline) for (Player op : Bukkit.getOnlinePlayers())
                if (op.isOp()) sendApplicationToOperator(op, p.getUniqueId());
            return;
        }
        sendPrompt(p);
    }

    private void sendPrompt(Player p) {
        Session s = sessions.get(p.getUniqueId());
        if (s == null) return;
        switch (s.idx) {
            case 0 -> messages.send(p, "apply_q1");
            case 1 -> messages.send(p, "apply_q2");
            case 2 -> messages.send(p, "apply_q3");
            case 3 -> messages.send(p, "apply_q4");
            case 4 -> messages.send(p, "apply_q5");
        }
    }

    public void sendApplicationToOperator(Player op, UUID applicant) {
        Map<String, Object> data = storage.getPending(applicant);
        if (data == null) return;
        @SuppressWarnings("unchecked") Map<String, Object> answers = (Map<String, Object>) data.get("answers");
        Map<String, String> ph = new LinkedHashMap<>();
        ph.put("player", (String) data.get("name"));
        ph.put("q1", String.valueOf(answers.get("q1_age")));
        ph.put("q2", String.valueOf(answers.get("q2_goal")));
        ph.put("q3", String.valueOf(answers.get("q3_agree_rules")));
        ph.put("q4", String.valueOf(answers.get("q4_banned_before")));
        ph.put("q5", String.valueOf(answers.get("q5_password")));
        messages.send(op, "apply_format_header", ph);
        messages.send(op, "apply_format_q1", ph);
        messages.send(op, "apply_format_q2", ph);
        messages.send(op, "apply_format_q3", ph);
        messages.send(op, "apply_format_q4", ph);
        messages.send(op, "apply_format_q5", ph);
        messages.send(op, "apply_format_footer", ph);
    }

    public void accept(String playerName) {
        List<Map<String, Object>> pending = storage.listPendingOrdered();
        UUID target = null;
        for (Map<String, Object> e : pending) {
            if (playerName.equalsIgnoreCase((String) e.get("name"))) {
                target = UUID.fromString((String) e.get("_uuid"));
                break;
            }
        }
        if (target != null) storage.removePending(target);

        verifyService.setVerified(playerName, true);

        Player p = Bukkit.getPlayerExact(playerName);
        if (p != null) {
            verifyService.enforceState(p);
            verifyService.teleportToSpectatorSpawn(p);
            messages.send(p, "apply_accepted_player");
        } else {
            UUID id = (target != null) ? target : Bukkit.getOfflinePlayer(playerName).getUniqueId();
            storage.addTeleportOnJoin(id);
            storage.addNotifyOnJoin(id, "accepted");
        }

        Map<String, String> ph = new LinkedHashMap<>();
        ph.put("player", playerName);
        Bukkit.getOnlinePlayers().forEach(pl -> messages.send(pl, "apply_accepted_broadcast", ph));
        Bukkit.getConsoleSender().sendMessage("[fendoris-verify] Accepted " + playerName);
    }

    public void deny(String playerName) {
        List<Map<String, Object>> pending = storage.listPendingOrdered();
        UUID target = null;
        Map<String, Object> record = null;
        for (Map<String, Object> e : pending) {
            if (playerName.equalsIgnoreCase((String) e.get("name"))) {
                target = UUID.fromString((String) e.get("_uuid"));
                record = e;
                break;
            }
        }
        if (target == null) return;
        storage.removePending(target);
        storage.markDenied(target, playerName);
        Bukkit.getConsoleSender().sendMessage("[fendoris-verify] Application denied for " + playerName + ": " + record);
        Player p = Bukkit.getPlayerExact(playerName);
        if (p != null) {
            messages.send(p, "apply_denied_player");
        } else {
            storage.addNotifyOnJoin(target, "denied");
        }
    }

    public void startReviewSession(Player op) {
        reviewIndex.put(op.getUniqueId(), 0);
        sendNextInReview(op);
    }

    public void stopReviewSession(Player op) {
        reviewIndex.remove(op.getUniqueId());
        messages.send(op, "apply_review_stopped");
    }

    public void sendNextInReview(Player op) {
        List<Map<String, Object>> list = storage.listPendingOrdered();
        Integer idx = reviewIndex.get(op.getUniqueId());
        if (idx == null) return;
        if (list.isEmpty() || idx >= list.size()) {
            reviewIndex.remove(op.getUniqueId());
            messages.send(op, "apply_review_finished");
            return;
        }
        Map<String, Object> e = list.get(idx);
        UUID uuid = UUID.fromString((String) e.get("_uuid"));
        sendApplicationToOperator(op, uuid);
        messages.send(op, "apply_review_next_hint");
        reviewIndex.put(op.getUniqueId(), idx + 1);
    }

    public void maybePromptOpsOnJoin(Player op) {
        if (!op.isOp()) return;
        int count = storage.pendingCount(); // prompt for any pending, not just "awaiting"
        if (count > 0) {
            Map<String, String> ph = new LinkedHashMap<>();
            ph.put("count", String.valueOf(count));
            messages.send(op, "apply_offline_prompt_to_op", ph);
        }
    }
}
