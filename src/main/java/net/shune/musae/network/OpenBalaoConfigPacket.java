package net.shune.musae.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.shune.musae.auth.AutorizacaoServidor;
import net.shune.musae.chat.BalaoStyle;

import java.util.function.Supplier;

public class OpenBalaoConfigPacket {

    private final int corTexto;
    private final int corFundo;
    private final int corBorda;
    private final float altura;
    private final BalaoStyle estilo;

    public OpenBalaoConfigPacket(int corTexto, int corFundo, int corBorda, float altura, BalaoStyle estilo) {
        this.corTexto = corTexto;
        this.corFundo = corFundo;
        this.corBorda = corBorda;
        this.altura = altura;
        this.estilo = estilo;
    }

    public static void encode(OpenBalaoConfigPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.corTexto);
        buf.writeInt(packet.corFundo);
        buf.writeInt(packet.corBorda);
        buf.writeFloat(packet.altura);
        buf.writeUtf(packet.estilo.name());
    }

    public static OpenBalaoConfigPacket decode(FriendlyByteBuf buf) {
        return new OpenBalaoConfigPacket(buf.readInt(), buf.readInt(), buf.readInt(), buf.readFloat(),
                BalaoStyle.valueOf(buf.readUtf()));
    }

    public static void handle(OpenBalaoConfigPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            if (!AutorizacaoServidor.isAutorizado()) return;
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    net.shune.musae.client.BalaoConfigScreen.abrir(
                            packet.corTexto, packet.corFundo, packet.corBorda, packet.altura, packet.estilo));
        });
        ctx.setPacketHandled(true);
    }
}
