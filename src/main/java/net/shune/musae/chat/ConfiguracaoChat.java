package net.shune.musae.chat;

import net.minecraft.server.MinecraftServer;
import net.shune.musae.monitor.ArquivoUtil;

import java.nio.file.Path;

public class ConfiguracaoChat {

    public double raioLocal = 50.0;

    private static ConfiguracaoChat instancia;

    private static Path arquivo(MinecraftServer servidor) {
        return ArquivoUtil.pastaDoMundo(servidor).resolve("chat_config.json");
    }

    public static ConfiguracaoChat get(MinecraftServer servidor) {
        if (instancia == null) {
            instancia = ArquivoUtil.lerJson(arquivo(servidor), ConfiguracaoChat.class, new ConfiguracaoChat());
        }
        return instancia;
    }

    public static void salvar(MinecraftServer servidor) {
        if (instancia != null) {
            ArquivoUtil.escreverJson(arquivo(servidor), instancia);
        }
    }

    public static void resetarCache() {
        instancia = null;
    }
}
