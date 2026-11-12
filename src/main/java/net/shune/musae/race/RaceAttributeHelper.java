package net.shune.musae.race;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class RaceAttributeHelper {

    private static final UUID HEALTH_UUID = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");
    private static final UUID ARMOR_UUID = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678902");
    private static final UUID SCALE_UUID = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678903");

    public static void applyHealthModifier(Player player, double amount) {
        AttributeInstance attr = player.getAttribute(Attributes.MAX_HEALTH);
        if (attr == null) return;
        removeModifier(attr, HEALTH_UUID);
        double delta = amount - 20.0;
        if (Math.abs(delta) > 0.001) {
            attr.addPermanentModifier(new AttributeModifier(
                    HEALTH_UUID, "musae:race_health", delta, AttributeModifier.Operation.ADDITION));
        }
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    public static void applyArmorModifier(Player player, double amount) {
        AttributeInstance attr = player.getAttribute(Attributes.ARMOR);
        if (attr == null) return;
        removeModifier(attr, ARMOR_UUID);
        if (amount > 0.001) {
            attr.addPermanentModifier(new AttributeModifier(
                    ARMOR_UUID, "musae:race_armor", amount, AttributeModifier.Operation.ADDITION));
        }
    }

    public static void applyScaleModifier(Player player, double scale) {
        try {
            var scaleAttr = player.getAttribute(
                    net.minecraftforge.registries.ForgeRegistries.ATTRIBUTES.getValue(
                            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("pehkui", "scale")));
            if (scaleAttr != null) {
                removeModifier(scaleAttr, SCALE_UUID);
                double delta = scale - 1.0;
                if (Math.abs(delta) > 0.001) {
                    scaleAttr.addPermanentModifier(new AttributeModifier(
                            SCALE_UUID, "musae:race_scale", delta, AttributeModifier.Operation.ADDITION));
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static void removeAllRaceModifiers(Player player) {
        removeFromAttr(player.getAttribute(Attributes.MAX_HEALTH), HEALTH_UUID);
        removeFromAttr(player.getAttribute(Attributes.ARMOR), ARMOR_UUID);
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static void removeFromAttr(AttributeInstance attr, UUID uuid) {
        if (attr == null) return;
        removeModifier(attr, uuid);
    }

    private static void removeModifier(AttributeInstance attr, UUID uuid) {
        AttributeModifier existing = attr.getModifier(uuid);
        if (existing != null) attr.removeModifier(existing);
    }
}
