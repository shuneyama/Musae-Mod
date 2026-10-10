package net.shune.musae.mixin.race;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.shune.musae.auth.AutorizacaoServidor;
import net.shune.musae.race.RaceCapability;
import net.shune.musae.race.RaceData;
import net.shune.musae.race.RaceManager;
import net.shune.musae.race.RaceType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(Inventory.class)
public abstract class ElytraProtectionMixin {

    @Shadow @Final public Player player;

    @Inject(method = "removeItem(Lnet/minecraft/world/item/ItemStack;)V", at = @At("HEAD"), cancellable = true)
    private void musae_preventElytraRemove(ItemStack stack, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        if (RaceManager.isRaceElytra(stack)) {
            RaceData data = RaceCapability.get(player).orElse(null);
            if (data != null && data.getRace() == RaceType.QUIMERA_ALADA) {
                ci.cancel();
            }
        }
    }
}