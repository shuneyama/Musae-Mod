package net.shune.musae.comandos;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class Notas {
    private Notas() {
    }

    public static final String KEY = "musae_bloco_notas";

    private static CompoundTag user(MinecraftServer server, String uuid) {
        CompoundTag root = U.root(server, KEY);
        if (!root.contains(uuid, 10)) root.put(uuid, new CompoundTag());
        return root.getCompound(uuid);
    }

    private static String achar(CompoundTag user, String nome) {
        if (user.contains(nome)) return nome;
        for (String k : U.keys(user)) {
            if (k.equalsIgnoreCase(nome)) return k;
        }
        return null;
    }

    public static CompletableFuture<Suggestions> sugerir(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        try {
            ServerPlayer p = U.player(ctx.getSource());
            if (p != null) {
                CompoundTag u = user(ctx.getSource().getServer(), p.getStringUUID());
                for (String k : U.keys(u)) builder.suggest(U.arg(k));
            }
        } catch (Exception ignored) {
        }
        return builder.buildFuture();
    }

    private static ServerPlayer jogador(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer p = U.player(ctx.getSource());
        if (p == null) U.reply(ctx.getSource(), U.red("Esse comando só pode ser usado por jogadores."));
        return p;
    }

    public static int adicionar(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer p = jogador(ctx);
        if (p == null) return 0;
        CommandSourceStack src = ctx.getSource();
        CompoundTag user = user(src.getServer(), p.getStringUUID());

        String nome = StringArgumentType.getString(ctx, "nome").trim();
        String texto = StringArgumentType.getString(ctx, "texto").trim();

        if (nome.isEmpty() || texto.isEmpty()) {
            U.reply(src, U.red("Informe um nome e um texto."));
            return 0;
        }
        if (nome.length() > 32 || nome.contains("\"")) {
            U.reply(src, U.red("Nome inválido (máximo 32 caracteres e sem aspas)."));
            return 0;
        }
        if (achar(user, nome) != null) {
            U.reply(src, U.red("Já existe uma nota chamada \"" + nome + "\". Use editar ou anexar."));
            return 0;
        }

        CompoundTag n = new CompoundTag();
        n.putString("texto", texto);
        n.putString("data", U.agora());
        n.putString("editada", "");
        n.putLong("ts", System.currentTimeMillis());
        user.put(nome, n);

        U.reply(src, U.green("Nota \"" + nome + "\" criada."));
        return 1;
    }

    private static final class Item {
        String nome, data, texto;
        long ts;
    }

    private static int listar(CommandSourceStack src, ServerPlayer p, String filtro) {
        CompoundTag user = user(src.getServer(), p.getStringUUID());
        List<Item> itens = new ArrayList<>();
        for (String nome : U.keys(user)) {
            CompoundTag n = user.getCompound(nome);
            String texto = n.getString("texto");
            if (filtro != null) {
                String f = filtro.toLowerCase();
                if (!nome.toLowerCase().contains(f) && !texto.toLowerCase().contains(f)) continue;
            }
            Item it = new Item();
            it.nome = nome;
            it.data = n.getString("data");
            it.texto = texto;
            it.ts = n.getLong("ts");
            itens.add(it);
        }

        if (itens.isEmpty()) {
            U.reply(src, U.red(filtro != null
                    ? "Nenhuma nota encontrada para \"" + filtro + "\"."
                    : "Seu bloco de notas está vazio. Use /musae bloco_notas adicionar <nome> texto"));
            return 0;
        }

        itens.sort((a, b) -> Long.compare(b.ts, a.ts));
        U.reply(src, U.gold((filtro != null ? "===== Busca: " + filtro : "===== Seu bloco de notas") + " (" + itens.size() + ") ====="));
        U.reply(src, U.gray("Clique em uma nota para abrir."));

        for (Item n : itens) {
            String linha = n.texto.replace("\n", " ");
            String previa = linha.length() > 40 ? linha.substring(0, 40) + "..." : linha;
            String dica = n.texto.length() > 300 ? n.texto.substring(0, 300) + "..." : n.texto;
            String data16 = n.data.length() >= 16 ? n.data.substring(0, 16) : n.data;

            MutableComponent nomeClick = Component.literal(n.nome).withStyle(ChatFormatting.AQUA).withStyle(ChatFormatting.UNDERLINE)
                    .withStyle(s -> s.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/musae bloco_notas abrir " + U.arg(n.nome)))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(dica))));

            U.reply(src, U.gray("[" + data16 + "] ").append(nomeClick).append(U.gray(" - " + previa)));
        }
        return itens.size();
    }

    public static int ver(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer p = jogador(ctx);
        if (p == null) return 0;
        return listar(ctx.getSource(), p, null);
    }

    public static int buscar(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer p = jogador(ctx);
        if (p == null) return 0;
        return listar(ctx.getSource(), p, StringArgumentType.getString(ctx, "busca"));
    }

    public static int abrir(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer p = jogador(ctx);
        if (p == null) return 0;
        CommandSourceStack src = ctx.getSource();
        CompoundTag user = user(src.getServer(), p.getStringUUID());
        String nome = achar(user, StringArgumentType.getString(ctx, "nome"));

        if (nome == null) {
            U.reply(src, U.red("Nota não encontrada. Use /musae bloco_notas ver."));
            return 0;
        }

        CompoundTag n = user.getCompound(nome);
        String info = "Criada: " + n.getString("data");
        if (!n.getString("editada").isEmpty()) info += "  |  Editada: " + n.getString("editada");

        U.reply(src, U.gold("===== Nota: " + nome + " ====="));
        U.reply(src, U.gray(info));
        for (String l : n.getString("texto").split("\n")) U.reply(src, U.white(l));

        String arg = U.arg(nome);
        U.tell(p, Component.empty()
                .append(U.btnSug("[EDITAR]", ChatFormatting.YELLOW, "/musae bloco_notas editar " + arg + " ", "Substituir o texto da nota"))
                .append(U.white("  "))
                .append(U.btnSug("[ANEXAR]", ChatFormatting.AQUA, "/musae bloco_notas anexar " + arg + " ", "Acrescentar uma linha à nota"))
                .append(U.white("  "))
                .append(U.btnSug("[REMOVER]", ChatFormatting.RED, "/musae bloco_notas remover " + arg, "Apagar a nota (confirme com Enter)")));
        return 1;
    }

    public static int atualizar(CommandContext<CommandSourceStack> ctx, boolean anexar) {
        ServerPlayer p = jogador(ctx);
        if (p == null) return 0;
        CommandSourceStack src = ctx.getSource();
        CompoundTag user = user(src.getServer(), p.getStringUUID());
        String nome = achar(user, StringArgumentType.getString(ctx, "nome"));

        if (nome == null) {
            U.reply(src, U.red("Nota não encontrada. Use /musae bloco_notas ver."));
            return 0;
        }

        String texto = StringArgumentType.getString(ctx, "texto").trim();
        if (texto.isEmpty()) {
            U.reply(src, U.red("Informe o texto."));
            return 0;
        }

        CompoundTag n = user.getCompound(nome);
        String agora = U.agora();
        if (anexar) n.putString("texto", n.getString("texto") + "\n[" + agora.substring(0, 16) + "] " + texto);
        else n.putString("texto", texto);
        n.putString("editada", agora);
        n.putLong("ts", System.currentTimeMillis());

        U.reply(src, U.green(anexar ? "Linha acrescentada à nota \"" + nome + "\"." : "Nota \"" + nome + "\" editada."));
        return 1;
    }

    public static int remover(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer p = jogador(ctx);
        if (p == null) return 0;
        CommandSourceStack src = ctx.getSource();
        CompoundTag user = user(src.getServer(), p.getStringUUID());
        String nome = achar(user, StringArgumentType.getString(ctx, "nome"));

        if (nome == null) {
            U.reply(src, U.red("Nota não encontrada. Use /musae bloco_notas ver."));
            return 0;
        }
        user.remove(nome);
        U.reply(src, U.green("Nota \"" + nome + "\" removida."));
        return 1;
    }
}
