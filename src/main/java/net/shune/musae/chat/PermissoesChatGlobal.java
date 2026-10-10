package net.shune.musae.chat;

import net.shune.musae.compatibility.MusaePermissoes;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.shune.musae.monitor.ArquivoUtil;

import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PermissoesChatGlobal {

    private static final Set<String> PERMITIDOS = ConcurrentHashMap.newKeySet();
    private static boolean carregado = false;

    private static Path arquivo(MinecraftServer servidor) {
        return ArquivoUtil.pastaDoMundo(servidor).resolve("chat_permissoes_globais.json");
    }

    private static void carregarSeNecessario(MinecraftServer servidor) {
        if (carregado) return;
        Dados dados = ArquivoUtil.lerJson(arquivo(servidor), Dados.class, new Dados());
        PERMITIDOS.clear();
        PERMITIDOS.addAll(dados.jogadores);
        carregado = true;
    }

    private static void salvar(MinecraftServer servidor) {
        Dados dados = new Dados();
        dados.jogadores.addAll(PERMITIDOS.stream().sorted().collect(Collectors.toList()));
        ArquivoUtil.escreverJson(arquivo(servidor), dados);
    }

    public static void resetarCache() {
        carregado = false;
        PERMITIDOS.clear();
    }

    public static boolean temPermissao(MinecraftServer servidor, UUID jogadorUUID) {
        carregarSeNecessario(servidor);
        return PERMITIDOS.contains(jogadorUUID.toString());
    }

    public static boolean podeUsarGlobal(ServerPlayer player) {
        if (MusaePermissoes.admin(player) || MusaePermissoes.global(player)) return true;
        if (player.getServer() == null) return false;
        return temPermissao(player.getServer(), player.getUUID());
    }

    public static boolean permitir(MinecraftServer servidor, UUID jogadorUUID) {
        carregarSeNecessario(servidor);
        boolean adicionado = PERMITIDOS.add(jogadorUUID.toString());
        if (adicionado) salvar(servidor);
        return adicionado;
    }

    public static boolean revogar(MinecraftServer servidor, UUID jogadorUUID) {
        carregarSeNecessario(servidor);
        boolean removido = PERMITIDOS.remove(jogadorUUID.toString());
        if (removido) salvar(servidor);
        return removido;
    }

    private static class Dados {
        java.util.List<String> jogadores = new java.util.ArrayList<>();
    }
}
