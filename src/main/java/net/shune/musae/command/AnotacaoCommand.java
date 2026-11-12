package net.shune.musae.command;

import net.shune.musae.compatibility.MusaePermissoes;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.shune.musae.monitor.Anotacao;
import net.shune.musae.monitor.AnotacaoManager;
import net.shune.musae.monitor.FormatoUtil;
import net.shune.musae.monitor.ListaAnotacoes;
import net.shune.musae.monitor.TipoAnotacao;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class AnotacaoCommand {

    private static final int ITENS_POR_PAGINA = 8;

    private static final SuggestionProvider<CommandSourceStack> SUGESTOES_TIPO = (ctx, builder) ->
            SharedSuggestionProvider.suggest(
                    Arrays.stream(TipoAnotacao.values()).map(TipoAnotacao::getId).collect(Collectors.toList()),
                    builder);

    public static LiteralArgumentBuilder<CommandSourceStack> buildAnotacaoSubcommand() {
        return Commands.literal("anotacao")
                .requires(MusaePermissoes::admin)
                .then(Commands.literal("adicionar")
                        .then(Commands.argument("jogador", GameProfileArgument.gameProfile())
                                .then(Commands.argument("tipo", StringArgumentType.word())
                                        .suggests(SUGESTOES_TIPO)
                                        .then(Commands.argument("texto", StringArgumentType.greedyString())
                                                .executes(AnotacaoCommand::adicionar)))))
                .then(Commands.literal("remover")
                        .then(Commands.argument("jogador", GameProfileArgument.gameProfile())
                                .then(Commands.argument("indice", IntegerArgumentType.integer(1))
                                        .executes(AnotacaoCommand::remover))))
                .then(Commands.literal("visualizar")
                        .then(Commands.argument("jogador", GameProfileArgument.gameProfile())
                                .executes(ctx -> visualizar(ctx, 1))
                                .then(Commands.argument("pagina", IntegerArgumentType.integer(1))
                                        .executes(ctx -> visualizar(ctx, IntegerArgumentType.getInteger(ctx, "pagina"))))));
    }

    private static GameProfile extrairPerfil(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<GameProfile> perfis = GameProfileArgument.getGameProfiles(ctx, "jogador");
        return perfis.iterator().next();
    }

    private static String limparTexto(String bruto) {
        String texto = bruto.trim();
        if (texto.startsWith("\"") && texto.endsWith("\"") && texto.length() > 1) {
            texto = texto.substring(1, texto.length() - 1).trim();
        }
        return texto;
    }

    private static int adicionar(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        GameProfile perfil = extrairPerfil(ctx);
        String tipoId = StringArgumentType.getString(ctx, "tipo");
        String texto = limparTexto(StringArgumentType.getString(ctx, "texto"));

        TipoAnotacao tipo = TipoAnotacao.fromId(tipoId);
        if (tipo == null) {
            ctx.getSource().sendFailure(Component.literal("Tipo de anotação inválido: " + tipoId)
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        if (texto.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("O texto da anotação não pode ser vazio.")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        String responsavel = ctx.getSource().getTextName();

        AnotacaoManager.adicionar(ctx.getSource().getServer(), perfil.getId().toString(), perfil.getName(),
                tipo, responsavel, texto);

        ctx.getSource().sendSuccess(() -> Component.literal("Anotação adicionada para " + perfil.getName()
                + " (" + tipo.getNomeExibicao() + ").").withStyle(tipo.getCor()), true);
        return 1;
    }

    private static int remover(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        GameProfile perfil = extrairPerfil(ctx);
        int indice = IntegerArgumentType.getInteger(ctx, "indice");

        boolean removido = AnotacaoManager.remover(ctx.getSource().getServer(), perfil.getName(), indice - 1);

        if (!removido) {
            ctx.getSource().sendFailure(Component.literal("Anotação #" + indice + " não encontrada.")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        ctx.getSource().sendSuccess(() -> Component.literal("Anotação #" + indice + " removida de "
                + perfil.getName() + ".").withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    private static int visualizar(CommandContext<CommandSourceStack> ctx, int pagina) throws CommandSyntaxException {
        GameProfile perfil = extrairPerfil(ctx);
        ListaAnotacoes lista = AnotacaoManager.carregar(ctx.getSource().getServer(), perfil.getName());

        if (lista.anotacoes.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("Nenhuma anotação encontrada para " + perfil.getName() + ".")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        List<Anotacao> anotacoes = lista.anotacoes;
        int total = anotacoes.size();
        int totalPaginas = Math.max(1, (int) Math.ceil(total / (double) ITENS_POR_PAGINA));
        pagina = Math.min(pagina, totalPaginas);

        ctx.getSource().sendSuccess(() -> Component.literal("== Anotações de " + perfil.getName() + " ==")
                .withStyle(ChatFormatting.GOLD), false);

        int inicio = (pagina - 1) * ITENS_POR_PAGINA;
        int fim = Math.min(total, inicio + ITENS_POR_PAGINA);

        for (int i = inicio; i < fim; i++) {
            Anotacao anotacao = anotacoes.get(i);
            int numero = i + 1;
            TipoAnotacao tipo = TipoAnotacao.fromId(anotacao.tipo);
            String nomeTipo = tipo != null ? tipo.getNomeExibicao() : anotacao.tipo;
            ChatFormatting corTipo = tipo != null ? tipo.getCor() : ChatFormatting.WHITE;

            ctx.getSource().sendSuccess(() -> {
                MutableComponent linha = Component.literal("#" + numero + " [").withStyle(ChatFormatting.WHITE);
                linha.append(Component.literal(nomeTipo).withStyle(corTipo));
                linha.append(Component.literal("] " + anotacao.comentario + " ").withStyle(ChatFormatting.WHITE));
                linha.append(Component.literal("(por " + anotacao.responsavel + ", "
                        + FormatoUtil.formatarDataHora(anotacao.horario) + ")").withStyle(ChatFormatting.GRAY));
                return linha;
            }, false);
        }

        int paginaFinal = pagina;
        int totalPaginasFinal = totalPaginas;
        ctx.getSource().sendSuccess(() -> Component.literal("Página " + paginaFinal + " de " + totalPaginasFinal)
                .withStyle(ChatFormatting.DARK_GRAY), false);

        return 1;
    }
}
