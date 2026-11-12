package net.shune.musae.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientRaceEmojiHandler {

    private static final Map<UUID, String> EMOJIS = new HashMap<>();

    public static void apply(boolean replace, Map<UUID, String> entries) {
        if (replace) EMOJIS.clear();
        for (Map.Entry<UUID, String> e : entries.entrySet()) {
            if (e.getValue() == null || e.getValue().isEmpty()) {
                EMOJIS.remove(e.getKey());
            } else {
                EMOJIS.put(e.getKey(), e.getValue());
            }
        }
    }

    public static String get(UUID uuid) {
        return EMOJIS.getOrDefault(uuid, "");
    }
}
