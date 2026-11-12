package net.shune.musae.compatibility;

public final class JadeClientState {

    private static volatile boolean podeVerNicks = false;

    private JadeClientState() {
    }

    public static boolean podeVerNicks() {
        return podeVerNicks;
    }

    public static void definir(boolean valor) {
        podeVerNicks = valor;
    }
}
