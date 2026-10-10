package net.shune.musae.chat;

import net.minecraft.server.MinecraftServer;
import net.shune.musae.monitor.ArquivoUtil;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerBalaoData {

    private static final Map<UUID, PlayerBalaoData> CACHE = new ConcurrentHashMap<>();

    public int corTexto = 0x141414;
    public int corFundo = 0xFFFFFF;
    public int corBorda = 0x000000;
    public float altura = BalaoConfig.BALLOON_HEIGHT_OFFSET;
    public BalaoStyle estilo = BalaoStyle.ROUNDED;

    private static Path pasta(MinecraftServer servidor) {
        return ArquivoUtil.pastaDoMundo(servidor, "balao_config");
    }

    public static PlayerBalaoData get(MinecraftServer servidor, UUID uuid) {
        return CACHE.computeIfAbsent(uuid, id ->
                ArquivoUtil.lerJson(pasta(servidor).resolve(id + ".json"), PlayerBalaoData.class, new PlayerBalaoData()));
    }

    public static void salvar(MinecraftServer servidor, UUID uuid) {
        PlayerBalaoData data = CACHE.get(uuid);
        if (data == null) return;
        ArquivoUtil.escreverJson(pasta(servidor).resolve(uuid + ".json"), data);
    }

    public static void resetarCache() {
        CACHE.clear();
    }
}
