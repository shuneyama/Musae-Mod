package net.shune.musae.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.shune.musae.auth.AutorizacaoServidor;
import net.shune.musae.chat.BalaoDebug;
import net.shune.musae.chat.BalaoStyle;
import net.shune.musae.client.BalaoClientManager;

import java.util.UUID;
import java.util.function.Supplier;

public class BalaoPacket {

    private final UUID jogadorUUID;
    private final String mensagem;
    private final int corTexto;
    private final int corFundo;
    private final int corBorda;
    private final float altura;
    private final BalaoStyle estilo;

    public BalaoPacket(UUID jogadorUUID, String mensagem, int corTexto, int corFundo, int corBorda, float altura, BalaoStyle estilo) {
        this.jogadorUUID = jogadorUUID;
        this.mensagem = mensagem;
        this.corTexto = corTexto;
        this.corFundo = corFundo;
        this.corBorda = corBorda;
        this.altura = altura;
        this.estilo = estilo;
    }

    public static void encode(BalaoPacket packet, FriendlyByteBuf buf) {
        buf.writeUUID(packet.jogadorUUID);
        buf.writeUtf(packet.mensagem);
        buf.writeInt(packet.corTexto);
        buf.writeInt(packet.corFundo);
        buf.writeInt(packet.corBorda);
        buf.writeFloat(packet.altura);
        buf.writeUtf(packet.estilo.name());
    }

    public static BalaoPacket decode(FriendlyByteBuf buf) {
        return new BalaoPacket(
                buf.readUUID(),
                buf.readUtf(),
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readFloat(),
                BalaoStyle.valueOf(buf.readUtf())
        );
    }

    public static void handle(BalaoPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            if (!AutorizacaoServidor.isAutorizado()) return;
            BalaoDebug.log("Pacote recebido no cliente: jogador=" + packet.jogadorUUID + " mensagem=\"" + packet.mensagem + "\"");
            BalaoClientManager.criarBalao(packet.jogadorUUID, packet.mensagem, net.shune.musae.chat.BalaoConfig.BALLOON_DURATION_TICKS,
                    packet.corTexto, packet.corFundo, packet.corBorda, packet.altura, packet.estilo);
        });
        ctx.setPacketHandled(true);
    }
}
