package net.shune.musae.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.shune.musae.MusaeMod;
import net.shune.musae.auth.AutorizacaoServidor;
import net.shune.musae.compatibility.TabModCompat;
import net.shune.musae.nickname.NicknameData;
import net.shune.musae.race.RaceManager;
import net.shune.musae.race.RaceType;
import net.shune.musae.util.PlayerScaleData;

import java.util.List;
import java.util.function.Supplier;

public class ApplyCharacterPacket {

    private static final int MAX_NICK_LENGTH = 16;
    private static final float MIN_SCALE = 1.50f / 1.80f;
    private static final float MAX_SCALE = 2.25f / 1.80f;
    private static final float INSETO_SCALE = 1.00f / 1.80f;

    private final String raceId;
    private final String nickname;
    private final float scale;

    public ApplyCharacterPacket(String raceId, String nickname, float scale) {
        this.raceId = raceId;
        this.nickname = nickname;
        this.scale = scale;
    }

    public static void encode(ApplyCharacterPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.raceId, 32);
        buf.writeUtf(packet.nickname, MAX_NICK_LENGTH * 4);
        buf.writeFloat(packet.scale);
    }

    public static ApplyCharacterPacket decode(FriendlyByteBuf buf) {
        return new ApplyCharacterPacket(
                buf.readUtf(32),
                buf.readUtf(MAX_NICK_LENGTH * 4),
                buf.readFloat()
        );
    }

    public static void handle(ApplyCharacterPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            if (!AutorizacaoServidor.isAutorizado()) return;
            ServerPlayer player = ctx.getSender();
            if (player == null) return;

            RaceType race = RaceType.fromId(packet.raceId);
            if (race == null || race == RaceType.NENHUMA) {
                MusaeMod.LOGGER.warn("[MusaeMod] ApplyCharacterPacket: raça inválida de {}: {}",
                        player.getName().getString(), packet.raceId);
                return;
            }

            String nick = packet.nickname == null ? "" : packet.nickname.trim();
            nick = nick.replaceAll("[§&]", "");
            if (nick.length() > MAX_NICK_LENGTH) nick = nick.substring(0, MAX_NICK_LENGTH);
            if (nick.isEmpty()) {
                MusaeMod.LOGGER.warn("[MusaeMod] ApplyCharacterPacket: nickname vazio de {}",
                        player.getName().getString());
                return;
            }

            float finalScale;
            if (race == RaceType.QUIMERA_INSETO) {
                finalScale = INSETO_SCALE;
            } else {
                finalScale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, packet.scale));
            }

            applyRace(player, race);
            applyNickname(player, nick);
            applyScale(player, finalScale);
        });
        ctx.setPacketHandled(true);
    }

    private static void applyRace(ServerPlayer player, RaceType race) {
        RaceManager.setRace(player, race);
    }

    private static void applyNickname(ServerPlayer player, String nick) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        NicknameData data = NicknameData.get(server);

        if (data.isNicknameTaken(nick, player.getUUID())) {
            player.sendSystemMessage(Component.literal("§cO nickname §e" + nick + "§c já está em uso, mantido o anterior."));
            return;
        }

        data.setNickname(player.getUUID(), nick, List.of());
        data.syncToAll(server, player.getUUID());

        player.refreshDisplayName();
        player.refreshTabListName();
        TabModCompat.syncToTab(player, data);

        ClientboundPlayerInfoUpdatePacket info = new ClientboundPlayerInfoUpdatePacket(
                ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, player);
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            p.connection.send(info);
        }
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            server.getCommands().sendCommands(p);
        }
    }

    private static void applyScale(ServerPlayer player, float scale) {
        PlayerScaleData.setScale(player, scale);
    }
}