package net.shune.musae.mixin.nick;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.shune.musae.auth.AutorizacaoServidor;
import net.shune.musae.nickname.NicknameData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {

    @Inject(method = "getTabListDisplayName", at = @At("HEAD"), cancellable = true)
    private void musae$getTabListDisplayName(CallbackInfoReturnable<Component> cir) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        ServerPlayer self = (ServerPlayer) (Object) this;
        if (self.getServer() == null) return;

        NicknameData data = NicknameData.get(self.getServer());
        MutableComponent styled = data.getStyledNickname(self.getUUID());
        if (styled != null) {
            cir.setReturnValue(styled);
        }
    }
}