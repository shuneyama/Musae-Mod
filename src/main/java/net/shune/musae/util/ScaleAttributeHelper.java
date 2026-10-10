package net.shune.musae.util;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeMod;

import java.util.UUID;

public class ScaleAttributeHelper {

    private static final UUID SPEED_UUID        = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final UUID STEP_UUID         = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567891");
    private static final UUID JUMP_UUID         = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567892");
    private static final UUID BLOCK_REACH_UUID  = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567893");
    private static final UUID ENTITY_REACH_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567894");

    public static void applyAllModifiers(Player player, float scale) {
        applySpeed(player, scale);
        applyStepHeight(player, scale);
        applyJump(player, scale);
        applyReach(player, scale);
    }

    public static void removeAllModifiers(Player player) {
        removeById(player.getAttribute(Attributes.MOVEMENT_SPEED), SPEED_UUID);
        removeById(player.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get()), STEP_UUID);
        removeById(player.getAttribute(Attributes.JUMP_STRENGTH), JUMP_UUID);
        removeById(player.getAttribute(ForgeMod.BLOCK_REACH.get()), BLOCK_REACH_UUID);
        removeById(player.getAttribute(ForgeMod.ENTITY_REACH.get()), ENTITY_REACH_UUID);
    }

    private static void applySpeed(Player player, float scale) {
        AttributeInstance attr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attr == null) return;
        removeById(attr, SPEED_UUID);
        double amount = (scale - 1.0) * 0.2;
        if (Math.abs(amount) < 0.0001) return;
        attr.addPermanentModifier(new AttributeModifier(
                SPEED_UUID, "musae:scale_speed",
                amount, AttributeModifier.Operation.MULTIPLY_TOTAL));
    }

    private static void applyStepHeight(Player player, float scale) {
        AttributeInstance attr = player.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get());
        if (attr == null) return;
        removeById(attr, STEP_UUID);
        if (scale <= 1.0f) return;
        attr.addPermanentModifier(new AttributeModifier(
                STEP_UUID, "musae:scale_step_height",
                (scale - 1.0) * 0.5, AttributeModifier.Operation.ADDITION));
    }

    private static void applyJump(Player player, float scale) {
        AttributeInstance attr = player.getAttribute(Attributes.JUMP_STRENGTH);
        if (attr == null) return;
        removeById(attr, JUMP_UUID);
        if (scale <= 1.0f) return;
        attr.addPermanentModifier(new AttributeModifier(
                JUMP_UUID, "musae:scale_jump",
                (scale - 1.0) * 0.05, AttributeModifier.Operation.ADDITION));
    }

    private static void applyReach(Player player, float scale) {
        double amount = scale - 1.0;

        AttributeInstance blockReach = player.getAttribute(ForgeMod.BLOCK_REACH.get());
        if (blockReach != null) {
            removeById(blockReach, BLOCK_REACH_UUID);
            if (Math.abs(amount) > 0.0001) {
                blockReach.addPermanentModifier(new AttributeModifier(
                        BLOCK_REACH_UUID, "musae:scale_block_reach",
                        amount, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        }

        AttributeInstance entityReach = player.getAttribute(ForgeMod.ENTITY_REACH.get());
        if (entityReach != null) {
            removeById(entityReach, ENTITY_REACH_UUID);
            if (Math.abs(amount) > 0.0001) {
                entityReach.addPermanentModifier(new AttributeModifier(
                        ENTITY_REACH_UUID, "musae:scale_entity_reach",
                        amount, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        }
    }

    private static void removeById(AttributeInstance attr, UUID uuid) {
        if (attr == null) return;
        AttributeModifier existing = attr.getModifier(uuid);
        if (existing != null) attr.removeModifier(existing);
    }
}