package net.shune.musae.item;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;
import net.shune.musae.auth.AutorizacaoServidor;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * Item base para todos os anéis relíquia do Musae Mod.
 * Equipável via Curios no slot "ring".
 * Aplica attribute modifiers via UUID (não altera atributo base).
 */
public class RelicRingItem extends Item implements ICurioItem {

    private final RingType ringType;

    public RelicRingItem(Properties properties, RingType ringType) {
        super(properties);
        this.ringType = ringType;
    }

    public RingType getRingType() {
        return ringType;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = LinkedHashMultimap.create();
        if (!AutorizacaoServidor.isAutorizado()) return modifiers;

        switch (ringType) {
            case BLACKSMITH -> {
                // Velocidade de mineração via RelicEventHandler (BreakSpeed event)
                // Não usa attribute modifier porque dig speed não é atributo vanilla no 1.20.1
            }
            case KNIGHT -> {
                // +1 dano de ataque
                modifiers.put(
                        Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(uuid, "musae:ring_knight", 2.0,
                                AttributeModifier.Operation.ADDITION)
                );
            }
            case MAGE -> {
                // Cooldown reduction do Iron's Spells (se presente)
                RelicCompatHandler.addMageCooldownModifier(modifiers, uuid);
            }
            case ROGUE -> {
                // +50% velocidade de movimento
                modifiers.put(
                        Attributes.MOVEMENT_SPEED,
                        new AttributeModifier(uuid, "musae:ring_rogue", 0.50,
                                AttributeModifier.Operation.MULTIPLY_BASE)
                );
            }
            case SWORDSMAN -> {
                // +1 attack speed (adição fixa)
                modifiers.put(
                        Attributes.ATTACK_SPEED,
                        new AttributeModifier(uuid, "musae:ring_swordsman", 1.0,
                                AttributeModifier.Operation.ADDITION)
                );
            }
            case MEDIC -> {
                // Sem atributo vanilla — efeito gerenciado pelo RelicEventHandler
                // (boost no progresso de revive do PlayerRevive)
            }
        }

        return modifiers;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        switch (ringType) {
            case BLACKSMITH -> tooltip.add(Component.literal(" Haste I")
                    .withStyle(ChatFormatting.GREEN));
            case MEDIC -> {
                if (ModList.get().isLoaded("playerrevive")) {
                    tooltip.add(Component.literal("Ressuscitação Acelerada")
                            .withStyle(ChatFormatting.GREEN));
                } else {
                    tooltip.add(Component.literal(" (Requer Player Revive)")
                            .withStyle(ChatFormatting.DARK_GRAY));
                }
            }
        }
    }

    public enum RingType {
        BLACKSMITH,
        KNIGHT,
        MAGE,
        ROGUE,
        SWORDSMAN,
        MEDIC
    }
}