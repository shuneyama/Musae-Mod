package net.shune.musae.mixin.monitor;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import net.shune.musae.auth.AutorizacaoServidor;
import net.shune.musae.monitor.ConfiguracaoMensagens;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerList.class)
public abstract class PlayerListMensagensMixin {

    @Shadow
    private MinecraftServer server;

    @Redirect(
            method = "placeNewPlayer",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"),
            require = 0
    )
    private void musae_redirecionarMensagemEntrada(PlayerList instance, Component mensagem, boolean bypass) {
        if (!AutorizacaoServidor.isAutorizado()) {
            instance.broadcastSystemMessage(mensagem, bypass);
            return;
        }
        if (ConfiguracaoMensagens.get(server).loginAtivo) {
            instance.broadcastSystemMessage(mensagem, bypass);
        }
    }

    @Redirect(
            method = "remove",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"),
            require = 0
    )
    private void musae_redirecionarMensagemSaida(PlayerList instance, Component mensagem, boolean bypass) {
        if (!AutorizacaoServidor.isAutorizado()) {
            instance.broadcastSystemMessage(mensagem, bypass);
            return;
        }
        if (ConfiguracaoMensagens.get(server).saidaAtivo) {
            instance.broadcastSystemMessage(mensagem, bypass);
        }
    }
}
