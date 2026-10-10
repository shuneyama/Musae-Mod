package net.shune.musae.chat;

import net.minecraft.server.MinecraftServer;
import net.shune.musae.monitor.ArquivoUtil;

import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ConfiguracaoBalao {

    private static final Set<String> DESATIVADOS = ConcurrentHashMap.newKeySet();
    private static boolean carregado = false;

    private static Path arquivo(MinecraftServer servidor) {
        return ArquivoUtil.pastaDoMundo(servidor).resolve("balao_preferencias.json");
    }

    private static void carregarSeNecessario(MinecraftServer servidor) {
        if (carregado) return;
        BalaoPreferenciasData dados = ArquivoUtil.lerJson(arquivo(servidor), BalaoPreferenciasData.class, new BalaoPreferenciasData());
        DESATIVADOS.clear();
        DESATIVADOS.addAll(dados.desativados);
        carregado = true;
    }

    private static void salvar(MinecraftServer servidor) {
        BalaoPreferenciasData dados = new BalaoPreferenciasData();
        dados.desativados.addAll(DESATIVADOS);
        ArquivoUtil.escreverJson(arquivo(servidor), dados);
    }

    public static void resetarCache() {
        carregado = false;
        DESATIVADOS.clear();
    }

    public static boolean isAtivo(MinecraftServer servidor, UUID jogadorUUID) {
        carregarSeNecessario(servidor);
        return !DESATIVADOS.contains(jogadorUUID.toString());
    }

    public static void ativar(MinecraftServer servidor, UUID jogadorUUID) {
        carregarSeNecessario(servidor);
        if (DESATIVADOS.remove(jogadorUUID.toString())) {
            salvar(servidor);
        }
    }

    public static void desativar(MinecraftServer servidor, UUID jogadorUUID) {
        carregarSeNecessario(servidor);
        if (DESATIVADOS.add(jogadorUUID.toString())) {
            salvar(servidor);
        }
    }

    public static class BalaoPreferenciasData {
        public Set<String> desativados = ConcurrentHashMap.newKeySet();
    }
}
