package net.shune.musae.mixin.nick;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.shune.musae.auth.AutorizacaoServidor;
import net.shune.musae.client.ClientNicknameHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerDisplayNameMixin {

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void musae$getDisplayName(CallbackInfoReturnable<Component> cir) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        Player self = (Player) (Object) this;

        if (self.level().isClientSide()) {
            MutableComponent styled = ClientNicknameHandler.getStyledNickname(self.getUUID());
            if (styled != null) {
                cir.setReturnValue(styled);
            }
        }
    }
}
