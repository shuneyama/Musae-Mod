package net.shune.musae.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.shune.musae.auth.AutorizacaoServidor;
import net.shune.musae.client.ClientFarejarHandler;

import java.util.function.Supplier;

public class FarejarPacket {

    private final int duracao;
    private final int[] ids;

    public FarejarPacket(int duracao, int[] ids) {
        this.duracao = duracao;
        this.ids = ids;
    }

    public static void encode(FarejarPacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.duracao);
        buf.writeVarIntArray(packet.ids);
    }

    public static FarejarPacket decode(FriendlyByteBuf buf) {
        return new FarejarPacket(buf.readVarInt(), buf.readVarIntArray());
    }

    public static void handle(FarejarPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            if (!AutorizacaoServidor.isAutorizado()) return;
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientFarejarHandler.marcar(packet.ids, packet.duracao));
        });
        ctx.setPacketHandled(true);
    }
}
