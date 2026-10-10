package net.shune.musae.client;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shune.musae.nickname.NicknameGradientUtil;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public class ClientNicknameHandler {

    private static final Map<UUID, NicknameCache> NICKNAME_MAP = new HashMap<>();
    private static final Map<String, UUID> NICKNAME_TO_UUID = new HashMap<>();

    public static void setNickname(UUID uuid, String nickname, String colorsStr) {
        NicknameCache old = NICKNAME_MAP.get(uuid);
        if (old != null) {
            NICKNAME_TO_UUID.remove(old.nickname.toLowerCase(Locale.ROOT));
        }

        List<String> colors = NicknameGradientUtil.deserializeColors(colorsStr);
        NICKNAME_MAP.put(uuid, new NicknameCache(nickname, colors));
        NICKNAME_TO_UUID.put(nickname.toLowerCase(Locale.ROOT), uuid);
    }

    public static void removeNickname(UUID uuid) {
        NicknameCache old = NICKNAME_MAP.remove(uuid);
        if (old != null) {
            NICKNAME_TO_UUID.remove(old.nickname.toLowerCase(Locale.ROOT));
        }
    }

    public static String getRawNickname(UUID uuid) {
        NicknameCache cache = NICKNAME_MAP.get(uuid);
        return cache != null ? cache.nickname : null;
    }

    public static MutableComponent getStyledNickname(UUID uuid) {
        NicknameCache cache = NICKNAME_MAP.get(uuid);
        if (cache == null) return null;

        if (cache.colors.isEmpty()) {
            return Component.literal(cache.nickname);
        }

        List<Integer> parsedColors = NicknameGradientUtil.parseColors(cache.colors);
        return NicknameGradientUtil.applyGradient(cache.nickname, parsedColors);
    }

    public static boolean hasNickname(UUID uuid) {
        return NICKNAME_MAP.containsKey(uuid);
    }

    public static Collection<String> getAllNicknames() {
        List<String> nicks = new ArrayList<>();
        for (NicknameCache cache : NICKNAME_MAP.values()) {
            nicks.add(cache.nickname);
        }
        return nicks;
    }

    public static UUID getUUIDByNickname(String nickname) {
        return NICKNAME_TO_UUID.get(nickname.toLowerCase(Locale.ROOT));
    }

    public static void clear() {
        NICKNAME_MAP.clear();
        NICKNAME_TO_UUID.clear();
    }

    private static class NicknameCache {
        final String nickname;
        final List<String> colors;

        NicknameCache(String nickname, List<String> colors) {
            this.nickname = nickname;
            this.colors = colors;
        }
    }
}
