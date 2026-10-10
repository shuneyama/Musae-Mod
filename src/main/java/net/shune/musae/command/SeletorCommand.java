package net.shune.musae.command;

import net.shune.musae.compatibility.MusaePermissoes;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.shune.musae.compatibility.TabModCompat;
import net.shune.musae.network.NetworkHandler;
import net.shune.musae.network.OpenSeletorPacket;
import net.shune.musae.nickname.NicknameData;
import net.shune.musae.race.RaceCapability;
import net.shune.musae.race.RaceData;
import net.shune.musae.race.RaceManager;
import net.shune.musae.race.RaceType;
import net.shune.musae.util.PlayerScaleData;

import java.util.Collection;

public class SeletorCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> buildSubcommand() {
        return Commands.literal("seletor")
                .requires(MusaePermissoes::admin)
                .executes(SeletorCommand::executeSelf)
                .then(Commands.argument("jogadores", EntityArgument.players())
                        .requires(MusaePermissoes::admin)
                        .executes(SeletorCommand::executeOthers));
    }

    private static int executeSelf(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            resetCharacter(player);
            NetworkHandler.sendToPlayer(player, new OpenSeletorPacket());
            return 1;
        } catch (CommandSyntaxException e) {
            ctx.getSource().sendFailure(Component.literal("Este comando só pode ser usado por um jogador."));
            return 0;
        }
    }

    private static int executeOthers(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "jogadores");
        for (ServerPlayer p : targets) {
            resetCharacter(p);
            NetworkHandler.sendToPlayer(p, new OpenSeletorPacket());
        }
        return targets.size();
    }

    private static void resetCharacter(ServerPlayer player) {
        RaceData data = RaceCapability.resolve(player);
        if (data.getRace() != RaceType.NENHUMA) {
            RaceManager.removeRace(player, data, data.getRace());
            RaceManager.syncToClient(player, data);
        }

        PlayerScaleData.setScale(player, PlayerScaleData.DEFAULT_SCALE);

        MinecraftServer server = player.getServer();
        if (server != null) {
            NicknameData nickData = NicknameData.get(server);
            if (nickData.getNickname(player.getUUID()) != null) {
                nickData.removeNickname(player.getUUID());
                nickData.syncToAll(server, player.getUUID());
                player.refreshDisplayName();
                player.refreshTabListName();
                TabModCompat.syncToTab(player, nickData);
                ClientboundPlayerInfoUpdatePacket info = new ClientboundPlayerInfoUpdatePacket(
                        ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, player);
                for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                    p.connection.send(info);
                }
            }
        }
    }
}