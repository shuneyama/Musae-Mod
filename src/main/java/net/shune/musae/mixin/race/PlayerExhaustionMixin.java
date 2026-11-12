package net.shune.musae.mixin.race;

import net.minecraft.world.entity.player.Player;
import net.shune.musae.auth.AutorizacaoServidor;
import net.shune.musae.race.RaceCapability;
import net.shune.musae.race.RaceConfig;
import net.shune.musae.race.RaceData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Player.class)
public abstract class PlayerExhaustionMixin {

    @ModifyVariable(method = "causeFoodExhaustion", at = @At("HEAD"), argsOnly = true)
    private float musae_modifyExhaustion(float exhaustion) {
        if (!AutorizacaoServidor.isAutorizado()) return exhaustion;
        Player self = (Player) (Object) this;
        RaceData data = RaceCapability.get(self).orElse(null);
        if (data != null && data.getRace().isQuimera()) {
            return (float) (exhaustion * RaceConfig.QUIMERA_EXHAUSTION_MULTIPLIER.get());
        }
        return exhaustion;
    }
}
