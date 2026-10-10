package net.shune.musae.item;

import com.google.common.collect.Multimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;
import net.shune.musae.MusaeMod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.List;
import java.util.UUID;

public class RelicCompatHandler {

    private static final UUID MAGE_RING_UUID = UUID.fromString("a7b3c1d4-1234-4e5f-9a8b-c2d3e4f5a6b7");

    /**
     * Adiciona o modifier de cooldown reduction do Iron's Spells.
     * O atributo COOLDOWN_REDUCTION do Iron's Spells usa valores positivos para redução,
     * então 0.10 com MULTIPLY_BASE = -10% cooldown.
     *
     * Se na sua versão do Iron's Spells o atributo funcionar ao contrário
     * (negativo = redução), troque 0.10 por -0.10.
     */
    public static void addMageCooldownModifier(Multimap<Attribute, AttributeModifier> modifiers, UUID uuid) {
        if (!ModList.get().isLoaded("irons_spellbooks")) return;

        try {
            Class<?> attributeRegistryClass = Class.forName(
                    "io.redspace.ironsspellbooks.api.registry.AttributeRegistry");
            var field = attributeRegistryClass.getField("COOLDOWN_REDUCTION");
            Object registryObject = field.get(null);

            var getMethod = registryObject.getClass().getMethod("get");
            Attribute cooldownAttr = (Attribute) getMethod.invoke(registryObject);

            // Iron's Spells: COOLDOWN_REDUCTION positivo = redução de cooldown
            // 0.10 com MULTIPLY_BASE = 10% de redução de cooldown
            modifiers.put(cooldownAttr,
                    new AttributeModifier(uuid, "musae:ring_mage_cooldown", 0.25,
                            AttributeModifier.Operation.MULTIPLY_BASE));

            MusaeMod.LOGGER.debug("[MusaeMod] Modifier de cooldown do Iron's Spells adicionado com sucesso.");

        } catch (Exception e) {
            MusaeMod.LOGGER.warn("[MusaeMod] Falha ao adicionar modifier de cooldown do Iron's Spells: {}", e.getMessage());
        }
    }

    public static boolean hasRelicMedic(Player player) {
        if (!ModList.get().isLoaded("curios")) return false;

        try {
            return CuriosApi.getCuriosInventory(player).resolve().map(handler -> {
                List<SlotResult> results = handler.findCurios(stack ->
                        stack.getItem() instanceof RelicRingItem ring
                                && ring.getRingType() == RelicRingItem.RingType.MEDIC);
                return !results.isEmpty();
            }).orElse(false);
        } catch (Exception e) {
            return false;
        }
    }

    public static float getReviveMultiplier(Player helper) {
        return hasRelicMedic(helper) ? 1.5f : 1.0f;
    }
}