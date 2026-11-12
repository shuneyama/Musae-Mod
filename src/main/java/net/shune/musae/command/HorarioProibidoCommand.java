package net.shune.musae.command;

import net.shune.musae.compatibility.MusaePermissoes;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.shune.musae.monitor.ConfiguracaoHorarioProibido;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class HorarioProibidoCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> buildHorarioProibidoSubcommand() {
        return Commands.literal("horarioproibido")
                .requires(MusaePermissoes::admin)
                .then(Commands.literal("definir")
                        .then(Commands.argument("inicio", StringArgumentType.word())
                                .then(Commands.argument("fim", StringArgumentType.word())
                                        .executes(HorarioProibidoCommand::definir))))
                .then(Commands.literal("ativar").executes(ctx -> alternar(ctx, true)))
                .then(Commands.literal("desativar").executes(ctx -> alternar(ctx, false)))
                .then(Commands.literal("status").executes(HorarioProibidoCommand::status));
    }

    private static int definir(CommandContext<CommandSourceStack> ctx) {
        String inicio = StringArgumentType.getString(ctx, "inicio");
        String fim = StringArgumentType.getString(ctx, "fim");

        try {
            LocalTime.parse(inicio);
            LocalTime.parse(fim);
        } catch (DateTimeParseException e) {
            ctx.getSource().sendFailure(Component.translatable("musae.horarioproibido.formato_invalido")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        MinecraftServer servidor = ctx.getSource().getServer();
        ConfiguracaoHorarioProibido config = ConfiguracaoHorarioProibido.get(servidor);
        config.horaInicio = inicio;
        config.horaFim = fim;
        ConfiguracaoHorarioProibido.salvar(servidor);

        ctx.getSource().sendSuccess(() -> Component.translatable("musae.horarioproibido.definido", inicio, fim)
                .withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    private static int alternar(CommandContext<CommandSourceStack> ctx, boolean ativo) {
        MinecraftServer servidor = ctx.getSource().getServer();
        ConfiguracaoHorarioProibido config = ConfiguracaoHorarioProibido.get(servidor);
        config.ativado = ativo;
        ConfiguracaoHorarioProibido.salvar(servidor);

        ctx.getSource().sendSuccess(() -> Component.translatable(
                ativo ? "musae.horarioproibido.ativado" : "musae.horarioproibido.desativado"
        ).withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    private static int status(CommandContext<CommandSourceStack> ctx) {
        ConfiguracaoHorarioProibido config = ConfiguracaoHorarioProibido.get(ctx.getSource().getServer());

        ctx.getSource().sendSuccess(() -> Component.translatable("musae.horarioproibido.status",
                Component.translatable(config.ativado ? "musae.geral.ativado" : "musae.geral.desativado"),
                config.horaInicio, config.horaFim
        ).withStyle(ChatFormatting.AQUA), false);
        return 1;
    }
}
