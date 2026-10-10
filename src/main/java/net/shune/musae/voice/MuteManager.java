package net.shune.musae.voice;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MuteManager {

    private static final Set<UUID> mutedPlayers = Collections.synchronizedSet(new HashSet<>());
    private static final Set<UUID> whitelistedPlayers = Collections.synchronizedSet(new HashSet<>());
    private static final Set<UUID> broadcastPlayers = Collections.synchronizedSet(new HashSet<>());

    // ==================== MUTE ====================

    public static void mutePlayer(UUID playerUUID) {
        mutedPlayers.add(playerUUID);
    }

    public static void unmutePlayer(UUID playerUUID) {
        mutedPlayers.remove(playerUUID);
    }

    public static Set<UUID> getMutedPlayers() {
        return new HashSet<>(mutedPlayers);
    }

    // ==================== WHITELIST ====================

    public static void addToWhitelist(UUID playerUUID) {
        whitelistedPlayers.add(playerUUID);
    }

    public static void removeFromWhitelist(UUID playerUUID) {
        whitelistedPlayers.remove(playerUUID);
    }

    public static Set<UUID> getWhitelistedPlayers() {
        return new HashSet<>(whitelistedPlayers);
    }

    // ==================== VOZ UNIVERSAL ====================

    public static void enableBroadcast(UUID playerUUID) {
        broadcastPlayers.add(playerUUID);
    }

    public static void disableBroadcast(UUID playerUUID) {
        broadcastPlayers.remove(playerUUID);
    }

    public static boolean isBroadcasting(UUID playerUUID) {
        return broadcastPlayers.contains(playerUUID);
    }

    public static Set<UUID> getBroadcastPlayers() {
        return new HashSet<>(broadcastPlayers);
    }

    // ==================== VERIFICACAO ====================

    public static boolean canPlayerSpeak(UUID playerUUID) {
        if (whitelistedPlayers.contains(playerUUID)) {
            return true;
        }

        if (mutedPlayers.contains(playerUUID)) {
            return false;
        }

        return true;
    }
}
