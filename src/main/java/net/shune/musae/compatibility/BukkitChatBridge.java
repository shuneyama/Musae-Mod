package net.shune.musae.compatibility;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.shune.musae.MusaeMod;
import net.shune.musae.auth.AutorizacaoServidor;
import net.shune.musae.chat.ChatEntrada;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public final class BukkitChatBridge {

    private static boolean registrado = false;

    private BukkitChatBridge() {
    }

    public static synchronized void registrar(MinecraftServer servidor) {
        if (registrado) return;
        try {
            Class<?> classeBukkit = Class.forName("org.bukkit.Bukkit");
            Class<?> classeGerenciador = Class.forName("org.bukkit.plugin.PluginManager");
            Class<?> classePlugin = Class.forName("org.bukkit.plugin.Plugin");
            Class<?> classeListener = Class.forName("org.bukkit.event.Listener");
            Class<?> classeExecutor = Class.forName("org.bukkit.plugin.EventExecutor");
            Class<?> classePrioridade = Class.forName("org.bukkit.event.EventPriority");
            Class<?> classeChat = Class.forName("org.bukkit.event.player.AsyncPlayerChatEvent");

            Object gerenciador = classeBukkit.getMethod("getPluginManager").invoke(null);
            if (gerenciador == null) return;

            Object plugin = escolherPlugin(gerenciador, classeGerenciador, classePlugin);
            if (plugin == null) {
                MusaeMod.LOGGER.warn("[MusaeMod] Nenhum plugin Bukkit habilitado para hospedar o gancho de chat.");
                return;
            }

            Object listener = Proxy.newProxyInstance(
                    classeListener.getClassLoader(),
                    new Class<?>[]{classeListener},
                    (proxy, metodo, args) -> tratarObjeto(proxy, metodo, args, "MusaeChatListener"));

            InvocationHandler executorHandler = (proxy, metodo, args) -> {
                if ("execute".equals(metodo.getName()) && args != null && args.length == 2) {
                    aoReceberChat(args[1]);
                    return null;
                }
                return tratarObjeto(proxy, metodo, args, "MusaeChatExecutor");
            };
            Object executor = Proxy.newProxyInstance(
                    classeExecutor.getClassLoader(),
                    new Class<?>[]{classeExecutor},
                    executorHandler);

            @SuppressWarnings({"unchecked", "rawtypes"})
            Object prioridade = Enum.valueOf((Class<Enum>) classePrioridade, "LOWEST");

            Method registrarEvento = classeGerenciador.getMethod("registerEvent",
                    Class.class, classeListener, classePrioridade, classeExecutor, classePlugin);
            registrarEvento.invoke(gerenciador, classeChat, listener, prioridade, executor, plugin);

            registrado = true;
            String nomePlugin = String.valueOf(classePlugin.getMethod("getName").invoke(plugin));
            MusaeMod.LOGGER.info("[MusaeMod] Gancho de chat do Bukkit registrado (hospedado em {}).", nomePlugin);
        } catch (ClassNotFoundException e) {
            MusaeMod.LOGGER.info("[MusaeMod] API Bukkit ausente, gancho de chat do Bukkit nao sera usado.");
        } catch (Throwable t) {
            Throwable causa = t;
            while (causa instanceof java.lang.reflect.InvocationTargetException && causa.getCause() != null) {
                causa = causa.getCause();
            }
            MusaeMod.LOGGER.warn("[MusaeMod] Falha ao registrar gancho de chat do Bukkit: {}: {}",
                    causa.getClass().getName(), causa.getMessage());
        }
    }

    private static Object escolherPlugin(Object gerenciador, Class<?> classeGerenciador, Class<?> classePlugin) throws Exception {
        Method obterPlugins = classeGerenciador.getMethod("getPlugins");
        Method estaHabilitado = classePlugin.getMethod("isEnabled");
        Method obterNome = classePlugin.getMethod("getName");
        Object[] plugins = (Object[]) obterPlugins.invoke(gerenciador);
        Object primeiro = null;
        for (Object candidato : plugins) {
            if (!(Boolean) estaHabilitado.invoke(candidato)) continue;
            if ("LuckPerms".equalsIgnoreCase(String.valueOf(obterNome.invoke(candidato)))) {
                return candidato;
            }
            if (primeiro == null) primeiro = candidato;
        }
        return primeiro;
    }

    private static void aoReceberChat(Object evento) {
        try {
            if (!AutorizacaoServidor.isAutorizado()) return;
            Class<?> classeEvento = evento.getClass();
            Object jogadorBukkit = classeEvento.getMethod("getPlayer").invoke(evento);
            if (jogadorBukkit == null) return;
            Object manipulador = jogadorBukkit.getClass().getMethod("getHandle").invoke(jogadorBukkit);
            if (!(manipulador instanceof ServerPlayer jogador)) return;
            String mensagem = (String) classeEvento.getMethod("getMessage").invoke(evento);
            classeEvento.getMethod("setCancelled", boolean.class).invoke(evento, true);
            ChatEntrada.receber(jogador, mensagem);
        } catch (Throwable t) {
            MusaeMod.LOGGER.warn("[MusaeMod] Erro no gancho de chat do Bukkit: {}", t.toString());
        }
    }

    private static Object tratarObjeto(Object proxy, Method metodo, Object[] args, String nome) {
        return switch (metodo.getName()) {
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> args != null && proxy == args[0];
            case "toString" -> nome;
            default -> null;
        };
    }
}
