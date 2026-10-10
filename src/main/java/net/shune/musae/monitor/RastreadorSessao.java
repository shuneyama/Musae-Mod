package net.shune.musae.monitor;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class RastreadorSessao {

    public static void aoEntrar(ServerPlayer jogador) {
        HistoricoLoginManager.registrarEntrada(jogador);
        HorarioProibidoManager.marcarEntrada(jogador);
    }

    public static void aoSair(ServerPlayer jogador) {
        HistoricoLoginManager.registrarSaida(jogador);
        MinecraftServer servidor = jogador.getServer();
        if (servidor != null) {
            HorarioProibidoManager.marcarSaida(servidor, jogador);
        }
    }
}
