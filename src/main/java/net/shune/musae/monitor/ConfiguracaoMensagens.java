package net.shune.musae.monitor;

import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;

public class ConfiguracaoMensagens {

    public boolean loginAtivo = true;
    public boolean saidaAtivo = true;

    private static ConfiguracaoMensagens instancia;

    private static Path arquivo(MinecraftServer servidor) {
        return ArquivoUtil.pastaDoMundo(servidor).resolve("mensagens_login.json");
    }

    public static ConfiguracaoMensagens get(MinecraftServer servidor) {
        if (instancia == null) {
            instancia = ArquivoUtil.lerJson(arquivo(servidor), ConfiguracaoMensagens.class, new ConfiguracaoMensagens());
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
