package net.shune.musae.chat;

import net.shune.musae.MusaeMod;

public class BalaoDebug {

    private static volatile boolean ativo = false;

    public static void setAtivo(boolean valor) {
        ativo = valor;
    }

    public static boolean isAtivo() {
        return ativo;
    }

    public static void log(String mensagem) {
        if (ativo) {
            MusaeMod.LOGGER.info("[MusaeBalaoDebug] " + mensagem);
        }
    }
}
