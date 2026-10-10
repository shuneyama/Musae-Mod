package net.shune.musae.compatibility;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.shune.musae.auth.AutorizacaoServidor;
import net.shune.musae.nickname.NicknameData;
import net.shune.musae.nickname.NicknameGradientUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/**
 * Ponte via reflexão pro mod TAB (me.neznamy.tab), sem dependência de compilação.
 * Registra o placeholder nativo do TAB "%musae_nomeformatado%" (nickname com gradiente
 * convertido pra codigos legacy &#RRGGBB, ou o nome real se nao tiver nickname) — o
 * dono do servidor referencia esse placeholder em groups.yml (ex: customtabname), do
 * mesmo jeito que o PlaceholderAPI funciona no plugin TAB.
 */
public class TabModCompat {

    private static final String PLACEHOLDER_IDENTIFIER = "%musae_nomeformatado%";

    private static boolean initialized = false;
    private static boolean available = false;

    private static Class<?> tabPlayerClass;
    private static Method getInstance;
    private static Method getPlayer;
    private static Method tabPlayerGetUniqueId;
    private static Method placeholderUpdateValue;
    private static Method eventBusRegister;
    private static Class<?> tabLoadEventClass;

    private static Object placeholder;
    private static boolean reloadListenerRegistered = false;

    private TabModCompat() {
    }

    /**
     * Registra o placeholder no TAB o mais cedo possivel (ServerAboutToStartEvent), antes
     * de qualquer jogador entrar — o TAB valida/faz cache dos placeholders usados no
     * groups.yml no proprio carregamento, entao registrar reativamente no primeiro
     * join e' tarde demais (TAB ja mostra o texto literal do placeholder desconhecido).
     * Nao depende de AutorizacaoServidor: o registro em si e' so infraestrutura, quem
     * respeita a trava e' o VALOR calculado em nomeFormatado(uuid).
     */
    public static void ensureInitialized() {
        if (!initialized) init();
    }

    private static void init() {
        try {
            Class<?> tabApiClass = Class.forName("me.neznamy.tab.api.TabAPI");
            tabPlayerClass = Class.forName("me.neznamy.tab.api.TabPlayer");
            Class<?> placeholderManagerClass = Class.forName("me.neznamy.tab.api.placeholder.PlaceholderManager");
            Class<?> playerPlaceholderClass = Class.forName("me.neznamy.tab.api.placeholder.PlayerPlaceholder");

            getInstance = tabApiClass.getMethod("getInstance");
            getPlayer = tabApiClass.getMethod("getPlayer", UUID.class);
            Method getPlaceholderManager = tabApiClass.getMethod("getPlaceholderManager");
            Method registerPlayerPlaceholder = placeholderManagerClass.getMethod(
                    "registerPlayerPlaceholder", String.class, int.class, Function.class);
            tabPlayerGetUniqueId = tabPlayerClass.getMethod("getUniqueId");
            placeholderUpdateValue = playerPlaceholderClass.getMethod("updateValue", tabPlayerClass, String.class);

            Class<?> eventBusClass = Class.forName("me.neznamy.tab.api.event.EventBus");
            Class<?> eventHandlerClass = Class.forName("me.neznamy.tab.api.event.EventHandler");
            tabLoadEventClass = Class.forName("me.neznamy.tab.api.event.plugin.TabLoadEvent");
            eventBusRegister = eventBusClass.getMethod("register", Class.class, eventHandlerClass);

            Object api = getInstance.invoke(null);
            Object placeholderManager = getPlaceholderManager.invoke(api);

            InvocationHandler handler = (proxy, method, args) -> {
                if ("apply".equals(method.getName()) && args != null && args.length == 1) {
                    return resolverNomeFormatado(args[0]);
                }
                return switch (method.getName()) {
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == (args != null ? args[0] : null);
                    case "toString" -> "TabModCompat$NomeFormatadoPlaceholder";
                    default -> null;
                };
            };

            Object resolverProxy = Proxy.newProxyInstance(
                    TabModCompat.class.getClassLoader(),
                    new Class<?>[]{Function.class},
                    handler
            );

            placeholder = registerPlayerPlaceholder.invoke(placeholderManager, PLACEHOLDER_IDENTIFIER, 1000, resolverProxy);

            registrarListenerDeReload(api);

            available = true;
            initialized = true;
            org.slf4j.LoggerFactory.getLogger("musae-tab")
                    .info("[Musae] Placeholder {} registrado no TAB com sucesso.", PLACEHOLDER_IDENTIFIER);
        } catch (Throwable t) {
            // TAB ainda nao terminou de inicializar (TabAPI.getInstance() == null) ou nao
            // esta presente. NAO marca initialized: a proxima chamada tenta de novo.
            Throwable causa = t;
            while (causa instanceof java.lang.reflect.InvocationTargetException && causa.getCause() != null) {
                causa = causa.getCause();
            }
            org.slf4j.LoggerFactory.getLogger("musae-tab")
                    .warn("[Musae] Falha ao registrar {} no TAB (tentara de novo): {}: {}",
                            PLACEHOLDER_IDENTIFIER, causa.getClass().getName(), causa.getMessage());
            for (StackTraceElement el : causa.getStackTrace()) {
                if (el.getClassName().startsWith("me.neznamy")) {
                    org.slf4j.LoggerFactory.getLogger("musae-tab")
                            .warn("[Musae]   em {}", el);
                }
            }
        }
    }

    /**
     * O /tab reload recarrega groups.yml e re-registra os placeholders usados, fazendo o
     * %musae_nomeformatado% voltar a ser tratado como desconhecido (valor congelado no
     * literal). Escutamos o TabLoadEvent (disparado no load E no reload) pra re-registrar
     * o placeholder e reempurrar os valores de todos os jogadores online.
     */
    private static void registrarListenerDeReload(Object api) {
        if (reloadListenerRegistered) return;

        try {
            InvocationHandler handler = (proxy, method, args) -> {
                if ("handle".equals(method.getName())) {
                    onTabReload();
                }
                return switch (method.getName()) {
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == (args != null ? args[0] : null);
                    case "toString" -> "TabModCompat$ReloadListener";
                    default -> null;
                };
            };

            Object listener = Proxy.newProxyInstance(
                    TabModCompat.class.getClassLoader(),
                    new Class<?>[]{Class.forName("me.neznamy.tab.api.event.EventHandler")},
                    handler
            );

            Method getEventBus = api.getClass().getMethod("getEventBus");
            Object eventBus = getEventBus.invoke(api);
            eventBusRegister.invoke(eventBus, tabLoadEventClass, listener);

            reloadListenerRegistered = true;
        } catch (Throwable t) {
            org.slf4j.LoggerFactory.getLogger("musae-tab")
                    .warn("[Musae] Nao foi possivel escutar o reload do TAB: {}", t.toString());
        }
    }

    private static void onTabReload() {
        try {
            Object api = getInstance.invoke(null);
            Object placeholderManager = api.getClass().getMethod("getPlaceholderManager").invoke(api);

            // O reload pode ter descartado o placeholder antigo; re-registra pra garantir.
            Method registerPlayerPlaceholder = Class.forName("me.neznamy.tab.api.placeholder.PlaceholderManager")
                    .getMethod("registerPlayerPlaceholder", String.class, int.class, Function.class);

            InvocationHandler handler = (proxy, method, args) -> {
                if ("apply".equals(method.getName()) && args != null && args.length == 1) {
                    return resolverNomeFormatado(args[0]);
                }
                return switch (method.getName()) {
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == (args != null ? args[0] : null);
                    case "toString" -> "TabModCompat$NomeFormatadoPlaceholder";
                    default -> null;
                };
            };

            Object resolverProxy = Proxy.newProxyInstance(
                    TabModCompat.class.getClassLoader(),
                    new Class<?>[]{Function.class},
                    handler
            );

            placeholder = registerPlayerPlaceholder.invoke(placeholderManager, PLACEHOLDER_IDENTIFIER, 1000, resolverProxy);

            // Reempurra o valor de todo mundo que esta online, senao a tablist fica no literal.
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                for (ServerPlayer online : server.getPlayerList().getPlayers()) {
                    try {
                        Object tabPlayer = getPlayer.invoke(api, online.getUUID());
                        if (tabPlayer != null) {
                            placeholderUpdateValue.invoke(placeholder, tabPlayer, nomeFormatado(online.getUUID()));
                        }
                    } catch (Exception ignored) {
                    }
                }
            }

            org.slf4j.LoggerFactory.getLogger("musae-tab")
                    .info("[Musae] Placeholder {} re-registrado apos reload do TAB.", PLACEHOLDER_IDENTIFIER);
        } catch (Throwable t) {
            org.slf4j.LoggerFactory.getLogger("musae-tab")
                    .warn("[Musae] Falha ao re-registrar {} apos reload do TAB: {}", PLACEHOLDER_IDENTIFIER, t.toString());
        }
    }

    private static String resolverNomeFormatado(Object tabPlayer) {
        try {
            UUID uuid = (UUID) tabPlayerGetUniqueId.invoke(tabPlayer);
            return nomeFormatado(uuid);
        } catch (Exception e) {
            return "";
        }
    }

    private static String nomeFormatado(UUID uuid) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return "";

        if (AutorizacaoServidor.isAutorizado()) {
            NicknameData data = NicknameData.get(server);
            NicknameData.NicknameEntry entry = data.getNickname(uuid);
            if (entry != null) return buildLegacyGradient(entry);
        }

        ServerPlayer online = server.getPlayerList().getPlayer(uuid);
        return online != null ? online.getGameProfile().getName() : "";
    }

    /**
     * Chame sempre que o nickname de um jogador mudar (definido, colorido ou removido)
     * pra forçar o TAB a recalcular o placeholder imediatamente, sem esperar o refresh.
     *
     * Importante: NAO basta chamar PlayerPlaceholder.update(player). O TAB so recalcula
     * a tablist quando o valor resolvido do placeholder muda (Property.update() == true),
     * e o valor so muda se alguem empurrar um valor novo. Como o TAB nao tem como saber
     * o nickname sozinho, empurramos o valor calculado via updateValue(player, valor).
     */
    public static void syncToTab(ServerPlayer player, NicknameData data) {
        ensureInitialized();
        if (!available) return;
        if (!AutorizacaoServidor.isAutorizado()) return;

        try {
            Object api = getInstance.invoke(null);
            Object tabPlayer = getPlayer.invoke(api, player.getUUID());
            if (tabPlayer == null) return;

            String valor = nomeFormatado(player.getUUID());
            placeholderUpdateValue.invoke(placeholder, tabPlayer, valor);
        } catch (Exception ignored) {
        }
    }

    private static String buildLegacyGradient(NicknameData.NicknameEntry entry) {
        String nickname = entry.nickname();
        if (entry.colors().isEmpty()) {
            return nickname;
        }

        List<Integer> colors = NicknameGradientUtil.parseColors(entry.colors());
        if (colors.size() == 1) {
            return "&#" + hex(colors.get(0)) + nickname;
        }

        StringBuilder sb = new StringBuilder();
        int length = nickname.length();
        for (int i = 0; i < length; i++) {
            float position = length == 1 ? 0f : (float) i / (length - 1);
            float segmentLength = 1.0f / (colors.size() - 1);
            int segmentIndex = Math.min((int) (position / segmentLength), colors.size() - 2);
            float segmentRatio = (position - segmentIndex * segmentLength) / segmentLength;
            int color = NicknameGradientUtil.interpolate(colors.get(segmentIndex), colors.get(segmentIndex + 1), segmentRatio);
            sb.append("&#").append(hex(color)).append(nickname.charAt(i));
        }
        return sb.toString();
    }

    private static String hex(int color) {
        return String.format("%06X", color & 0xFFFFFF);
    }
}
