package net.shune.musae.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shune.musae.MusaeMod;
import net.shune.musae.util.PlayerScaleData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class ClientScaleHandler {

    private static final Map<UUID, Float> SCALE_MAP = new HashMap<>();
    private static final Map<UUID, Float> PREVIEW_OVERRIDE = new HashMap<>();

    public static void applyScale(UUID playerUUID, float scale) {
        SCALE_MAP.put(playerUUID, scale);
        MusaeMod.LOGGER.debug("[MusaeMod] Cliente recebeu escala {} para UUID {}", scale, playerUUID);
    }

    public static void applyScaleToRenderedPlayer(UUID playerUUID, float scale) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        for (Entity e : mc.level.entitiesForRendering()) {
            if (e instanceof Player player && player.getUUID().equals(playerUUID)) {
                player.getPersistentData().putFloat(PlayerScaleData.KEY_SCALE, scale);
                player.refreshDimensions();
                break;
            }
        }
    }


    public static float getScale(UUID playerUUID) {
        Float override = PREVIEW_OVERRIDE.get(playerUUID);
        if (override != null) return override;
        return SCALE_MAP.getOrDefault(playerUUID, 1.0f);
    }

    public static void removePlayer(UUID playerUUID) {
        SCALE_MAP.remove(playerUUID);
        PREVIEW_OVERRIDE.remove(playerUUID);
    }

    public static void setPreviewOverride(UUID playerUUID, float scale) {
        PREVIEW_OVERRIDE.put(playerUUID, scale);
    }

    public static void clearPreviewOverride(UUID playerUUID) {
        PREVIEW_OVERRIDE.remove(playerUUID);
    }
}