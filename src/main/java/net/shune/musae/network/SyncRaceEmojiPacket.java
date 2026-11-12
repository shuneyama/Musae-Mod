package net.shune.musae.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.shune.musae.auth.AutorizacaoServidor;
import net.shune.musae.client.ClientRaceEmojiHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class SyncRaceEmojiPacket {

    private final boolean replace;
    private final Map<UUID, String> entries;

    public SyncRaceEmojiPacket(boolean replace, Map<UUID, String> entries) {
        this.replace = replace;
        this.entries = entries;
    }

    public static SyncRaceEmojiPacket single(UUID uuid, String emoji) {
        Map<UUID, String> map = new HashMap<>();
        map.put(uuid, emoji == null ? "" : emoji);
        return new SyncRaceEmojiPacket(false, map);
    }

    public static void encode(SyncRaceEmojiPacket pkt, FriendlyByteBuf buf) {
        buf.writeBoolean(pkt.replace);
        buf.writeVarInt(pkt.entries.size());
        for (Map.Entry<UUID, String> e : pkt.entries.entrySet()) {
            buf.writeUUID(e.getKey());
            buf.writeUtf(e.getValue(), 32);
        }
    }

    public static SyncRaceEmojiPacket decode(FriendlyByteBuf buf) {
        boolean replace = buf.readBoolean();
        int size = buf.readVarInt();
        Map<UUID, String> map = new HashMap<>();
        for (int i = 0; i < size; i++) {
            map.put(buf.readUUID(), buf.readUtf(32));
        }
        return new SyncRaceEmojiPacket(replace, map);
    }

    public static void handle(SyncRaceEmojiPacket pkt, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            if (!AutorizacaoServidor.isAutorizado()) return;
            ClientRaceEmojiHandler.apply(pkt.replace, pkt.entries);
        });
        ctx.setPacketHandled(true);
    }
}
