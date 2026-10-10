package net.shune.musae.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.shune.musae.MusaeMod;

public class NetworkHandler {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(MusaeMod.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        CHANNEL.messageBuilder(SyncScalePacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncScalePacket::encode)
                .decoder(SyncScalePacket::decode)
                .consumerMainThread(SyncScalePacket::handle)
                .add();

        CHANNEL.messageBuilder(SyncNicknamePacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncNicknamePacket::encode)
                .decoder(SyncNicknamePacket::decode)
                .consumerMainThread(SyncNicknamePacket::handle)
                .add();

        CHANNEL.messageBuilder(SyncAllNicknamesPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncAllNicknamesPacket::encode)
                .decoder(SyncAllNicknamesPacket::decode)
                .consumerMainThread(SyncAllNicknamesPacket::handle)
                .add();

        CHANNEL.messageBuilder(ContractSignPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ContractSignPacket::encode)
                .decoder(ContractSignPacket::decode)
                .consumerMainThread(ContractSignPacket::handle)
                .add();

        CHANNEL.messageBuilder(ContractUpdatePagesPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ContractUpdatePagesPacket::encode)
                .decoder(ContractUpdatePagesPacket::decode)
                .consumerMainThread(ContractUpdatePagesPacket::handle)
                .add();

        CHANNEL.messageBuilder(SyncRacePacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncRacePacket::encode)
                .decoder(SyncRacePacket::decode)
                .consumerMainThread(SyncRacePacket::handle)
                .add();

        CHANNEL.messageBuilder(RaceAbilityPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(RaceAbilityPacket::encode)
                .decoder(RaceAbilityPacket::decode)
                .consumerMainThread(RaceAbilityPacket::handle)
                .add();

        CHANNEL.messageBuilder(OpenSeletorPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(OpenSeletorPacket::encode)
                .decoder(OpenSeletorPacket::decode)
                .consumerMainThread(OpenSeletorPacket::handle)
                .add();

        CHANNEL.messageBuilder(ApplyCharacterPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ApplyCharacterPacket::encode)
                .decoder(ApplyCharacterPacket::decode)
                .consumerMainThread(ApplyCharacterPacket::handle)
                .add();

        CHANNEL.messageBuilder(BalaoPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(BalaoPacket::encode)
                .decoder(BalaoPacket::decode)
                .consumerMainThread(BalaoPacket::handle)
                .add();

        CHANNEL.messageBuilder(OpenBalaoConfigPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(OpenBalaoConfigPacket::encode)
                .decoder(OpenBalaoConfigPacket::decode)
                .consumerMainThread(OpenBalaoConfigPacket::handle)
                .add();

        CHANNEL.messageBuilder(SaveBalaoConfigPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(SaveBalaoConfigPacket::encode)
                .decoder(SaveBalaoConfigPacket::decode)
                .consumerMainThread(SaveBalaoConfigPacket::handle)
                .add();

        CHANNEL.messageBuilder(SyncItensBanidosPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncItensBanidosPacket::encode)
                .decoder(SyncItensBanidosPacket::decode)
                .consumerMainThread(SyncItensBanidosPacket::handle)
                .add();

        CHANNEL.messageBuilder(SyncItensPermitidosPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncItensPermitidosPacket::encode)
                .decoder(SyncItensPermitidosPacket::decode)
                .consumerMainThread(SyncItensPermitidosPacket::handle)
                .add();

        CHANNEL.messageBuilder(SyncAutorizacaoPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncAutorizacaoPacket::encode)
                .decoder(SyncAutorizacaoPacket::decode)
                .consumerMainThread(SyncAutorizacaoPacket::handle)
                .add();
    }

    public static <T> void sendToPlayer(ServerPlayer player, T packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static <T> void sendToAll(T packet) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
    }

    public static <T> void sendToAllTracking(ServerPlayer player, T packet) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), packet);
    }
}