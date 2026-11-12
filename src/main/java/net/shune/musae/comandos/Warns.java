package net.shune.musae.comandos;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public final class Warns {
    private Warns() {
    }

    public static final String KEY = "musae_warns";
    public static final int KICK_EM = 3;
    public static final int BAN_EM = 5;
    public static final int EXPIRA_DIAS = 0;

    private static boolean ativo(CompoundTag w) {
        if (EXPIRA_DIAS <= 0) return true;
        return System.currentTimeMillis() - w.getLong("ts") < EXPIRA_DIAS * 86400000L;
    }

    private static int contar(ListTag lista) {
        int n = 0;
        for (int i = 0; i < lista.size(); i++) {
            if (ativo(lista.getCompound(i))) n++;
        }
        return n;
    }

    private static GameProfile perfil(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return GameProfileArgument.getGameProfiles(ctx, "player").iterator().next();
    }

    public static int adicionar(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        MinecraftServer server = src.getServer();
        GameProfile profile = perfil(ctx);
        String uuid = profile.getId().toString();
        String nome = profile.getName();
        String motivo = StringArgumentType.getString(ctx, "motivo").trim();

        if (motivo.isEmpty()) {
            U.reply(src, U.red("Informe o motivo do warn."));
            return 0;
        }
        if (motivo.length() > 200) motivo = motivo.substring(0, 200);

        CompoundTag root = U.root(server, KEY);
        ListTag lista = root.contains(uuid, 9) ? root.getList(uuid, 10) : new ListTag();

        CompoundTag w = new CompoundTag();
        w.putString("alvo", nome);
        w.putString("motivo", motivo);
        w.putString("staff", src.getTextName());
        w.putString("data", U.agora());
        w.putLong("ts", System.currentTimeMillis());
        lista.add(w);
        root.put(uuid, lista);

        int ativos = contar(lista);
        Logs.acao(server, src.getTextName(), "Warn adicionado", nome + " (" + ativos + " ativos): " + motivo);
        U.reply(src, U.green("Warn #" + lista.size() + " aplicado em " + nome + " (" + ativos + " ativo(s))."));

        ServerPlayer alvo = U.player(server, uuid);
        if (alvo != null) {
            String n = U.nome(alvo);
            U.tell(alvo, U.red("========== ⚠ AVISO (WARN) =========="));
            U.tell(alvo, U.gray("Você recebeu um warn de ").append(U.yellow(src.getTextName())).append(U.gray(".")));
            U.tell(alvo, U.gray("Motivo: ").append(U.white(motivo)));
            U.tell(alvo, U.gray("Warns ativos: ").append(U.red(String.valueOf(ativos))));
            U.tell(alvo, U.red("====================================="));
            U.cmd(server, "title " + n + " times 10 80 20");
            U.cmd(server, "title " + n + " subtitle " + U.json(motivo, "yellow", false));
            U.cmd(server, "title " + n + " title " + U.json("⚠ Você recebeu um WARN", "red", true));
            U.cmd(server, "playsound minecraft:block.note_block.bass master " + n);
        }

        MutableComponent aviso = U.red("[WARN] ")
                .append(U.yellow(src.getTextName()))
                .append(U.white(" deu warn em "))
                .append(U.yellow(nome))
                .append(U.white(" (" + ativos + " ativo(s)): " + motivo));
        ServerPlayer eu = U.player(src);
        String euUuid = eu != null ? eu.getStringUUID() : "";
        for (ServerPlayer d : Teams.staffOnlineGeral(server, euUuid)) U.tell(d, aviso);

        if (BAN_EM > 0 && ativos >= BAN_EM) {
            U.cmd(server, "ban " + nome + " Limite de warns atingido (" + ativos + ")");
            Logs.acao(server, "AUTO", "Ban automático", nome + " (" + ativos + " warns)");
            U.reply(src, U.darkRed(nome + " atingiu " + BAN_EM + " warns e foi banido automaticamente."));
        } else if (KICK_EM > 0 && ativos == KICK_EM) {
            U.cmd(server, "kick " + nome + " Você atingiu " + ativos + " warns. Leia as regras do servidor.");
            Logs.acao(server, "AUTO", "Kick automático", nome + " (" + ativos + " warns)");
            U.reply(src, U.gold(nome + " atingiu " + KICK_EM + " warns e foi kickado automaticamente."));
        }
        return 1;
    }

    private static void mostrar(CommandSourceStack src, String titulo, ListTag lista, boolean comStaff) {
        U.reply(src, U.gold("===== " + titulo + " (" + lista.size() + ", " + contar(lista) + " ativos) ====="));
        for (int i = 0; i < lista.size(); i++) {
            CompoundTag w = lista.getCompound(i);
            MutableComponent linha = U.yellow("#" + (i + 1) + " ")
                    .append(U.gray("[" + w.getString("data") + "] "));
            if (comStaff) linha.append(U.aqua(w.getString("staff") + ": "));
            linha.append(U.white(w.getString("motivo")));
            if (!ativo(w)) linha.append(U.darkGray(" (expirado)"));
            U.reply(src, linha);
        }
    }

    public static int ver(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        GameProfile profile = perfil(ctx);
        String uuid = profile.getId().toString();
        CompoundTag root = U.root(src.getServer(), KEY);

        if (!root.contains(uuid, 9) || root.getList(uuid, 10).isEmpty()) {
            U.reply(src, U.red(profile.getName() + " não possui warns."));
            return 0;
        }
        mostrar(src, "Warns de " + profile.getName(), root.getList(uuid, 10), true);
        return 1;
    }

    public static int meus(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer p = U.player(src);
        if (p == null) {
            U.reply(src, U.red("Esse comando só pode ser usado por jogadores."));
            return 0;
        }
        String uuid = p.getStringUUID();
        CompoundTag root = U.root(src.getServer(), KEY);

        if (!root.contains(uuid, 9) || root.getList(uuid, 10).isEmpty()) {
            U.reply(src, U.green("Você não possui warns."));
            return 1;
        }
        mostrar(src, "Seus warns", root.getList(uuid, 10), false);
        return 1;
    }

    public static int remover(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        MinecraftServer server = src.getServer();
        GameProfile profile = perfil(ctx);
        String uuid = profile.getId().toString();
        CompoundTag root = U.root(server, KEY);
        int numero = IntegerArgumentType.getInteger(ctx, "numero");

        if (!root.contains(uuid, 9) || root.getList(uuid, 10).isEmpty()) {
            U.reply(src, U.red(profile.getName() + " não possui warns."));
            return 0;
        }

        ListTag antiga = root.getList(uuid, 10);
        if (numero < 1 || numero > antiga.size()) {
            U.reply(src, U.red("Número inválido. Use um valor entre 1 e " + antiga.size() + "."));
            return 0;
        }

        String removido = antiga.getCompound(numero - 1).getString("motivo");
        ListTag nova = new ListTag();
        for (int i = 0; i < antiga.size(); i++) {
            if (i != numero - 1) nova.add(antiga.getCompound(i));
        }
        if (nova.isEmpty()) root.remove(uuid);
        else root.put(uuid, nova);

        Logs.acao(server, src.getTextName(), "Warn removido", profile.getName() + " #" + numero + " (\"" + removido + "\")");
        U.reply(src, U.green("Warn #" + numero + " de " + profile.getName() + " removido."));

        ServerPlayer alvo = U.player(server, uuid);
        if (alvo != null) U.tell(alvo, U.green("Um de seus warns foi removido pela staff."));
        return 1;
    }

    public static int limpar(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        GameProfile profile = perfil(ctx);
        String uuid = profile.getId().toString();
        CompoundTag root = U.root(src.getServer(), KEY);

        if (!root.contains(uuid)) {
            U.reply(src, U.red(profile.getName() + " não possui warns."));
            return 0;
        }
        int qtd = root.getList(uuid, 10).size();
        root.remove(uuid);
        Logs.acao(src.getServer(), src.getTextName(), "Warns apagados", profile.getName() + " (" + qtd + ")");
        U.reply(src, U.green("Todos os warns de " + profile.getName() + " foram apagados."));
        return 1;
    }

    private static final class Linha {
        String nome;
        int ativos;
    }

    public static int listaGeral(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        CompoundTag root = U.root(src.getServer(), KEY);
        List<Linha> linhas = new ArrayList<>();

        for (String uuid : U.keys(root)) {
            ListTag lista = root.getList(uuid, 10);
            if (lista.isEmpty()) continue;
            int ativos = contar(lista);
            if (ativos == 0) continue;
            Linha l = new Linha();
            l.nome = lista.getCompound(lista.size() - 1).getString("alvo");
            l.ativos = ativos;
            linhas.add(l);
        }

        if (linhas.isEmpty()) {
            U.reply(src, U.red("Nenhum jogador com warns ativos."));
            return 0;
        }

        linhas.sort((a, b) -> Integer.compare(b.ativos, a.ativos));
        U.reply(src, U.gold("===== Jogadores com warns (" + linhas.size() + ") ====="));
        for (Linha l : linhas) {
            U.reply(src, U.yellow("- " + l.nome + ": ").append(U.red(l.ativos + " warn(s)")));
        }
        return linhas.size();
    }
}
