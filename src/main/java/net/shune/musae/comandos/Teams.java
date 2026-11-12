package net.shune.musae.comandos;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Teams {
    private Teams() {
    }

    public static final String KEY = "musae_equipes";
    public static final String[] NOMES = {"npc", "staff", "mod"};
    public static final int CHAT_MAX = 500;
    public static final int CHAT_VISIVEL = 10;

    public static CompoundTag root(MinecraftServer server, String equipe) {
        CompoundTag r = U.root(server, KEY);
        if (!r.contains(equipe, 10)) r.put(equipe, new CompoundTag());
        return r.getCompound(equipe);
    }

    public static boolean in(MinecraftServer server, String equipe, String uuid) {
        return root(server, equipe).contains(uuid);
    }

    public static List<ServerPlayer> online(MinecraftServer server, String[] equipes, String excluirUuid) {
        List<ServerPlayer> lista = new ArrayList<>();
        for (ServerPlayer p : U.players(server)) {
            String u = p.getStringUUID();
            if (u.equals(excluirUuid)) continue;
            for (String e : equipes) {
                if (in(server, e, u)) {
                    lista.add(p);
                    break;
                }
            }
        }
        return lista;
    }

    public static List<ServerPlayer> staffOnlineGeral(MinecraftServer server, String excluirUuid) {
        List<ServerPlayer> lista = new ArrayList<>();
        for (ServerPlayer p : U.players(server)) {
            String u = p.getStringUUID();
            if (u.equals(excluirUuid)) continue;
            if (U.ehOp(p) || in(server, "staff", u) || in(server, "mod", u)) lista.add(p);
        }
        return lista;
    }

    public static String equipeDe(MinecraftServer server, String uuid) {
        for (String e : new String[]{"staff", "mod", "npc"}) {
            if (in(server, e, uuid)) return e;
        }
        return null;
    }

    public static boolean permitido(CommandSourceStack s) {
        try {
            if (s.hasPermission(2)) return true;
            ServerPlayer p = U.player(s);
            if (p == null) return false;
            String e = equipeDe(s.getServer(), p.getStringUUID());
            return "staff".equals(e) || "mod".equals(e);
        } catch (Exception ex) {
            return false;
        }
    }

    public static final class Msg {
        public String data = "";
        public String autor = "";
        public String texto = "";
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, List<Msg>> cache = new HashMap<>();

    public static void limparCache() {
        cache.clear();
    }

    private static Path arquivo(String equipe) {
        return FMLPaths.GAMEDIR.get().resolve("musae_equipes").resolve("chat_" + equipe + ".json");
    }

    private static String str(JsonObject o, String k) {
        return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString() : "";
    }

    private static List<Msg> lista(String equipe) {
        List<Msg> l = cache.get(equipe);
        if (l != null) return l;

        l = new ArrayList<>();
        Path f = arquivo(equipe);
        if (Files.exists(f)) {
            try (Reader r = Files.newBufferedReader(f, StandardCharsets.UTF_8)) {
                JsonObject obj = GSON.fromJson(r, JsonObject.class);
                if (obj != null && obj.has("mensagens")) {
                    for (JsonElement el : obj.getAsJsonArray("mensagens")) {
                        JsonObject m = el.getAsJsonObject();
                        Msg msg = new Msg();
                        msg.data = str(m, "data");
                        msg.autor = str(m, "autor");
                        msg.texto = str(m, "texto");
                        l.add(msg);
                    }
                }
            } catch (Exception e) {
                net.shune.musae.MusaeMod.LOGGER.error("[musae] nao foi possivel ler " + f, e);
            }
        }
        cache.put(equipe, l);
        return l;
    }

    private static void salvar(String equipe) {
        Path f = arquivo(equipe);
        try {
            JsonObject obj = new JsonObject();
            obj.addProperty("equipe", equipe);
            JsonArray arr = new JsonArray();
            for (Msg m : lista(equipe)) {
                JsonObject o = new JsonObject();
                o.addProperty("data", m.data);
                o.addProperty("autor", m.autor);
                o.addProperty("texto", m.texto);
                arr.add(o);
            }
            obj.add("mensagens", arr);
            Files.createDirectories(f.getParent());
            Files.writeString(f, GSON.toJson(obj), StandardCharsets.UTF_8);
        } catch (Exception e) {
            net.shune.musae.MusaeMod.LOGGER.error("[musae] nao foi possivel salvar " + f, e);
        }
    }

    private static void chatAdd(String equipe, String autor, String texto) {
        List<Msg> l = lista(equipe);
        Msg m = new Msg();
        m.data = U.agora();
        m.autor = autor;
        m.texto = texto;
        l.add(m);
        while (l.size() > CHAT_MAX) l.remove(0);
        salvar(equipe);
    }

    private static void mostrarChat(CommandSourceStack src, String equipe) {
        List<Msg> l = lista(equipe);
        U.reply(src, U.gold("===== Chat da equipe " + equipe.toUpperCase() + " ====="));
        if (l.isEmpty()) {
            U.reply(src, U.gray("Nenhuma mensagem ainda."));
        } else {
            int ini = Math.max(0, l.size() - CHAT_VISIVEL);
            for (int i = ini; i < l.size(); i++) {
                Msg m = l.get(i);
                String hora = m.data.length() >= 16 ? m.data.substring(11, 16) : m.data;
                U.reply(src, U.gray("[" + hora + "] ")
                        .append(U.aqua(m.autor + ": "))
                        .append(U.white(m.texto)));
            }
        }
        U.reply(src, U.gray("Enviar: /musae equipe " + equipe + " mensagem \"texto\""));
    }

    public static boolean podeMensagem(CommandSourceStack s, String equipe) {
        try {
            if (s.hasPermission(2)) return true;
            ServerPlayer p = U.player(s);
            return p != null && in(s.getServer(), equipe, p.getStringUUID());
        } catch (Exception ex) {
            return false;
        }
    }

    public static int chatVer(CommandContext<CommandSourceStack> ctx, String equipe) {
        CommandSourceStack src = ctx.getSource();
        if (!podeMensagem(src, equipe)) {
            U.reply(src, U.red("Você não faz parte da equipe " + equipe.toUpperCase() + "."));
            return 0;
        }
        mostrarChat(src, equipe);
        return 1;
    }

    public static int chatEnviar(CommandContext<CommandSourceStack> ctx, String equipe) {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer p = U.player(src);
        if (p == null) {
            U.reply(src, U.red("Esse comando só pode ser usado por jogadores."));
            return 0;
        }
        if (!podeMensagem(src, equipe)) {
            U.reply(src, U.red("Você não faz parte da equipe " + equipe.toUpperCase() + "."));
            return 0;
        }
        enviarParaEquipe(ctx, p, equipe);
        return 1;
    }

    private static void enviarParaEquipe(CommandContext<CommandSourceStack> ctx, ServerPlayer p, String equipe) {
        CommandSourceStack src = ctx.getSource();
        String texto = StringArgumentType.getString(ctx, "texto");
        chatAdd(equipe, U.nome(p), texto);

        var msg = U.gold("[Equipe " + equipe.toUpperCase() + "] ")
                .append(U.aqua(U.nome(p) + ": "))
                .append(U.white(texto));
        for (ServerPlayer d : online(src.getServer(), new String[]{equipe}, p.getStringUUID())) {
            U.tell(d, msg);
        }
        mostrarChat(src, equipe);
    }

    public static int chatVer(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer p = U.player(src);
        if (p == null) {
            U.reply(src, U.red("Esse comando só pode ser usado por jogadores."));
            return 0;
        }
        String equipe = equipeDe(src.getServer(), p.getStringUUID());
        if (equipe == null) {
            U.reply(src, U.red("Você não faz parte de nenhuma equipe."));
            return 0;
        }
        mostrarChat(src, equipe);
        return 1;
    }

    public static int chatEnviar(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer p = U.player(src);
        if (p == null) {
            U.reply(src, U.red("Esse comando só pode ser usado por jogadores."));
            return 0;
        }
        String uuid = p.getStringUUID();
        String equipe = equipeDe(src.getServer(), uuid);
        if (equipe == null) {
            U.reply(src, U.red("Você não faz parte de nenhuma equipe."));
            return 0;
        }

        enviarParaEquipe(ctx, p, equipe);
        return 1;
    }

    private static GameProfile perfil(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return GameProfileArgument.getGameProfiles(ctx, "player").iterator().next();
    }

    public static int adicionar(CommandContext<CommandSourceStack> ctx, String equipe) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        MinecraftServer server = src.getServer();
        GameProfile profile = perfil(ctx);
        CompoundTag membros = root(server, equipe);
        String uuid = profile.getId().toString();

        if (membros.contains(uuid)) {
            U.reply(src, U.red(profile.getName() + " já está na equipe " + equipe.toUpperCase() + "."));
            return 0;
        }
        membros.putString(uuid, profile.getName());
        Logs.acao(server, src.getTextName(), "Membro adicionado à equipe " + equipe.toUpperCase(), profile.getName());
        U.reply(src, U.green(profile.getName() + " foi adicionado à equipe " + equipe.toUpperCase() + "."));
        atualizarComandos(server, uuid);
        return 1;
    }

    public static int remover(CommandContext<CommandSourceStack> ctx, String equipe) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        MinecraftServer server = src.getServer();
        GameProfile profile = perfil(ctx);
        CompoundTag membros = root(server, equipe);
        String uuid = profile.getId().toString();

        if (!membros.contains(uuid)) {
            U.reply(src, U.red(profile.getName() + " não está na equipe " + equipe.toUpperCase() + "."));
            return 0;
        }
        membros.remove(uuid);
        Logs.acao(server, src.getTextName(), "Membro removido da equipe " + equipe.toUpperCase(), profile.getName());
        U.reply(src, U.green(profile.getName() + " foi removido da equipe " + equipe.toUpperCase() + "."));
        atualizarComandos(server, uuid);
        return 1;
    }

    private static void atualizarComandos(MinecraftServer server, String uuid) {
        ServerPlayer p = U.player(server, uuid);
        if (p != null) {
            server.getCommands().sendCommands(p);
        }
    }

    public static int ver(CommandContext<CommandSourceStack> ctx, String equipe) {
        CommandSourceStack src = ctx.getSource();
        MinecraftServer server = src.getServer();
        CompoundTag membros = root(server, equipe);

        if (membros.size() == 0) {
            U.reply(src, U.red("A equipe " + equipe.toUpperCase() + " não possui membros."));
            return 0;
        }

        Map<String, String> on = new HashMap<>();
        for (ServerPlayer p : U.players(server)) on.put(p.getStringUUID(), U.nome(p));

        U.reply(src, U.gold("===== Equipe " + equipe.toUpperCase() + " (" + membros.size() + ") ====="));
        for (String uuid : U.keys(membros)) {
            String nome = on.containsKey(uuid) ? on.get(uuid) : membros.getString(uuid);
            var status = on.containsKey(uuid) ? U.green(" (online)") : U.gray(" (offline)");
            U.reply(src, U.yellow("- " + nome).append(status));
        }
        return membros.size();
    }

    public static int online(CommandContext<CommandSourceStack> ctx, String equipe) {
        CommandSourceStack src = ctx.getSource();
        List<ServerPlayer> lista = online(src.getServer(), new String[]{equipe}, "");

        if (lista.isEmpty()) {
            U.reply(src, U.red("Nenhum membro da equipe " + equipe.toUpperCase() + " está online."));
            return 0;
        }
        List<String> nomes = new ArrayList<>();
        for (ServerPlayer p : lista) nomes.add(U.nome(p));
        U.reply(src, U.gold("Equipe " + equipe.toUpperCase() + " online (" + lista.size() + "): ")
                .append(U.white(String.join(", ", nomes))));
        return lista.size();
    }
}
