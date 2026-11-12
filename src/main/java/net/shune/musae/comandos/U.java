package net.shune.musae.comandos;

import com.google.gson.JsonObject;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class U {
    private U() {
    }

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public static String agora() {
        return LocalDateTime.now().format(FMT);
    }

    public static CompoundTag root(MinecraftServer server, String key) {
        return MusaeData.get(server).root(key);
    }

    public static String dim(Entity e) {
        return e.level().dimension().location().toString();
    }

    public static ServerLevel level(MinecraftServer server, String dim) {
        return server.getLevel(ResourceKey.create(Registries.DIMENSION, new ResourceLocation(dim)));
    }

    public static MutableComponent txt(String s, ChatFormatting c) {
        return Component.literal(s).withStyle(c);
    }

    public static MutableComponent red(String s) { return txt(s, ChatFormatting.RED); }
    public static MutableComponent darkRed(String s) { return txt(s, ChatFormatting.DARK_RED); }
    public static MutableComponent gray(String s) { return txt(s, ChatFormatting.GRAY); }
    public static MutableComponent darkGray(String s) { return txt(s, ChatFormatting.DARK_GRAY); }
    public static MutableComponent green(String s) { return txt(s, ChatFormatting.GREEN); }
    public static MutableComponent yellow(String s) { return txt(s, ChatFormatting.YELLOW); }
    public static MutableComponent gold(String s) { return txt(s, ChatFormatting.GOLD); }
    public static MutableComponent white(String s) { return txt(s, ChatFormatting.WHITE); }
    public static MutableComponent aqua(String s) { return txt(s, ChatFormatting.AQUA); }

    public static MutableComponent negrito(String s, ChatFormatting c) {
        return Component.literal(s).withStyle(c).withStyle(ChatFormatting.BOLD);
    }

    public static MutableComponent botao(String texto, ChatFormatting cor, ClickEvent.Action acao, String valor, String dica) {
        return Component.literal(texto).withStyle(cor).withStyle(ChatFormatting.BOLD)
                .withStyle(s -> s.withClickEvent(new ClickEvent(acao, valor))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(dica))));
    }

    public static MutableComponent btnRun(String texto, ChatFormatting cor, String comando, String dica) {
        return botao(texto, cor, ClickEvent.Action.RUN_COMMAND, comando, dica);
    }

    public static MutableComponent btnSug(String texto, ChatFormatting cor, String comando, String dica) {
        return botao(texto, cor, ClickEvent.Action.SUGGEST_COMMAND, comando, dica);
    }

    public static MutableComponent link(String url) {
        return Component.literal(url).withStyle(ChatFormatting.AQUA).withStyle(ChatFormatting.UNDERLINE)
                .withStyle(s -> s.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url)));
    }

    public static void tell(ServerPlayer p, Component c) {
        p.sendSystemMessage(c);
    }

    public static void reply(CommandSourceStack src, Component c) {
        src.sendSuccess(() -> c, false);
    }

    public static ServerPlayer player(CommandSourceStack src) {
        return src.getEntity() instanceof ServerPlayer p ? p : null;
    }

    public static ServerPlayer player(MinecraftServer server, String uuid) {
        try {
            return server.getPlayerList().getPlayer(UUID.fromString(uuid));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static List<ServerPlayer> players(MinecraftServer server) {
        return new ArrayList<>(server.getPlayerList().getPlayers());
    }

    public static String nome(ServerPlayer p) {
        return p.getGameProfile().getName();
    }

    public static boolean ehOp(ServerPlayer p) {
        return p.hasPermissions(2);
    }

    public static List<String> keys(CompoundTag t) {
        return new ArrayList<>(t.getAllKeys());
    }

    public static void listAdd(CompoundTag root, String chave, CompoundTag item, int max) {
        ListTag lista = root.contains(chave, 9) ? root.getList(chave, 10) : new ListTag();
        lista.add(item);
        if (lista.size() > max) {
            ListTag nova = new ListTag();
            for (int i = lista.size() - max; i < lista.size(); i++) {
                nova.add(lista.getCompound(i));
            }
            lista = nova;
        }
        root.put(chave, lista);
    }

    public static String arg(String nome) {
        return nome.contains(" ") ? "\"" + nome + "\"" : nome;
    }

    public static void cmd(MinecraftServer server, String comando) {
        try {
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(), comando);
        } catch (Exception e) {
            net.shune.musae.MusaeMod.LOGGER.error("[musae] falha ao executar '" + comando + "'", e);
        }
    }

    public static String json(String texto, String cor, boolean negrito) {
        JsonObject o = new JsonObject();
        o.addProperty("text", texto);
        o.addProperty("color", cor);
        if (negrito) o.addProperty("bold", true);
        return o.toString();
    }
}
