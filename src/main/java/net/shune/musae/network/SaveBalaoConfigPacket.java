package net.shune.musae.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.shune.musae.auth.AutorizacaoServidor;
import net.shune.musae.chat.BalaoStyle;
import net.shune.musae.chat.PlayerBalaoData;

import java.util.function.Supplier;

public class SaveBalaoConfigPacket {

    private final int corTexto;
    private final int corFundo;
    private final int corBorda;
    private final float altura;
    private final BalaoStyle estilo;

    public SaveBalaoConfigPacket(int corTexto, int corFundo, int corBorda, float altura, BalaoStyle estilo) {
        this.corTexto = corTexto;
        this.corFundo = corFundo;
        this.corBorda = corBorda;
        this.altura = altura;
        this.estilo = estilo;
    }

    public static void encode(SaveBalaoConfigPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.corTexto);
        buf.writeInt(packet.corFundo);
        buf.writeInt(packet.corBorda);
        buf.writeFloat(packet.altura);
        buf.writeUtf(packet.estilo.name());
    }

    public static SaveBalaoConfigPacket decode(FriendlyByteBuf buf) {
        return new SaveBalaoConfigPacket(buf.readInt(), buf.readInt(), buf.readInt(), buf.readFloat(),
                BalaoStyle.valueOf(buf.readUtf()));
    }

    public static void handle(SaveBalaoConfigPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ServerPlayer player = ctx.getSender();
        ctx.enqueueWork(() -> {
            if (!AutorizacaoServidor.isAutorizado()) return;
            if (player == null || player.getServer() == null) return;

            PlayerBalaoData data = PlayerBalaoData.get(player.getServer(), player.getUUID());
            data.corTexto = packet.corTexto;
            data.corFundo = packet.corFundo;
            data.corBorda = packet.corBorda;
            data.altura = packet.altura;
            data.estilo = packet.estilo;
            PlayerBalaoData.salvar(player.getServer(), player.getUUID());
        });
        ctx.setPacketHandled(true);
    }
}
