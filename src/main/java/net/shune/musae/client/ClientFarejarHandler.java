package net.shune.musae.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientFarejarHandler {

    private static final Map<Integer, Long> EXPIRACAO = new ConcurrentHashMap<>();

    private ClientFarejarHandler() {
    }

    public static void marcar(int[] ids, int duracaoTicks) {
        long fim = System.currentTimeMillis() + duracaoTicks * 50L;
        for (int id : ids) {
            EXPIRACAO.put(id, fim);
        }
    }

    public static boolean brilhando(int id) {
        if (EXPIRACAO.isEmpty()) return false;
        Long fim = EXPIRACAO.get(id);
        if (fim == null) return false;
        if (fim < System.currentTimeMillis()) {
            EXPIRACAO.remove(id);
            return false;
        }
        return true;
    }
}
