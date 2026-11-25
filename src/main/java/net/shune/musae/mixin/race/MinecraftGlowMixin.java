package net.shune.musae.mixin.race;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.shune.musae.auth.AutorizacaoServidor;
import net.shune.musae.client.ClientFarejarHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftGlowMixin {

    @Inject(method = "shouldEntityAppearGlowing", at = @At("HEAD"), cancellable = true)
    private void musae_brilhoFarejar(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        if (ClientFarejarHandler.brilhando(entity.getId())) {
            cir.setReturnValue(true);
        }
    }
}
