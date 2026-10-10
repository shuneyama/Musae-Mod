package net.shune.musae.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.shune.musae.chat.PlayerBalaoData;
import net.shune.musae.network.NetworkHandler;
import net.shune.musae.network.OpenBalaoConfigPacket;

public class BalaoCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> buildBalaoSubcommand() {
        return Commands.literal("balao")
                .executes(BalaoCommand::abrirGui);
    }

    private static int abrirGui(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
            ctx.getSource().sendFailure(Component.literal("Este comando só pode ser usado por um jogador."));
            return 0;
        }

        PlayerBalaoData data = PlayerBalaoData.get(player.getServer(), player.getUUID());
        NetworkHandler.sendToPlayer(player, new OpenBalaoConfigPacket(
                data.corTexto, data.corFundo, data.corBorda, data.altura, data.estilo));
        return 1;
    }
}
