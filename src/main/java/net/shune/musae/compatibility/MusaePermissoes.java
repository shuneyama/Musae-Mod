package net.shune.musae.compatibility;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.shune.musae.MusaeMod;

import java.lang.reflect.Method;

public final class MusaePermissoes {

    public static final String NO_ADMIN = "musae.admin";
    public static final String NO_GLOBAL = "musae.global";

    private static boolean falhaRegistrada = false;

    private MusaePermissoes() {
    }

    public static boolean admin(CommandSourceStack fonte) {
        if (fonte.hasPermission(2)) return true;
        return fonte.getEntity() instanceof ServerPlayer jogador && temNo(jogador, NO_ADMIN);
    }

    public static boolean global(CommandSourceStack fonte) {
        return fonte.getEntity() instanceof ServerPlayer jogador && temNo(jogador, NO_GLOBAL);
    }

    public static boolean admin(ServerPlayer jogador) {
        return jogador.hasPermissions(2) || temNo(jogador, NO_ADMIN);
    }

    public static boolean global(ServerPlayer jogador) {
        return temNo(jogador, NO_GLOBAL);
    }

    public static boolean temNo(ServerPlayer jogador, String no) {
        try {
            Method obterEntidade = jogador.getClass().getMethod("getBukkitEntity");
            Object entidadeBukkit = obterEntidade.invoke(jogador);
            if (entidadeBukkit == null) return false;
            Method possui = entidadeBukkit.getClass().getMethod("hasPermission", String.class);
            Object resultado = possui.invoke(entidadeBukkit, no);
            return resultado instanceof Boolean b && b;
        } catch (NoSuchMethodException e) {
            return false;
        } catch (Throwable t) {
            if (!falhaRegistrada) {
                falhaRegistrada = true;
                MusaeMod.LOGGER.warn("[MusaeMod] Falha ao consultar permissao Bukkit/LuckPerms: {}", t.toString());
            }
            return false;
        }
    }
}
