package net.shune.musae.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.shune.musae.auth.AutorizacaoServidor;

import java.util.function.Supplier;

public class SyncRacePacket {

    private final String raceId;
    private final int cooldown;
    private final int anjoCharges;
    private final boolean iraAtiva;
    private final boolean venenoAtivo;
    private final boolean espiritoAtivo;
    private final boolean hasRaceElytra;

    public SyncRacePacket(String raceId, int cooldown, int anjoCharges,
                          boolean iraAtiva, boolean venenoAtivo, boolean espiritoAtivo,
                          boolean hasRaceElytra) {
        this.raceId = raceId;
        this.cooldown = cooldown;
        this.anjoCharges = anjoCharges;
        this.iraAtiva = iraAtiva;
        this.venenoAtivo = venenoAtivo;
        this.espiritoAtivo = espiritoAtivo;
        this.hasRaceElytra = hasRaceElytra;
    }

    public static void encode(SyncRacePacket pkt, FriendlyByteBuf buf) {
        buf.writeUtf(pkt.raceId);
        buf.writeVarInt(pkt.cooldown);
        buf.writeVarInt(pkt.anjoCharges);
        buf.writeBoolean(pkt.iraAtiva);
        buf.writeBoolean(pkt.venenoAtivo);
        buf.writeBoolean(pkt.espiritoAtivo);
        buf.writeBoolean(pkt.hasRaceElytra);
    }

    public static SyncRacePacket decode(FriendlyByteBuf buf) {
        return new SyncRacePacket(
                buf.readUtf(), buf.readVarInt(), buf.readVarInt(),
                buf.readBoolean(), buf.readBoolean(), buf.readBoolean(),
                buf.readBoolean());
    }

    public static void handle(SyncRacePacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (!AutorizacaoServidor.isAutorizado()) return;
            net.shune.musae.client.ClientRaceHandler.receive(
                    pkt.raceId, pkt.cooldown, pkt.anjoCharges,
                    pkt.iraAtiva, pkt.venenoAtivo, pkt.espiritoAtivo,
                    pkt.hasRaceElytra);
        });
        ctx.get().setPacketHandled(true);
    }
}