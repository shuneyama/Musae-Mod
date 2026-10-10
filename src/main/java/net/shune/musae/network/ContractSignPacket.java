package net.shune.musae.network;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.shune.musae.auth.AutorizacaoServidor;
import net.shune.musae.contract.ContractBookItem;

import java.util.function.Supplier;

public class ContractSignPacket {

    public enum SignAction {
        AUTHOR, COSIGNER
    }

    private final InteractionHand hand;
    private final SignAction action;

    public ContractSignPacket(InteractionHand hand, SignAction action) {
        this.hand = hand;
        this.action = action;
    }

    public static void encode(ContractSignPacket pkt, FriendlyByteBuf buf) {
        buf.writeEnum(pkt.hand);
        buf.writeEnum(pkt.action);
    }

    public static ContractSignPacket decode(FriendlyByteBuf buf) {
        return new ContractSignPacket(buf.readEnum(InteractionHand.class), buf.readEnum(SignAction.class));
    }

    public static void handle(ContractSignPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (!AutorizacaoServidor.isAutorizado()) return;
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            ItemStack stack = player.getItemInHand(pkt.hand);
            if (!(stack.getItem() instanceof ContractBookItem)) return;

            if (pkt.action == SignAction.AUTHOR) {
                ContractBookItem.signAsAuthor(stack, player);
                player.sendSystemMessage(Component.translatable("item.musae.contract_book.msg_signed")
                        .withStyle(ChatFormatting.GOLD));
            } else if (pkt.action == SignAction.COSIGNER) {
                boolean success = ContractBookItem.signAsCosigner(stack, player);
                if (success) {
                    ItemStack copy = ContractBookItem.createCopy(stack);
                    if (!player.getInventory().add(copy)) {
                        player.drop(copy, false);
                    }
                    player.sendSystemMessage(Component.translatable("item.musae.contract_book.msg_cosigned")
                            .withStyle(ChatFormatting.GREEN));
                    player.level().playSound(null,
                            player.getX(), player.getY(), player.getZ(),
                            net.minecraft.sounds.SoundEvents.BEACON_ACTIVATE,
                            net.minecraft.sounds.SoundSource.PLAYERS,
                            0.7f, 1.0f);
                } else {
                    player.sendSystemMessage(Component.translatable("item.musae.contract_book.msg_cannot_cosign")
                            .withStyle(ChatFormatting.RED));
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
