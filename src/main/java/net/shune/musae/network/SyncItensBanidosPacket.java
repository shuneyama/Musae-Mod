package net.shune.musae.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.shune.musae.auth.AutorizacaoServidor;
import net.shune.musae.client.ItensBanidosClientCache;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SyncItensBanidosPacket {

    private final List<String> itens;

    public SyncItensBanidosPacket(List<String> itens) {
        this.itens = itens;
    }

    public static void encode(SyncItensBanidosPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.itens.size());
        for (String item : packet.itens) {
            buf.writeUtf(item);
        }
    }

    public static SyncItensBanidosPacket decode(FriendlyByteBuf buf) {
        int tamanho = buf.readInt();
        List<String> itens = new ArrayList<>(tamanho);
        for (int i = 0; i < tamanho; i++) {
            itens.add(buf.readUtf());
        }
        return new SyncItensBanidosPacket(itens);
    }

    public static void handle(SyncItensBanidosPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            if (!AutorizacaoServidor.isAutorizado()) return;
            ItensBanidosClientCache.setBanidos(packet.itens);
        });
        ctx.setPacketHandled(true);
    }
}
