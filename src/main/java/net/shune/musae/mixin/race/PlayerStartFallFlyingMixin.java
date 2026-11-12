package net.shune.musae.mixin.race;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.shune.musae.auth.AutorizacaoServidor;
import net.shune.musae.race.RaceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerStartFallFlyingMixin {

    @Inject(method = "tryToStartFallFlying", at = @At("HEAD"), cancellable = true)
    private void musae_tryToStartFallFlyingForRaceElytra(CallbackInfoReturnable<Boolean> cir) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        Player self = (Player) (Object) this;

        if (self.onGround()) return;
        if (self.isFallFlying()) return;
        if (self.isInWater()) return;
        if (self.hasEffect(MobEffects.LEVITATION)) return;
        if (!RaceManager.hasRaceElytraEquipped(self)) return;

        net.minecraft.world.item.ItemStack chest = self.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST);
        if (chest.getItem() == net.minecraft.world.item.Items.ELYTRA) return;

        self.startFallFlying();
        cir.setReturnValue(true);
    }
}