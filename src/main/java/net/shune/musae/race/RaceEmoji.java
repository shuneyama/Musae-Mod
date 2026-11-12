package net.shune.musae.race;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.shune.musae.auth.AutorizacaoServidor;
import net.shune.musae.network.NetworkHandler;
import net.shune.musae.network.SyncRaceEmojiPacket;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class RaceEmoji {

    private RaceEmoji() {}

    public static String of(Player player) {
        if (player == null || player.level().isClientSide()) return "";
        if (!AutorizacaoServidor.isAutorizado()) return "";
        RaceType race = RaceCapability.get(player).map(RaceData::getRace).orElse(RaceType.NENHUMA);
        return race == null ? "" : race.getEmoji();
    }

    public static Component prefix(String emoji, Component base) {
        if (emoji == null || emoji.isEmpty()) return base;
        return Component.literal(emoji + " ").append(base);
    }

    public static void refresh(ServerPlayer player) {
        player.refreshDisplayName();
        player.refreshTabListName();

        NetworkHandler.sendToAll(SyncRaceEmojiPacket.single(player.getUUID(), of(player)));

        MinecraftServer server = player.getServer();
        if (server != null) {
            net.shune.musae.compatibility.TabModCompat.syncToTab(player,
                    net.shune.musae.nickname.NicknameData.get(server));
        }
    }

    public static void sendAllTo(ServerPlayer receiver) {
        MinecraftServer server = receiver.getServer();
        if (server == null) return;
        Map<UUID, String> entries = new HashMap<>();
        for (ServerPlayer online : server.getPlayerList().getPlayers()) {
            String emoji = of(online);
            if (!emoji.isEmpty()) entries.put(online.getUUID(), emoji);
        }
        NetworkHandler.sendToPlayer(receiver, new SyncRaceEmojiPacket(true, entries));
    }
}
