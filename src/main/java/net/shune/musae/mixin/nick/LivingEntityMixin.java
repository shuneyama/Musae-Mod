package net.shune.musae.mixin.nick;

import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.shune.musae.auth.AutorizacaoServidor;
import net.shune.musae.util.PlayerScaleData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "getEyeHeight", at = @At("HEAD"), cancellable = true)
    private void musae$getEyeHeight(Pose pose, EntityDimensions dims,
                                    CallbackInfoReturnable<Float> cir) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        if (!((Object) this instanceof Player player)) return;

        float scale = PlayerScaleData.getScale(player);
        if (Math.abs(scale - 1.0f) <= 0.001f) return;

        float eyeHeight = switch (pose) {
            case CROUCHING -> 1.27f * scale;
            case SWIMMING, FALL_FLYING, SPIN_ATTACK -> 0.4f * scale;
            case SLEEPING -> 0.2f * scale;
            default -> 1.62f * scale;
        };

        cir.setReturnValue(eyeHeight);
    }
}