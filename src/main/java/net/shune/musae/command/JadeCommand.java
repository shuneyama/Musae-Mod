package net.shune.musae.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.shune.musae.compatibility.MusaePermissoes;
import net.shune.musae.compatibility.PermissoesJade;

import java.util.Collection;

public class JadeCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> buildJadeSubcommand() {
        return Commands.literal("jade")
                .requires(MusaePermissoes::admin)
                .then(Commands.literal("permitir")
                        .then(Commands.argument("jogadores", EntityArgument.players())
                                .executes(JadeCommand::permitir)))
                .then(Commands.literal("revogar")
                        .then(Commands.argument("jogadores", EntityArgument.players())
                                .executes(JadeCommand::revogar)));
    }

    private static int permitir(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<ServerPlayer> jogadores = EntityArgument.getPlayers(ctx, "jogadores");
        MinecraftServer servidor = ctx.getSource().getServer();
        int total = 0;

        for (ServerPlayer jogador : jogadores) {
            if (PermissoesJade.permitir(servidor, jogador.getUUID())) {
                total++;
                jogador.sendSystemMessage(Component.literal("Você agora pode ver os nicks dos jogadores pelo Jade.")
                        .withStyle(ChatFormatting.GREEN));
            }
            PermissoesJade.sincronizar(jogador);
        }

        int totalFinal = total;
        ctx.getSource().sendSuccess(() -> Component.literal(totalFinal + " jogador(es) liberado(s) para ver nicks no Jade.")
                .withStyle(ChatFormatting.GREEN), true);
        return total;
    }

    private static int revogar(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<ServerPlayer> jogadores = EntityArgument.getPlayers(ctx, "jogadores");
        MinecraftServer servidor = ctx.getSource().getServer();
        int total = 0;

        for (ServerPlayer jogador : jogadores) {
            if (PermissoesJade.revogar(servidor, jogador.getUUID())) {
                total++;
                jogador.sendSystemMessage(Component.literal("Sua permissão para ver nicks no Jade foi revogada.")
                        .withStyle(ChatFormatting.RED));
            }
            PermissoesJade.sincronizar(jogador);
        }

        int totalFinal = total;
        ctx.getSource().sendSuccess(() -> Component.literal(totalFinal + " jogador(es) tiveram a visualização de nicks no Jade revogada.")
                .withStyle(ChatFormatting.GREEN), true);
        return total;
    }
}
