package net.shune.musae.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.shune.musae.auth.AutorizacaoServidor;

import java.util.function.Supplier;

public class SyncAutorizacaoPacket {

    private final boolean autorizado;

    public SyncAutorizacaoPacket(boolean autorizado) {
        this.autorizado = autorizado;
    }

    public static void encode(SyncAutorizacaoPacket packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.autorizado);
    }

    public static SyncAutorizacaoPacket decode(FriendlyByteBuf buf) {
        return new SyncAutorizacaoPacket(buf.readBoolean());
    }

    public static void handle(SyncAutorizacaoPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> AutorizacaoServidor.aplicarEstadoRecebidoDoServidor(packet.autorizado));
        ctx.setPacketHandled(true);
    }
}
