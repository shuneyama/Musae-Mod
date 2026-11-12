package net.shune.musae.chat;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AntiSpamManager {

    private static final int COOLDOWN_MS = 5000;

    private static final Map<UUID, Long> ultimoEnvio = new ConcurrentHashMap<>();
    private static final Map<UUID, String> ultimaMensagem = new ConcurrentHashMap<>();

    public static boolean podeEnviar(UUID uuid, String mensagem) {
        long agora = System.currentTimeMillis();

        Long ultimo = ultimoEnvio.get(uuid);
        String anterior = ultimaMensagem.get(uuid);

        if (ultimo != null && anterior != null && anterior.equalsIgnoreCase(mensagem)) {
            if (agora - ultimo < COOLDOWN_MS) {
                return false;
            }
        }

        ultimoEnvio.put(uuid, agora);
        ultimaMensagem.put(uuid, mensagem);
        return true;
    }

    public static void limpar(UUID uuid) {
        ultimoEnvio.remove(uuid);
        ultimaMensagem.remove(uuid);
    }
}
