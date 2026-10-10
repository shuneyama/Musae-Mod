package net.shune.musae.mixin.race;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.shune.musae.auth.AutorizacaoServidor;
import net.shune.musae.race.RaceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class ElytraFlightMixin {

    @Redirect(
            method = "updateFallFlying",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"),
            require = 0
    )
    private ItemStack musae_redirectChestSlotForElytra(LivingEntity instance, EquipmentSlot slot) {
        if (!AutorizacaoServidor.isAutorizado()) return instance.getItemBySlot(slot);
        ItemStack original = instance.getItemBySlot(slot);

        if (slot != EquipmentSlot.CHEST || !(instance instanceof Player player)) {
            return original;
        }

        if (original.getItem() == Items.ELYTRA) {
            return original;
        }

        if (RaceManager.hasRaceElytraEquipped(player)) {
            if (player.level().isClientSide()) {
                ItemStack fake = new ItemStack(Items.ELYTRA);
                fake.getOrCreateTag().putBoolean("Unbreakable", true);
                fake.getOrCreateTag().putBoolean("raca_elytra", true);
                return fake;
            } else {
                net.shune.musae.race.RaceData data =
                        net.shune.musae.race.RaceCapability.get(player).orElse(null);
                if (data != null && data.hasRaceElytra()) {
                    return data.getRaceElytra();
                }
            }
        }

        return original;
    }
}