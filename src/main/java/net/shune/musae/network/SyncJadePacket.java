package net.shune.musae.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.shune.musae.compatibility.JadeClientState;

import java.util.function.Supplier;

public class SyncJadePacket {

    private final boolean podeVerNicks;

    public SyncJadePacket(boolean podeVerNicks) {
        this.podeVerNicks = podeVerNicks;
    }

    public static void encode(SyncJadePacket packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.podeVerNicks);
    }

    public static SyncJadePacket decode(FriendlyByteBuf buf) {
        return new SyncJadePacket(buf.readBoolean());
    }

    public static void handle(SyncJadePacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> JadeClientState.definir(packet.podeVerNicks));
        ctx.setPacketHandled(true);
    }
}
