package net.shune.musae.monitor;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class HorarioProibidoManager {

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final Map<String, Long> ENTRADA_ATUAL = new HashMap<>();

    private static Path arquivoDoJogador(MinecraftServer servidor, String nome) {
        return ArquivoUtil.pastaDoMundo(servidor, "horario_proibido").resolve(ArquivoUtil.sanitizarNomeArquivo(nome) + ".txt");
    }

    public static void marcarEntrada(ServerPlayer jogador) {
        ENTRADA_ATUAL.put(jogador.getGameProfile().getName(), System.currentTimeMillis());
    }

    public static void marcarSaida(MinecraftServer servidor, ServerPlayer jogador) {
        String nome = jogador.getGameProfile().getName();
        ConfiguracaoHorarioProibido config = ConfiguracaoHorarioProibido.get(servidor);
        if (config.dentroDoHorario()) {
            registrarTopico(servidor, jogador, true);
        }
        ENTRADA_ATUAL.remove(nome);
    }

    public static void registrarTopico(MinecraftServer servidor, ServerPlayer jogador, boolean deslogando) {
        String nome = jogador.getGameProfile().getName();
        Long entrada = ENTRADA_ATUAL.get(nome);
        if (entrada == null) {
            entrada = System.currentTimeMillis();
            ENTRADA_ATUAL.put(nome, entrada);
        }

        ZonedDateTime agora = ZonedDateTime.now(ConfiguracaoHorarioProibido.FUSO_BRASILIA);
        ZonedDateTime horarioEntrada = ZonedDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(entrada), ConfiguracaoHorarioProibido.FUSO_BRASILIA);

        String linha = String.format(
                "[%s] UUID: %s | Coordenada: %.1f, %.1f, %.1f (%s) | Login: %s | Logout: %s",
                agora.format(FORMATO_HORA),
                jogador.getUUID(),
                jogador.getX(), jogador.getY(), jogador.getZ(),
                jogador.level().dimension().location(),
                horarioEntrada.format(FORMATO_HORA),
                deslogando ? agora.format(FORMATO_HORA) : "--"
        );

        ArquivoUtil.anexarTexto(arquivoDoJogador(servidor, nome), linha);
    }

    public static void tick(MinecraftServer servidor) {
        ConfiguracaoHorarioProibido config = ConfiguracaoHorarioProibido.get(servidor);
        if (!config.dentroDoHorario()) return;

        for (ServerPlayer jogador : servidor.getPlayerList().getPlayers()) {
            registrarTopico(servidor, jogador, false);
        }
    }
}
