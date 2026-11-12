package net.shune.musae.itens;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.shune.musae.monitor.ArquivoUtil;

import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ConfiguracaoItensBanidos {

    private static final Set<String> ITENS_BANIDOS = ConcurrentHashMap.newKeySet();
    private static boolean carregado = false;

    private static Path arquivo(MinecraftServer servidor) {
        return ArquivoUtil.pastaDoMundo(servidor).resolve("itens_banidos.json");
    }

    private static void carregarSeNecessario(MinecraftServer servidor) {
        if (carregado) return;
        ItensBanidosData dados = ArquivoUtil.lerJson(arquivo(servidor), ItensBanidosData.class, new ItensBanidosData());
        ITENS_BANIDOS.clear();
        ITENS_BANIDOS.addAll(dados.itens);
        carregado = true;
    }

    private static void salvar(MinecraftServer servidor) {
        ItensBanidosData dados = new ItensBanidosData();
        dados.itens.addAll(ITENS_BANIDOS.stream().sorted().collect(Collectors.toList()));
        ArquivoUtil.escreverJson(arquivo(servidor), dados);
    }

    public static void resetarCache() {
        carregado = false;
        ITENS_BANIDOS.clear();
    }

    public static boolean isBanido(MinecraftServer servidor, ResourceLocation itemId) {
        carregarSeNecessario(servidor);
        return ITENS_BANIDOS.contains(itemId.toString());
    }

    public static boolean isBanido(ResourceLocation itemId) {
        return ITENS_BANIDOS.contains(itemId.toString());
    }

    public static boolean banir(MinecraftServer servidor, ResourceLocation itemId) {
        carregarSeNecessario(servidor);
        boolean adicionado = ITENS_BANIDOS.add(itemId.toString());
        if (adicionado) {
            salvar(servidor);
            sincronizarComTodos(servidor);
        }
        return adicionado;
    }

    public static boolean desbanir(MinecraftServer servidor, ResourceLocation itemId) {
        carregarSeNecessario(servidor);
        boolean removido = ITENS_BANIDOS.remove(itemId.toString());
        if (removido) {
            salvar(servidor);
            sincronizarComTodos(servidor);
        }
        return removido;
    }

    public static void sincronizarComTodos(MinecraftServer servidor) {
        carregarSeNecessario(servidor);
        net.shune.musae.network.NetworkHandler.sendToAll(
                new net.shune.musae.network.SyncItensBanidosPacket(new java.util.ArrayList<>(ITENS_BANIDOS)));
    }

    public static void sincronizarComJogador(net.minecraft.server.level.ServerPlayer jogador) {
        if (jogador.getServer() == null) return;
        carregarSeNecessario(jogador.getServer());
        net.shune.musae.network.NetworkHandler.sendToPlayer(jogador,
                new net.shune.musae.network.SyncItensBanidosPacket(new java.util.ArrayList<>(ITENS_BANIDOS)));
    }

    public static Set<ResourceLocation> getItensBanidos(MinecraftServer servidor) {
        carregarSeNecessario(servidor);
        return ITENS_BANIDOS.stream()
                .map(ResourceLocation::parse)
                .collect(Collectors.toSet());
    }
}
