package net.shune.musae.chat;

import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ChatEntrada {

    private static final long JANELA_DUPLICIDADE_MS = 500L;
    private static final Map<UUID, Registro> ULTIMOS = new ConcurrentHashMap<>();

    private ChatEntrada() {
    }

    public static void receber(ServerPlayer jogador, String mensagem) {
        if (jogador == null || jogador.getServer() == null || mensagem == null) return;
        long agora = System.currentTimeMillis();
        Registro anterior = ULTIMOS.get(jogador.getUUID());
        if (anterior != null && anterior.mensagem.equals(mensagem) && agora - anterior.instante < JANELA_DUPLICIDADE_MS) {
            return;
        }
        ULTIMOS.put(jogador.getUUID(), new Registro(mensagem, agora));
        jogador.getServer().execute(() -> ChatManager.enviarMensagemLocal(jogador, mensagem));
    }

    private record Registro(String mensagem, long instante) {
    }
}
