package net.shune.musae.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.shune.musae.MusaeMod;
import net.shune.musae.network.NetworkHandler;
import net.shune.musae.network.SyncScalePacket;

public class PlayerScaleData {

    public static final String KEY_SCALE = "musae_scale";

    public static final float MIN_SCALE     = 0.5f;
    public static final float MAX_SCALE     = 1.5f;
    public static final float DEFAULT_SCALE = 1.0f;

    public static final float COMMAND_MIN_SCALE = 0.1f;
    public static final float COMMAND_MAX_SCALE = 5.0f;

    public static final float BASE_WIDTH  = 0.6f;
    public static final float BASE_HEIGHT = 1.8f;

    public static float getScale(Player player) {
        CompoundTag data = player.getPersistentData();
        if (data.contains(KEY_SCALE)) {
            return data.getFloat(KEY_SCALE);
        }
        return DEFAULT_SCALE;
    }

    public static float getScaleFromEntity(Player player) {
        return getScale(player);
    }

    public static float alturaEmPe(float scale) {
        float altura = BASE_HEIGHT * scale;
        if (altura >= 0.995f && altura < 1.03f) {
            return 1.03f;
        }
        return altura;
    }

    public static EntityDimensions dimensoes(Pose pose, float scale) {
        float largura = BASE_WIDTH * scale;
        float altura = switch (pose) {
            case CROUCHING -> 1.5f * scale;
            case SWIMMING, FALL_FLYING, SPIN_ATTACK -> 0.6f * scale;
            default -> alturaEmPe(scale);
        };
        return EntityDimensions.scalable(largura, altura);
    }

    public static float alturaOlhos(Pose pose, float scale) {
        return switch (pose) {
            case CROUCHING -> 1.27f * scale;
            case SWIMMING, FALL_FLYING, SPIN_ATTACK -> 0.4f * scale;
            case SLEEPING -> 0.2f * scale;
            default -> 1.62f * scale;
        };
    }

    public static void setScale(Player player, float scale) {
        setScale(player, scale, MIN_SCALE, MAX_SCALE);
    }

    public static void setScale(Player player, float scale, float min, float max) {
        scale = Math.max(min, Math.min(max, scale));
        player.getPersistentData().putFloat(KEY_SCALE, scale);
        applyScale(player);
    }

    public static void applyScale(Player player) {
        if (player.level().isClientSide()) return;

        float scale = getScale(player);

        player.refreshDimensions();

        if (Math.abs(scale - 1.0f) < 0.001f) {
            ScaleAttributeHelper.removeAllModifiers(player);
        } else {
            ScaleAttributeHelper.applyAllModifiers(player, scale);
        }

        if (player instanceof ServerPlayer sp) {
            SyncScalePacket packet = new SyncScalePacket(player.getUUID(), scale);
            NetworkHandler.sendToAllTracking(sp, packet);
            NetworkHandler.sendToPlayer(sp, packet);
        }

        MusaeMod.LOGGER.debug("[MusaeMod] Escala de {} -> {}", player.getName().getString(), scale);
    }
}
