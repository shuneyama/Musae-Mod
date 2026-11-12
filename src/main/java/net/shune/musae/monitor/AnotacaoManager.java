package net.shune.musae.monitor;

import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;

public class AnotacaoManager {

    private static Path arquivoDoJogador(MinecraftServer servidor, String nome) {
        return ArquivoUtil.pastaDoMundo(servidor, "anotacoes").resolve(ArquivoUtil.sanitizarNomeArquivo(nome) + ".json");
    }

    public static ListaAnotacoes carregar(MinecraftServer servidor, String nome) {
        return ArquivoUtil.lerJson(arquivoDoJogador(servidor, nome), ListaAnotacoes.class, new ListaAnotacoes());
    }

    private static void salvar(MinecraftServer servidor, String nome, ListaAnotacoes lista) {
        ArquivoUtil.escreverJson(arquivoDoJogador(servidor, nome), lista);
    }

    public static Anotacao adicionar(MinecraftServer servidor, String uuidJogador, String nomeJogador,
                                      TipoAnotacao tipo, String responsavel, String comentario) {
        ListaAnotacoes lista = carregar(servidor, nomeJogador);
        Anotacao anotacao = new Anotacao(uuidJogador, nomeJogador, tipo.getId(), responsavel, comentario, System.currentTimeMillis());
        lista.anotacoes.add(anotacao);
        salvar(servidor, nomeJogador, lista);
        return anotacao;
    }

    public static boolean remover(MinecraftServer servidor, String nomeJogador, int indice) {
        ListaAnotacoes lista = carregar(servidor, nomeJogador);
        if (indice < 0 || indice >= lista.anotacoes.size()) return false;

        lista.anotacoes.remove(indice);
        salvar(servidor, nomeJogador, lista);
        return true;
    }
}
