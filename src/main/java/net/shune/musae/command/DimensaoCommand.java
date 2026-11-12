package net.shune.musae.command;

import net.shune.musae.compatibility.MusaePermissoes;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.stream.Collectors;

public class DimensaoCommand {

    private static final SuggestionProvider<CommandSourceStack> SUGESTOES_DIMENSAO = (ctx, builder) ->
            SharedSuggestionProvider.suggestResource(
                    ctx.getSource().getServer().levelKeys().stream().map(ResourceKey::location),
                    builder);

    public static LiteralArgumentBuilder<CommandSourceStack> buildDimensaoSubcommand() {
        return Commands.literal("dimensao")
                .requires(MusaePermissoes::admin)
                .then(Commands.argument("jogadores", EntityArgument.players())
                        .then(Commands.argument("dimensao", ResourceLocationArgument.id())
                                .suggests(SUGESTOES_DIMENSAO)
                                .executes(DimensaoCommand::execute)));
    }

    private static int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<ServerPlayer> jogadores = EntityArgument.getPlayers(ctx, "jogadores");
        ResourceLocation local = ResourceLocationArgument.getId(ctx, "dimensao");

        ResourceKey<Level> chaveDimensao = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, local);
        ServerLevel destino = ctx.getSource().getServer().getLevel(chaveDimensao);

        if (destino == null) {
            ctx.getSource().sendFailure(Component.literal("Dimensão não encontrada: " + local)
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        Vec3 destinoPos = Vec3.atBottomCenterOf(destino.getSharedSpawnPos());
        int teleportados = 0;

        for (ServerPlayer jogador : jogadores) {
            if (jogador.level() == destino) {
                continue;
            }

            jogador.teleportTo(destino, destinoPos.x, destinoPos.y, destinoPos.z, jogador.getYRot(), jogador.getXRot());
            teleportados++;
        }

        if (teleportados == 0) {
            ctx.getSource().sendFailure(Component.literal("Nenhum jogador foi teleportado (já estavam na dimensão).")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        int totalTeleportados = teleportados;
        ctx.getSource().sendSuccess(() -> Component.literal(totalTeleportados == 1
                ? jogadores.iterator().next().getGameProfile().getName() + " foi teleportado para a dimensão " + local + "."
                : totalTeleportados + " jogadores foram teleportados para a dimensão " + local + ".")
                .withStyle(ChatFormatting.GREEN), true);
        return teleportados;
    }
}
