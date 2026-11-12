package net.shune.musae.mixin.nick;

import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.shune.musae.auth.AutorizacaoServidor;
import net.shune.musae.util.PlayerScaleData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerEntityMixin {

    @Inject(method = "getDimensions", at = @At("HEAD"), cancellable = true)
    private void musae$getDimensions(Pose pose,
                                     CallbackInfoReturnable<EntityDimensions> cir) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        Player self = (Player) (Object) this;
        float scale = PlayerScaleData.getScale(self);
        if (Math.abs(scale - 1.0f) <= 0.001f) return;

        cir.setReturnValue(PlayerScaleData.dimensoes(pose, scale));
    }
}
