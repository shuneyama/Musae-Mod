package net.shune.musae.command;

import net.shune.musae.compatibility.MusaePermissoes;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.shune.musae.monitor.ConfiguracaoMensagens;

public class MensagensCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> buildMensagensSubcommand() {
        return Commands.literal("mensagens")
                .requires(MusaePermissoes::admin)
                .then(Commands.literal("login")
                        .then(Commands.literal("ativar").executes(ctx -> definirLogin(ctx, true)))
                        .then(Commands.literal("desativar").executes(ctx -> definirLogin(ctx, false))))
                .then(Commands.literal("saida")
                        .then(Commands.literal("ativar").executes(ctx -> definirSaida(ctx, true)))
                        .then(Commands.literal("desativar").executes(ctx -> definirSaida(ctx, false))))
                .then(Commands.literal("status").executes(MensagensCommand::status));
    }

    private static int definirLogin(CommandContext<CommandSourceStack> ctx, boolean ativo) {
        MinecraftServer servidor = ctx.getSource().getServer();
        ConfiguracaoMensagens config = ConfiguracaoMensagens.get(servidor);
        config.loginAtivo = ativo;
        ConfiguracaoMensagens.salvar(servidor);

        ctx.getSource().sendSuccess(() -> Component.literal(
                ativo ? "Mensagens de entrada foram ativadas." : "Mensagens de entrada foram desativadas."
        ).withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    private static int definirSaida(CommandContext<CommandSourceStack> ctx, boolean ativo) {
        MinecraftServer servidor = ctx.getSource().getServer();
        ConfiguracaoMensagens config = ConfiguracaoMensagens.get(servidor);
        config.saidaAtivo = ativo;
        ConfiguracaoMensagens.salvar(servidor);

        ctx.getSource().sendSuccess(() -> Component.literal(
                ativo ? "Mensagens de saída foram ativadas." : "Mensagens de saída foram desativadas."
        ).withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    private static int status(CommandContext<CommandSourceStack> ctx) {
        ConfiguracaoMensagens config = ConfiguracaoMensagens.get(ctx.getSource().getServer());

        String loginStatus = config.loginAtivo ? "ativado" : "desativado";
        String saidaStatus = config.saidaAtivo ? "ativado" : "desativado";

        ctx.getSource().sendSuccess(() -> Component.literal("Mensagens de entrada: " + loginStatus
                + " | Mensagens de saída: " + saidaStatus).withStyle(ChatFormatting.AQUA), false);
        return 1;
    }
}
