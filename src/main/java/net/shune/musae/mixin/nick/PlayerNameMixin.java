package net.shune.musae.mixin.nick;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.shune.musae.auth.AutorizacaoServidor;
import net.shune.musae.nickname.NicknameData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerNameMixin {

    @Inject(method = "getName", at = @At("HEAD"), cancellable = true)
    private void musae$getName(CallbackInfoReturnable<Component> cir) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        Player self = (Player) (Object) this;

        if (self.level().isClientSide()) return;

        MinecraftServer server = self.getServer();
        if (server == null) return;

        NicknameData data = NicknameData.get(server);
        MutableComponent styled = data.getStyledNickname(self.getUUID());
        if (styled != null) {
            cir.setReturnValue(styled);
        }
    }
}