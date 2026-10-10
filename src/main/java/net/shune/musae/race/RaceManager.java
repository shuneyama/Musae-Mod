package net.shune.musae.race;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.shune.musae.network.NetworkHandler;
import net.shune.musae.network.SyncRacePacket;
import net.shune.musae.util.PlayerScaleData;

public class RaceManager {

    public static void setRace(ServerPlayer player, RaceType newRace) {
        RaceData data = RaceCapability.resolve(player);
        RaceType oldRace = data.getRace();

        if (oldRace == newRace && newRace != RaceType.NENHUMA) {
            player.sendSystemMessage(Component.literal("Você já possui essa raça!")
                    .withStyle(ChatFormatting.YELLOW));
            return;
        }

        removeRace(player, data, oldRace);

        data.setRace(newRace);

        applyRace(player, data, newRace);

        syncToClient(player, data);

        if (newRace == RaceType.NENHUMA) {
            player.sendSystemMessage(Component.literal("Sua raça foi removida.")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            player.sendSystemMessage(Component.literal("Sua raça agora é: " + newRace.getDisplayName())
                    .withStyle(ChatFormatting.GREEN));
        }
    }

    public static void removeRace(ServerPlayer player, RaceData data, RaceType race) {
        RaceAttributeHelper.removeAllRaceModifiers(player);

        player.removeAllEffects();

        switch (race) {
            case QUIMERA_ALADA -> removeRaceElytra(player, data);
            case QUIMERA_INSETO -> PlayerScaleData.setScale(player, PlayerScaleData.DEFAULT_SCALE);
            case AQUARIANO -> player.setNoGravity(false);
            default -> {}
        }

        data.setRace(RaceType.NENHUMA);
    }

    public static void applyRace(ServerPlayer player, RaceData data, RaceType race) {
        switch (race) {
            case DEMONIO -> {
                double targetHealth = 20.0 + RaceConfig.DEMONIO_EXTRA_HEALTH.get();
                RaceAttributeHelper.applyHealthModifier(player, targetHealth);
                player.setHealth(player.getMaxHealth());
            }
            case HUMANO -> {
                RaceAttributeHelper.applyHealthModifier(player, 20.0);
            }
            case QUIMERA -> {}
            case QUIMERA_ALADA -> {
                RaceAttributeHelper.applyHealthModifier(player, RaceConfig.QUIMERA_ALADA_HEALTH.get());
                giveRaceElytra(player, data);
                if (player.getHealth() > player.getMaxHealth()) {
                    player.setHealth(player.getMaxHealth());
                }
            }
            case QUIMERA_INSETO -> {
                RaceAttributeHelper.applyHealthModifier(player, RaceConfig.QUIMERA_INSETO_HEALTH.get());
                RaceAttributeHelper.applyArmorModifier(player, RaceConfig.QUIMERA_INSETO_ARMOR.get());
                PlayerScaleData.setScale(player, 1.00f / 1.80f);
                if (player.getHealth() > player.getMaxHealth()) {
                    player.setHealth(player.getMaxHealth());
                }
            }
            case AQUARIANO -> {}
            case ANJO -> {
                data.setAnjoCharges(RaceConfig.ANJO_MAX_CHARGES.get());
                data.setAnjoRegenTimer(RaceConfig.ANJO_CHARGE_REGEN_TICKS.get());
            }
            default -> {}
        }
    }

    public static void reapplyOnRespawnOrJoin(ServerPlayer player) {
        RaceData data = RaceCapability.resolve(player);
        if (data.getRace() == RaceType.NENHUMA) return;
        applyRace(player, data, data.getRace());
        syncToClient(player, data);
    }

    public static void syncToClient(ServerPlayer player, RaceData data) {
        NetworkHandler.sendToPlayer(player, new SyncRacePacket(
                data.getRace().getId(),
                data.getCooldownTicks(),
                data.getAnjoCharges(),
                data.isIraAtiva(),
                data.isVenenoProximoAtaque(),
                data.isEspiritoAtivo(),
                data.hasRaceElytra()
        ));
    }

    private static void giveRaceElytra(ServerPlayer player, RaceData data) {
        if (data.hasRaceElytra()) return;

        ItemStack elytra = new ItemStack(Items.ELYTRA);
        elytra.enchant(Enchantments.BINDING_CURSE, 1);
        CompoundTag tag = elytra.getOrCreateTag();
        tag.putBoolean("raca_elytra", true);
        tag.putBoolean("Unbreakable", true);
        elytra.hideTooltipPart(ItemStack.TooltipPart.ENCHANTMENTS);
        elytra.hideTooltipPart(ItemStack.TooltipPart.UNBREAKABLE);

        data.setRaceElytra(elytra);
    }

    private static void removeRaceElytra(ServerPlayer player, RaceData data) {
        data.setRaceElytra(ItemStack.EMPTY);

        ItemStack chest = player.getInventory().armor.get(2);
        if (isRaceElytra(chest)) {
            player.getInventory().armor.set(2, ItemStack.EMPTY);
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (isRaceElytra(stack)) {
                player.getInventory().setItem(i, ItemStack.EMPTY);
            }
        }
    }

    public static boolean isRaceElytra(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.getOrCreateTag().getBoolean("raca_elytra");
    }

    public static boolean hasRaceElytraEquipped(net.minecraft.world.entity.player.Player player) {
        if (player.level().isClientSide()) {
            return net.shune.musae.client.ClientRaceHandler.hasRaceElytra();
        }
        RaceData data = RaceCapability.get(player).orElse(null);
        return data != null && data.hasRaceElytra();
    }
}