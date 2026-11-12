package net.shune.musae.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.shune.musae.auth.AutorizacaoServidor;

import java.util.function.Supplier;

public class OpenSeletorPacket {

    public OpenSeletorPacket() {}

    public static void encode(OpenSeletorPacket packet, FriendlyByteBuf buf) {}

    public static OpenSeletorPacket decode(FriendlyByteBuf buf) {
        return new OpenSeletorPacket();
    }

    public static void handle(OpenSeletorPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            if (!AutorizacaoServidor.isAutorizado()) return;
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> net.shune.musae.client.screen.SeletorScreenOpener::abrir);
        });
        ctx.setPacketHandled(true);
    }
}