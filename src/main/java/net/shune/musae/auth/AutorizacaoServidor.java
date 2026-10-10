package net.shune.musae.auth;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.shune.musae.MusaeMod;
import net.shune.musae.network.NetworkHandler;
import net.shune.musae.network.SyncAutorizacaoPacket;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class AutorizacaoServidor {

    public static final String IP_ESPERADO = "198.89.99.136";
    public static final int PORTA_ESPERADA = 25757;

    public static final boolean MODO_DESENVOLVIMENTO = false;

    private static volatile boolean autorizado = false;

    private AutorizacaoServidor() {
    }

    public static void verificar(MinecraftServer servidor) {
        if (MODO_DESENVOLVIMENTO) {
            autorizado = true;
            return;
        }

        if (!servidor.isDedicatedServer()) {
            autorizado = false;
            return;
        }

        if (servidor.getPort() != PORTA_ESPERADA) {
            autorizado = false;
            MusaeMod.LOGGER.info("[Debug] Porta (" + servidor.getPort() + ")");
            return;
        }

        Thread verificacao = new Thread(() -> verificarIpPublico(servidor), "musae-autorizacao-servidor");
        verificacao.setDaemon(true);
        verificacao.start();
    }

    private static void verificarIpPublico(MinecraftServer servidor) {
        String ipDetectado;
        try {
            ipDetectado = consultarIpPublico();
        } catch (Exception e) {
            autorizado = false;
            MusaeMod.LOGGER.info("[Debug] Falha (" + e.getClass().getSimpleName() + ": " + e.getMessage() + ")");
            return;
        }

        if (ipDetectado != null && ipDetectado.equals(IP_ESPERADO)) {
            autorizado = true;
            sincronizarComTodos(servidor);
        } else {
            autorizado = false;
            MusaeMod.LOGGER.info("[Debug] Publico (" + ipDetectado + ")");
        }
    }

    private static String consultarIpPublico() throws Exception {
        URL url = new URL("https://api.ipify.org");
        HttpURLConnection conexao = (HttpURLConnection) url.openConnection();
        conexao.setConnectTimeout(5000);
        conexao.setReadTimeout(5000);
        conexao.setRequestMethod("GET");

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conexao.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder resposta = new StringBuilder();
            String linha;
            while ((linha = reader.readLine()) != null) {
                resposta.append(linha.trim());
            }
            return resposta.toString();
        } finally {
            conexao.disconnect();
        }
    }

    private static void sincronizarComTodos(MinecraftServer servidor) {
        SyncAutorizacaoPacket pacote = new SyncAutorizacaoPacket(true);
        for (ServerPlayer jogador : servidor.getPlayerList().getPlayers()) {
            NetworkHandler.sendToPlayer(jogador, pacote);
        }
    }

    public static void aplicarEstadoRecebidoDoServidor(boolean valor) {
        autorizado = valor;
    }

    public static boolean isAutorizado() {
        return autorizado;
    }
}
