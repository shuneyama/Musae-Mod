package net.shune.musae.command;

import net.shune.musae.compatibility.MusaePermissoes;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.shune.musae.util.PlayerScaleData;

import java.util.Collection;

public class ScaleCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> buildScaleSubcommand() {
        return Commands.literal("escala")
                .requires(MusaePermissoes::admin)

                .then(Commands.argument("valor", FloatArgumentType.floatArg(
                                PlayerScaleData.COMMAND_MIN_SCALE, PlayerScaleData.COMMAND_MAX_SCALE))
                        .executes(ctx -> executeSetSelf(ctx, FloatArgumentType.getFloat(ctx, "valor")))

                        .then(Commands.argument("jogadores", EntityArgument.players())
                                .requires(MusaePermissoes::admin)
                                .executes(ctx -> executeSetOthers(ctx,
                                        FloatArgumentType.getFloat(ctx, "valor"),
                                        EntityArgument.getPlayers(ctx, "jogadores")))
                        )
                )

                .then(Commands.literal("verificar")
                        .executes(ScaleCommand::executeGetSelf)
                        .then(Commands.argument("jogador", EntityArgument.player())
                                .requires(MusaePermissoes::admin)
                                .executes(ctx -> executeGetOther(ctx,
                                        EntityArgument.getPlayer(ctx, "jogador")))
                        )
                )

                .then(Commands.literal("resetar")
                        .executes(ScaleCommand::executeResetSelf)
                        .then(Commands.argument("jogadores", EntityArgument.players())
                                .requires(MusaePermissoes::admin)
                                .executes(ctx -> executeResetOthers(ctx,
                                        EntityArgument.getPlayers(ctx, "jogadores")))
                        )
                )

                .then(Commands.literal("alterar")
                        .requires(MusaePermissoes::admin)
                        .then(Commands.argument("jogadores", EntityArgument.players())
                                .executes(ctx -> executeAlterar(ctx,
                                        EntityArgument.getPlayers(ctx, "jogadores"),
                                        PlayerScaleData.DEFAULT_SCALE))
                                .then(Commands.argument("valor", FloatArgumentType.floatArg(
                                                PlayerScaleData.COMMAND_MIN_SCALE, PlayerScaleData.COMMAND_MAX_SCALE))
                                        .executes(ctx -> executeAlterar(ctx,
                                                EntityArgument.getPlayers(ctx, "jogadores"),
                                                FloatArgumentType.getFloat(ctx, "valor")))
                                )
                        )
                );
    }

    private static int executeSetSelf(CommandContext<CommandSourceStack> ctx, float scale) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            PlayerScaleData.setScale(player, scale, PlayerScaleData.COMMAND_MIN_SCALE, PlayerScaleData.COMMAND_MAX_SCALE);
            ctx.getSource().sendSuccess(
                    () -> Component.literal("Sua escala foi definida para §e" + scale + "§r."),
                    false);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Este comando só pode ser usado por um jogador."));
            return 0;
        }
    }

    private static int executeSetOthers(CommandContext<CommandSourceStack> ctx,
                                        float scale, Collection<ServerPlayer> targets) {
        for (ServerPlayer target : targets) {
            PlayerScaleData.setScale(target, scale, PlayerScaleData.COMMAND_MIN_SCALE, PlayerScaleData.COMMAND_MAX_SCALE);
            ctx.getSource().sendSuccess(
                    () -> Component.literal("Escala de §b" + target.getName().getString()
                            + "§r definida para §e" + scale + "§r."),
                    true);
        }
        return targets.size();
    }

    private static int executeGetSelf(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            float scale = PlayerScaleData.getScale(player);
            ctx.getSource().sendSuccess(
                    () -> Component.literal("Sua escala atual é §e" + scale + "§r."),
                    false);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Este comando só pode ser usado por um jogador."));
            return 0;
        }
    }

    private static int executeGetOther(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
        float scale = PlayerScaleData.getScale(target);
        ctx.getSource().sendSuccess(
                () -> Component.literal("Escala de §b" + target.getName().getString()
                        + "§r: §e" + scale + "§r."),
                false);
        return 1;
    }

    private static int executeResetSelf(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            PlayerScaleData.setScale(player, PlayerScaleData.DEFAULT_SCALE);
            ctx.getSource().sendSuccess(
                    () -> Component.literal("Sua escala foi resetada para §e" + PlayerScaleData.DEFAULT_SCALE + "§r."),
                    false);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Este comando só pode ser usado por um jogador."));
            return 0;
        }
    }

    private static int executeResetOthers(CommandContext<CommandSourceStack> ctx,
                                          Collection<ServerPlayer> targets) {
        for (ServerPlayer target : targets) {
            PlayerScaleData.setScale(target, PlayerScaleData.DEFAULT_SCALE);
            ctx.getSource().sendSuccess(
                    () -> Component.literal("Escala de §b" + target.getName().getString()
                            + "§r resetada para §e" + PlayerScaleData.DEFAULT_SCALE + "§r."),
                    true);
        }
        return targets.size();
    }

    private static int executeAlterar(CommandContext<CommandSourceStack> ctx,
                                       Collection<ServerPlayer> targets, float scale) {
        for (ServerPlayer target : targets) {
            PlayerScaleData.setScale(target, scale, PlayerScaleData.COMMAND_MIN_SCALE, PlayerScaleData.COMMAND_MAX_SCALE);
            ctx.getSource().sendSuccess(
                    () -> Component.literal("Escala de §b" + target.getName().getString()
                            + "§r alterada para §e" + scale + "§r."),
                    true);
        }
        return targets.size();
    }
}
