package net.shune.musae.command;

import net.shune.musae.compatibility.MusaePermissoes;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.shune.musae.itens.ConfiguracaoItensBanidos;
import net.shune.musae.itens.PermissoesItensBanidos;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ItensCommand {

    private static final SuggestionProvider<CommandSourceStack> SUGESTOES_TODOS_ITENS = (ctx, builder) ->
            SharedSuggestionProvider.suggestResource(BuiltInRegistries.ITEM.keySet().stream(), builder);

    private static final SuggestionProvider<CommandSourceStack> SUGESTOES_ITENS_BANIDOS = (ctx, builder) ->
            SharedSuggestionProvider.suggestResource(
                    ConfiguracaoItensBanidos.getItensBanidos(ctx.getSource().getServer()).stream(), builder);

    public static LiteralArgumentBuilder<CommandSourceStack> buildItensSubcommand() {
        return Commands.literal("itens")
                .requires(MusaePermissoes::admin)
                .then(Commands.literal("banir")
                        .executes(ItensCommand::banirMao)
                        .then(Commands.argument("item_id", ResourceLocationArgument.id())
                                .suggests(SUGESTOES_TODOS_ITENS)
                                .executes(ItensCommand::banir)))
                .then(Commands.literal("desbanir")
                        .executes(ItensCommand::desbanirMao)
                        .then(Commands.argument("item_id", ResourceLocationArgument.id())
                                .suggests(SUGESTOES_ITENS_BANIDOS)
                                .executes(ItensCommand::desbanir)))
                .then(Commands.literal("listar")
                        .executes(ItensCommand::listar))
                .then(Commands.literal("jogadores")
                        .then(Commands.literal("liberar")
                                .then(Commands.argument("jogadores", EntityArgument.players())
                                        .executes(ItensCommand::liberarMao)
                                        .then(Commands.argument("item_id", ResourceLocationArgument.id())
                                                .suggests(SUGESTOES_ITENS_BANIDOS)
                                                .executes(ItensCommand::liberar))))
                        .then(Commands.literal("revogar")
                                .then(Commands.argument("jogadores", EntityArgument.players())
                                        .executes(ItensCommand::revogarMao)
                                        .then(Commands.argument("item_id", ResourceLocationArgument.id())
                                                .suggests(SUGESTOES_ITENS_BANIDOS)
                                                .executes(ItensCommand::revogar))))
                        .then(Commands.literal("verificar")
                                .then(Commands.argument("jogador", EntityArgument.player())
                                        .executes(ItensCommand::verificar))));
    }

    private static ResourceLocation obterItemDaMao(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) return null;
        ItemStack maoPrincipal = player.getMainHandItem();
        if (maoPrincipal.isEmpty()) return null;
        return BuiltInRegistries.ITEM.getKey(maoPrincipal.getItem());
    }

    private static int banirMao(CommandContext<CommandSourceStack> ctx) {
        return doBanir(ctx, obterItemDaMao(ctx));
    }

    private static int banir(CommandContext<CommandSourceStack> ctx) {
        return doBanir(ctx, ResourceLocationArgument.getId(ctx, "item_id"));
    }

    private static int desbanirMao(CommandContext<CommandSourceStack> ctx) {
        return doDesbanir(ctx, obterItemDaMao(ctx));
    }

    private static int desbanir(CommandContext<CommandSourceStack> ctx) {
        return doDesbanir(ctx, ResourceLocationArgument.getId(ctx, "item_id"));
    }

    private static int doBanir(CommandContext<CommandSourceStack> ctx, ResourceLocation itemId) {
        if (itemId == null) {
            ctx.getSource().sendFailure(Component.literal("Você precisa estar segurando um item na mão ou informar o ID.")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        if (!BuiltInRegistries.ITEM.containsKey(itemId)) {
            ctx.getSource().sendFailure(Component.literal("Item não encontrado no registro: " + itemId)
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        if (ConfiguracaoItensBanidos.banir(ctx.getSource().getServer(), itemId)) {
            ctx.getSource().sendSuccess(() -> Component.literal("Item banido: " + itemId)
                    .withStyle(ChatFormatting.GREEN), true);
            return 1;
        }

        ctx.getSource().sendFailure(Component.literal("Esse item já está banido: " + itemId)
                .withStyle(ChatFormatting.YELLOW));
        return 0;
    }

    private static int doDesbanir(CommandContext<CommandSourceStack> ctx, ResourceLocation itemId) {
        if (itemId == null) {
            ctx.getSource().sendFailure(Component.literal("Você precisa estar segurando um item na mão ou informar o ID.")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        if (ConfiguracaoItensBanidos.desbanir(ctx.getSource().getServer(), itemId)) {
            ctx.getSource().sendSuccess(() -> Component.literal("Item desbanido: " + itemId)
                    .withStyle(ChatFormatting.GREEN), true);
            return 1;
        }

        ctx.getSource().sendFailure(Component.literal("Esse item não estava banido: " + itemId)
                .withStyle(ChatFormatting.YELLOW));
        return 0;
    }

    private static int listar(CommandContext<CommandSourceStack> ctx) {
        Set<ResourceLocation> banidos = ConfiguracaoItensBanidos.getItensBanidos(ctx.getSource().getServer());

        if (banidos.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal("Nenhum item está banido no momento.")
                    .withStyle(ChatFormatting.YELLOW), false);
            return 0;
        }

        List<ResourceLocation> ordenados = banidos.stream().sorted().collect(Collectors.toList());

        ctx.getSource().sendSuccess(() -> Component.literal("== Itens banidos (" + ordenados.size() + ") ==")
                .withStyle(ChatFormatting.GOLD), false);

        for (ResourceLocation id : ordenados) {
            ctx.getSource().sendSuccess(() -> Component.literal("  - " + id).withStyle(ChatFormatting.WHITE), false);
        }

        return 1;
    }

    private static int liberarMao(CommandContext<CommandSourceStack> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        return doLiberar(ctx, obterItemDaMao(ctx));
    }

    private static int liberar(CommandContext<CommandSourceStack> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        return doLiberar(ctx, ResourceLocationArgument.getId(ctx, "item_id"));
    }

    private static int revogarMao(CommandContext<CommandSourceStack> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        return doRevogar(ctx, obterItemDaMao(ctx));
    }

    private static int revogar(CommandContext<CommandSourceStack> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        return doRevogar(ctx, ResourceLocationArgument.getId(ctx, "item_id"));
    }

    private static int doLiberar(CommandContext<CommandSourceStack> ctx, ResourceLocation itemId) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        if (itemId == null) {
            ctx.getSource().sendFailure(Component.literal("Você precisa estar segurando um item na mão ou informar o ID.")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        if (!ConfiguracaoItensBanidos.isBanido(ctx.getSource().getServer(), itemId)) {
            ctx.getSource().sendFailure(Component.literal("Esse item não está banido, não há necessidade de liberar: " + itemId)
                    .withStyle(ChatFormatting.YELLOW));
            return 0;
        }

        Collection<ServerPlayer> jogadores = EntityArgument.getPlayers(ctx, "jogadores");
        int total = 0;

        for (ServerPlayer jogador : jogadores) {
            if (PermissoesItensBanidos.liberar(ctx.getSource().getServer(), jogador.getUUID(), itemId)) {
                total++;
                ctx.getSource().sendSuccess(() -> Component.literal("Item " + itemId + " liberado para "
                        + jogador.getGameProfile().getName() + ".").withStyle(ChatFormatting.GREEN), true);
            }
        }

        if (total == 0) {
            ctx.getSource().sendFailure(Component.literal("Nenhum jogador precisava dessa liberação (já estava liberado pra todos).")
                    .withStyle(ChatFormatting.YELLOW));
            return 0;
        }

        return total;
    }

    private static int doRevogar(CommandContext<CommandSourceStack> ctx, ResourceLocation itemId) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        if (itemId == null) {
            ctx.getSource().sendFailure(Component.literal("Você precisa estar segurando um item na mão ou informar o ID.")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        Collection<ServerPlayer> jogadores = EntityArgument.getPlayers(ctx, "jogadores");
        int total = 0;

        for (ServerPlayer jogador : jogadores) {
            if (PermissoesItensBanidos.revogar(ctx.getSource().getServer(), jogador.getUUID(), itemId)) {
                total++;
                ctx.getSource().sendSuccess(() -> Component.literal("Liberação do item " + itemId + " revogada de "
                        + jogador.getGameProfile().getName() + ".").withStyle(ChatFormatting.GREEN), true);
            }
        }

        if (total == 0) {
            ctx.getSource().sendFailure(Component.literal("Nenhum dos jogadores tinha esse item liberado.")
                    .withStyle(ChatFormatting.YELLOW));
            return 0;
        }

        return total;
    }

    private static int verificar(CommandContext<CommandSourceStack> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer jogador = EntityArgument.getPlayer(ctx, "jogador");
        Set<ResourceLocation> banidos = ConfiguracaoItensBanidos.getItensBanidos(ctx.getSource().getServer());
        Set<ResourceLocation> permitidos = PermissoesItensBanidos.getPermitidos(ctx.getSource().getServer(), jogador.getUUID());

        List<ResourceLocation> liberadosRelevantes = permitidos.stream()
                .filter(banidos::contains)
                .sorted()
                .collect(Collectors.toList());

        if (liberadosRelevantes.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal(jogador.getGameProfile().getName()
                    + " não possui nenhum item banido liberado.").withStyle(ChatFormatting.YELLOW), false);
            return 0;
        }

        ctx.getSource().sendSuccess(() -> Component.literal("== Itens liberados para " + jogador.getGameProfile().getName()
                + " (" + liberadosRelevantes.size() + ") ==").withStyle(ChatFormatting.GOLD), false);

        for (ResourceLocation id : liberadosRelevantes) {
            ctx.getSource().sendSuccess(() -> Component.literal("  - " + id).withStyle(ChatFormatting.WHITE), false);
        }

        return 1;
    }
}
