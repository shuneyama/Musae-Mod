package net.shune.musae.monitor;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;
import java.util.List;

public class HistoricoLoginManager {

    private static Path pasta(MinecraftServer servidor) {
        return ArquivoUtil.pastaDoMundo(servidor, "monitoramento");
    }

    private static Path arquivoDoJogador(MinecraftServer servidor, String nome) {
        return pasta(servidor).resolve(ArquivoUtil.sanitizarNomeArquivo(nome) + ".json");
    }

    public static HistoricoJogador carregar(MinecraftServer servidor, String nome) {
        return ArquivoUtil.lerJson(arquivoDoJogador(servidor, nome), HistoricoJogador.class, null);
    }

    private static void salvar(MinecraftServer servidor, HistoricoJogador historico) {
        ArquivoUtil.escreverJson(arquivoDoJogador(servidor, historico.nome), historico);
    }

    public static String capturarIp(ServerPlayer jogador) {
        try {
            java.net.SocketAddress endereco = obterConexaoBruta(jogador).getRemoteAddress();
            return endereco != null ? endereco.toString()
                    .replaceFirst("^/", "")
                    .replaceFirst(":\\d+$", "") : "desconhecido";
        } catch (Exception e) {
            return "desconhecido";
        }
    }

    private static net.minecraft.network.Connection obterConexaoBruta(ServerPlayer jogador) throws Exception {
        Object listener = jogador.connection;
        Class<?> classeAtual = listener.getClass();

        while (classeAtual != null) {
            for (java.lang.reflect.Field campo : classeAtual.getDeclaredFields()) {
                if (net.minecraft.network.Connection.class.isAssignableFrom(campo.getType())) {
                    campo.setAccessible(true);
                    return (net.minecraft.network.Connection) campo.get(listener);
                }
            }
            classeAtual = classeAtual.getSuperclass();
        }

        throw new IllegalStateException("Não foi possível localizar a conexão do jogador");
    }

    public static SessaoJogador registrarEntrada(ServerPlayer jogador) {
        MinecraftServer servidor = jogador.getServer();
        if (servidor == null) return null;

        String nome = jogador.getGameProfile().getName();
        HistoricoJogador historico = carregar(servidor, nome);
        if (historico == null) {
            historico = new HistoricoJogador(jogador.getUUID().toString(), nome);
        }

        SessaoJogador aberta = historico.sessaoAberta();
        if (aberta != null) {
            aberta.saida = System.currentTimeMillis();
        }

        SessaoJogador sessao = new SessaoJogador(
                System.currentTimeMillis(),
                capturarIp(jogador),
                jogador.getX(), jogador.getY(), jogador.getZ(),
                jogador.level().dimension().location().toString()
        );

        historico.sessoes.add(sessao);
        salvar(servidor, historico);
        return sessao;
    }

    public static void registrarSaida(ServerPlayer jogador) {
        MinecraftServer servidor = jogador.getServer();
        if (servidor == null) return;

        String nome = jogador.getGameProfile().getName();
        HistoricoJogador historico = carregar(servidor, nome);
        if (historico == null) return;

        SessaoJogador aberta = historico.sessaoAberta();
        if (aberta != null) {
            aberta.saida = System.currentTimeMillis();
            aberta.x = jogador.getX();
            aberta.y = jogador.getY();
            aberta.z = jogador.getZ();
            aberta.dimensao = jogador.level().dimension().location().toString();
        }

        salvar(servidor, historico);
    }

    public static long entradaAtual(MinecraftServer servidor, ServerPlayer jogador) {
        HistoricoJogador historico = carregar(servidor, jogador.getGameProfile().getName());
        if (historico == null) return System.currentTimeMillis();

        SessaoJogador aberta = historico.sessaoAberta();
        return aberta != null ? aberta.entrada : System.currentTimeMillis();
    }

    public static long tempoTotalJogado(HistoricoJogador historico) {
        long total = 0;
        for (SessaoJogador sessao : historico.sessoes) {
            total += sessao.duracaoMillis();
        }
        return total;
    }

    public static long mediaPorSessao(HistoricoJogador historico) {
        List<SessaoJogador> sessoes = historico.sessoes;
        if (sessoes.isEmpty()) return 0;
        return tempoTotalJogado(historico) / sessoes.size();
    }

    public static SessaoJogador ultimaSessao(HistoricoJogador historico) {
        List<SessaoJogador> sessoes = historico.sessoes;
        if (sessoes.isEmpty()) return null;
        return sessoes.get(sessoes.size() - 1);
    }
}
