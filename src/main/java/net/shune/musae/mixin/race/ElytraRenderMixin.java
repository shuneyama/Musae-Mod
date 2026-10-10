package net.shune.musae.mixin.race;

import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.shune.musae.auth.AutorizacaoServidor;
import net.shune.musae.client.ClientRaceHandler;
import net.shune.musae.race.RaceType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ElytraLayer.class)
public abstract class ElytraRenderMixin {

    @Redirect(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;")
    )
    private ItemStack musae_redirectElytraRender(LivingEntity instance, EquipmentSlot slot) {
        if (!AutorizacaoServidor.isAutorizado()) return instance.getItemBySlot(slot);
        ItemStack original = instance.getItemBySlot(slot);

        if (slot != EquipmentSlot.CHEST || !(instance instanceof Player)) {
            return original;
        }

        if (original.getItem() == Items.ELYTRA) {
            return original;
        }

        if (ClientRaceHandler.getRace() == RaceType.QUIMERA_ALADA && ClientRaceHandler.hasRaceElytra()) {
            ItemStack fake = new ItemStack(Items.ELYTRA);
            fake.getOrCreateTag().putBoolean("raca_elytra", true);
            return fake;
        }

        return original;
    }
}