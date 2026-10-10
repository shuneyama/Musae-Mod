package net.shune.musae.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.shune.musae.auth.AutorizacaoServidor;
import net.shune.musae.client.ClientNicknameHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class SyncAllNicknamesPacket {

    private final Map<UUID, NickEntry> entries;

    public SyncAllNicknamesPacket(Map<UUID, NickEntry> entries) {
        this.entries = entries;
    }

    public static void encode(SyncAllNicknamesPacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.entries.size());
        for (Map.Entry<UUID, NickEntry> entry : packet.entries.entrySet()) {
            buf.writeUUID(entry.getKey());
            buf.writeUtf(entry.getValue().nickname);
            buf.writeUtf(entry.getValue().colors);
        }
    }

    public static SyncAllNicknamesPacket decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<UUID, NickEntry> entries = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            UUID uuid = buf.readUUID();
            String nickname = buf.readUtf();
            String colors = buf.readUtf();
            entries.put(uuid, new NickEntry(nickname, colors));
        }
        return new SyncAllNicknamesPacket(entries);
    }

    public static void handle(SyncAllNicknamesPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            if (!AutorizacaoServidor.isAutorizado()) return;
            ClientNicknameHandler.clear();
            for (Map.Entry<UUID, NickEntry> entry : packet.entries.entrySet()) {
                ClientNicknameHandler.setNickname(
                        entry.getKey(),
                        entry.getValue().nickname,
                        entry.getValue().colors
                );
            }
        });
        ctx.setPacketHandled(true);
    }

    public record NickEntry(String nickname, String colors) {}
}
