package net.shune.musae.mixin.monitor;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import net.shune.musae.auth.AutorizacaoServidor;
import net.shune.musae.monitor.ConfiguracaoMensagens;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public abstract class PlayerListMensagensMixin {

    @Shadow
    private MinecraftServer server;

    @Inject(
            method = "broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void musae_filtrarMensagemEntradaSaida(Component mensagem, boolean bypass, CallbackInfo ci) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        if (!(mensagem.getContents() instanceof TranslatableContents traduzivel)) return;

        String chave = traduzivel.getKey();
        boolean entrada = chave.equals("multiplayer.player.joined")
                || chave.equals("multiplayer.player.joined.renamed");
        boolean saida = chave.equals("multiplayer.player.left");
        if (!entrada && !saida) return;

        ConfiguracaoMensagens config = ConfiguracaoMensagens.get(server);
        if ((entrada && !config.loginAtivo) || (saida && !config.saidaAtivo)) {
            ci.cancel();
        }
    }
}
