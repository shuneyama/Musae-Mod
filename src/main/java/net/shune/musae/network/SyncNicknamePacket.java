package net.shune.musae.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.shune.musae.auth.AutorizacaoServidor;
import net.shune.musae.client.ClientNicknameHandler;

import java.util.UUID;
import java.util.function.Supplier;

public class SyncNicknamePacket {

    private final UUID playerUUID;
    private final String nickname;
    private final String colors;

    public SyncNicknamePacket(UUID playerUUID, String nickname, String colors) {
        this.playerUUID = playerUUID;
        this.nickname = nickname;
        this.colors = colors;
    }

    public static void encode(SyncNicknamePacket packet, FriendlyByteBuf buf) {
        buf.writeUUID(packet.playerUUID);
        buf.writeUtf(packet.nickname);
        buf.writeUtf(packet.colors);
    }

    public static SyncNicknamePacket decode(FriendlyByteBuf buf) {
        return new SyncNicknamePacket(
                buf.readUUID(),
                buf.readUtf(),
                buf.readUtf()
        );
    }

    public static void handle(SyncNicknamePacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            if (!AutorizacaoServidor.isAutorizado()) return;
            if (packet.nickname.isEmpty()) {
                ClientNicknameHandler.removeNickname(packet.playerUUID);
            } else {
                ClientNicknameHandler.setNickname(packet.playerUUID, packet.nickname, packet.colors);
            }
        });
        ctx.setPacketHandled(true);
    }
}
