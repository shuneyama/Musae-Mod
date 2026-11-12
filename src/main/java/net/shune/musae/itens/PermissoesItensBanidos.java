package net.shune.musae.itens;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.shune.musae.monitor.ArquivoUtil;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PermissoesItensBanidos {

    private static final Map<UUID, Set<String>> CACHE = new ConcurrentHashMap<>();

    private static Path pasta(MinecraftServer servidor) {
        return ArquivoUtil.pastaDoMundo(servidor, "itens_permitidos");
    }

    private static Path arquivo(MinecraftServer servidor, UUID jogadorUUID) {
        return pasta(servidor).resolve(jogadorUUID + ".json");
    }

    private static Set<String> carregar(MinecraftServer servidor, UUID jogadorUUID) {
        return CACHE.computeIfAbsent(jogadorUUID, id -> {
            ItensPermitidosData dados = ArquivoUtil.lerJson(arquivo(servidor, id), ItensPermitidosData.class, new ItensPermitidosData());
            Set<String> conjunto = ConcurrentHashMap.newKeySet();
            conjunto.addAll(dados.itens);
            return conjunto;
        });
    }

    private static void salvar(MinecraftServer servidor, UUID jogadorUUID) {
        Set<String> itens = CACHE.get(jogadorUUID);
        if (itens == null) return;

        ItensPermitidosData dados = new ItensPermitidosData();
        dados.itens.addAll(itens.stream().sorted().collect(Collectors.toList()));
        ArquivoUtil.escreverJson(arquivo(servidor, jogadorUUID), dados);
    }

    public static void resetarCache() {
        CACHE.clear();
    }

    public static boolean isPermitido(MinecraftServer servidor, UUID jogadorUUID, ResourceLocation itemId) {
        if (servidor == null || jogadorUUID == null || itemId == null) return false;
        return carregar(servidor, jogadorUUID).contains(itemId.toString());
    }

    public static boolean liberar(MinecraftServer servidor, UUID jogadorUUID, ResourceLocation itemId) {
        boolean adicionado = carregar(servidor, jogadorUUID).add(itemId.toString());
        if (adicionado) {
            salvar(servidor, jogadorUUID);
            sincronizarSeOnline(servidor, jogadorUUID);
        }
        return adicionado;
    }

    public static boolean revogar(MinecraftServer servidor, UUID jogadorUUID, ResourceLocation itemId) {
        boolean removido = carregar(servidor, jogadorUUID).remove(itemId.toString());
        if (removido) {
            salvar(servidor, jogadorUUID);
            sincronizarSeOnline(servidor, jogadorUUID);
        }
        return removido;
    }

    private static void sincronizarSeOnline(MinecraftServer servidor, UUID jogadorUUID) {
        net.minecraft.server.level.ServerPlayer jogador = servidor.getPlayerList().getPlayer(jogadorUUID);
        if (jogador != null) {
            sincronizarComJogador(jogador);
        }
    }

    public static void sincronizarComJogador(net.minecraft.server.level.ServerPlayer jogador) {
        if (jogador.getServer() == null) return;
        Set<String> itens = carregar(jogador.getServer(), jogador.getUUID());
        net.shune.musae.network.NetworkHandler.sendToPlayer(jogador,
                new net.shune.musae.network.SyncItensPermitidosPacket(new java.util.ArrayList<>(itens)));
    }

    public static Set<ResourceLocation> getPermitidos(MinecraftServer servidor, UUID jogadorUUID) {
        return carregar(servidor, jogadorUUID).stream()
                .map(ResourceLocation::parse)
                .collect(Collectors.toSet());
    }
}
