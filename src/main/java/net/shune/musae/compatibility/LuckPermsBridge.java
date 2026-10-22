package net.shune.musae.compatibility;

import net.shune.musae.MusaeMod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.function.Predicate;

public final class LuckPermsBridge {

    private static boolean falhaRegistrada = false;

    private LuckPermsBridge() {
    }

    public static boolean disponivel() {
        return carregarApi() != null;
    }

    public static boolean permitir(UUID jogadorUUID, String no) {
        return definirPermissao(jogadorUUID, no, true);
    }

    public static boolean revogar(UUID jogadorUUID, String no) {
        return definirPermissao(jogadorUUID, no, false);
    }

    private static boolean definirPermissao(UUID jogadorUUID, String no, boolean valor) {
        Api api = carregarApi();
        if (api == null) return false;
        try {
            Class<?> classeGerenciador = api.classe("net.luckperms.api.model.user.UserManager");
            Object gerenciador = api.classe("net.luckperms.api.LuckPerms").getMethod("getUserManager").invoke(api.instancia());
            Object usuario = classeGerenciador.getMethod("getUser", UUID.class).invoke(gerenciador, jogadorUUID);
            if (usuario == null) return false;

            if (temPermissaoEfetiva(api, usuario, no) == valor) return false;

            Class<?> classeNo = api.classe("net.luckperms.api.node.Node");
            Class<?> classeMapa = api.classe("net.luckperms.api.model.data.NodeMap");
            Object dados = api.classe("net.luckperms.api.model.PermissionHolder").getMethod("data").invoke(usuario);

            Method obterChave = classeNo.getMethod("getKey");
            Predicate<Object> mesmaChave = noExistente -> {
                try {
                    return no.equalsIgnoreCase((String) obterChave.invoke(noExistente));
                } catch (ReflectiveOperationException e) {
                    return false;
                }
            };
            classeMapa.getMethod("clear", Predicate.class).invoke(dados, mesmaChave);

            if (temPermissaoEfetiva(api, usuario, no) != valor) {
                Class<?> classeConstrutor = api.classe("net.luckperms.api.node.NodeBuilder");
                Object construtor = classeNo.getMethod("builder", String.class).invoke(null, no);
                construtor = classeConstrutor.getMethod("value", boolean.class).invoke(construtor, valor);
                Object novoNo = classeConstrutor.getMethod("build").invoke(construtor);
                classeMapa.getMethod("add", classeNo).invoke(dados, novoNo);
            }

            classeGerenciador.getMethod("saveUser", api.classe("net.luckperms.api.model.user.User"))
                    .invoke(gerenciador, usuario);
            return true;
        } catch (Throwable t) {
            registrarFalha(t);
            return false;
        }
    }

    private static boolean temPermissaoEfetiva(Api api, Object usuario, String no) throws ReflectiveOperationException {
        Object cache = api.classe("net.luckperms.api.model.PermissionHolder").getMethod("getCachedData").invoke(usuario);
        Object permissoes = api.classe("net.luckperms.api.cacheddata.CachedDataManager").getMethod("getPermissionData").invoke(cache);
        Object resultado = api.classe("net.luckperms.api.cacheddata.CachedPermissionData")
                .getMethod("checkPermission", String.class).invoke(permissoes, no);
        return (Boolean) api.classe("net.luckperms.api.util.Tristate").getMethod("asBoolean").invoke(resultado);
    }

    private static Api carregarApi() {
        try {
            Object gerenciador = Class.forName("org.bukkit.Bukkit").getMethod("getPluginManager").invoke(null);
            if (gerenciador == null) return null;
            Object plugin = Class.forName("org.bukkit.plugin.PluginManager")
                    .getMethod("getPlugin", String.class).invoke(gerenciador, "LuckPerms");
            if (plugin == null) return null;
            if (!(Boolean) Class.forName("org.bukkit.plugin.Plugin").getMethod("isEnabled").invoke(plugin)) return null;

            ClassLoader carregador = plugin.getClass().getClassLoader();
            Object instancia = Class.forName("net.luckperms.api.LuckPermsProvider", true, carregador)
                    .getMethod("get").invoke(null);
            return new Api(carregador, instancia);
        } catch (ClassNotFoundException e) {
            return null;
        } catch (Throwable t) {
            registrarFalha(t);
            return null;
        }
    }

    private static void registrarFalha(Throwable t) {
        if (falhaRegistrada) return;
        falhaRegistrada = true;
        Throwable causa = t;
        while (causa instanceof InvocationTargetException && causa.getCause() != null) {
            causa = causa.getCause();
        }
        MusaeMod.LOGGER.warn("[MusaeMod] Falha ao acessar a API do LuckPerms: {}: {}",
                causa.getClass().getName(), causa.getMessage());
    }

    private record Api(ClassLoader carregador, Object instancia) {
        Class<?> classe(String nome) throws ClassNotFoundException {
            return Class.forName(nome, false, carregador);
        }
    }
}
