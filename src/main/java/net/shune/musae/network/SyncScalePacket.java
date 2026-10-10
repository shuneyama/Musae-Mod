package net.shune.musae.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.shune.musae.auth.AutorizacaoServidor;
import net.shune.musae.client.ClientScaleHandler;

import java.util.UUID;
import java.util.function.Supplier;

public class SyncScalePacket {

    private final UUID playerUUID;
    private final float scale;

    public SyncScalePacket(UUID playerUUID, float scale) {
        this.playerUUID = playerUUID;
        this.scale = scale;
    }

    public static void encode(SyncScalePacket packet, FriendlyByteBuf buf) {
        buf.writeUUID(packet.playerUUID);
        buf.writeFloat(packet.scale);
    }

    public static SyncScalePacket decode(FriendlyByteBuf buf) {
        return new SyncScalePacket(buf.readUUID(), buf.readFloat());
    }

    public static void handle(SyncScalePacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            if (!AutorizacaoServidor.isAutorizado()) return;
            ClientScaleHandler.applyScale(packet.playerUUID, packet.scale);
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    ClientScaleHandler.applyScaleToRenderedPlayer(packet.playerUUID, packet.scale));
        });
        ctx.setPacketHandled(true);
    }
}
