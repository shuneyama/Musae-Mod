package net.shune.musae.command;

import net.shune.musae.compatibility.MusaePermissoes;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.shune.musae.chat.ChatManager;
import net.shune.musae.chat.ConfiguracaoChat;
import net.shune.musae.chat.PermissoesChatGlobal;
import net.shune.musae.auth.AutorizacaoServidor;

import java.util.Collection;

public class ChatCommand {

    public static void registerComandosGlobais(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("g")
                        .requires(src -> AutorizacaoServidor.isAutorizado() && podeUsarGlobal(src))
                        .then(Commands.argument("mensagem", StringArgumentType.greedyString())
                                .executes(ChatCommand::enviarGlobal))
        );

        dispatcher.register(
                Commands.literal("global")
                        .requires(src -> AutorizacaoServidor.isAutorizado() && podeUsarGlobal(src))
                        .then(Commands.argument("mensagem", StringArgumentType.greedyString())
                                .executes(ChatCommand::enviarGlobal))
        );
    }

    private static boolean podeUsarGlobal(CommandSourceStack src) {
        if (MusaePermissoes.admin(src)) return true;
        if (MusaePermissoes.global(src)) return true;
        return src.getEntity() instanceof ServerPlayer player && PermissoesChatGlobal.podeUsarGlobal(player);
    }

    private static int enviarGlobal(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
            ctx.getSource().sendFailure(Component.literal("Este comando só pode ser usado por um jogador."));
            return 0;
        }

        ChatManager.enviarMensagemGlobal(player, StringArgumentType.getString(ctx, "mensagem"));
        return 1;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> buildChatSubcommand() {
        return Commands.literal("chat")
                .requires(MusaePermissoes::admin)
                .then(Commands.literal("raio")
                        .then(Commands.literal("definir")
                                .then(Commands.argument("valor", DoubleArgumentType.doubleArg(1.0, 500.0))
                                        .executes(ChatCommand::definirRaio)))
                        .then(Commands.literal("verificar")
                                .executes(ChatCommand::verificarRaio)))
                .then(Commands.literal("permitir")
                        .then(Commands.argument("jogadores", EntityArgument.players())
                                .executes(ChatCommand::permitirGlobal)))
                .then(Commands.literal("revogar")
                        .then(Commands.argument("jogadores", EntityArgument.players())
                                .executes(ChatCommand::revogarGlobal)));
    }

    private static int definirRaio(CommandContext<CommandSourceStack> ctx) {
        double valor = DoubleArgumentType.getDouble(ctx, "valor");
        MinecraftServer servidor = ctx.getSource().getServer();

        ConfiguracaoChat config = ConfiguracaoChat.get(servidor);
        config.raioLocal = valor;
        ConfiguracaoChat.salvar(servidor);

        ctx.getSource().sendSuccess(() -> Component.literal("Raio do chat local definido para " + valor + " blocos.")
                .withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    private static int verificarRaio(CommandContext<CommandSourceStack> ctx) {
        double valor = ConfiguracaoChat.get(ctx.getSource().getServer()).raioLocal;

        ctx.getSource().sendSuccess(() -> Component.literal("Raio do chat local atual: " + valor + " blocos.")
                .withStyle(ChatFormatting.AQUA), false);
        return 1;
    }

    private static int permitirGlobal(CommandContext<CommandSourceStack> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        Collection<ServerPlayer> jogadores = EntityArgument.getPlayers(ctx, "jogadores");
        MinecraftServer servidor = ctx.getSource().getServer();
        int total = 0;

        for (ServerPlayer jogador : jogadores) {
            if (PermissoesChatGlobal.permitir(servidor, jogador.getUUID())) {
                total++;
                jogador.sendSystemMessage(Component.literal("Você agora pode usar /g e /global.")
                        .withStyle(ChatFormatting.GREEN));
            }
        }

        int totalFinal = total;
        ctx.getSource().sendSuccess(() -> Component.literal(totalFinal + " jogador(es) liberado(s) para usar o chat global.")
                .withStyle(ChatFormatting.GREEN), true);
        return total;
    }

    private static int revogarGlobal(CommandContext<CommandSourceStack> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        Collection<ServerPlayer> jogadores = EntityArgument.getPlayers(ctx, "jogadores");
        MinecraftServer servidor = ctx.getSource().getServer();
        int total = 0;

        for (ServerPlayer jogador : jogadores) {
            if (PermissoesChatGlobal.revogar(servidor, jogador.getUUID())) {
                total++;
                jogador.sendSystemMessage(Component.literal("Sua permissão para usar /g e /global foi revogada.")
                        .withStyle(ChatFormatting.RED));
            }
        }

        int totalFinal = total;
        ctx.getSource().sendSuccess(() -> Component.literal(totalFinal + " jogador(es) tiveram a permissão de chat global revogada.")
                .withStyle(ChatFormatting.GREEN), true);
        return total;
    }
}
