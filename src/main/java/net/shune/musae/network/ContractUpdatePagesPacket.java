package net.shune.musae.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.shune.musae.auth.AutorizacaoServidor;
import net.shune.musae.contract.ContractBookItem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ContractUpdatePagesPacket {

    private static final int MAX_PAGES = 20;
    private static final int MAX_PAGE_LENGTH = 256;

    private final InteractionHand hand;
    private final List<String> pages;

    public ContractUpdatePagesPacket(InteractionHand hand, List<String> pages) {
        this.hand = hand;
        this.pages = pages;
    }

    public static void encode(ContractUpdatePagesPacket pkt, FriendlyByteBuf buf) {
        buf.writeEnum(pkt.hand);
        buf.writeVarInt(Math.min(pkt.pages.size(), MAX_PAGES));
        for (int i = 0; i < Math.min(pkt.pages.size(), MAX_PAGES); i++) {
            String page = pkt.pages.get(i);
            if (page.length() > MAX_PAGE_LENGTH) {
                page = page.substring(0, MAX_PAGE_LENGTH);
            }
            buf.writeUtf(page, MAX_PAGE_LENGTH);
        }
    }

    public static ContractUpdatePagesPacket decode(FriendlyByteBuf buf) {
        InteractionHand hand = buf.readEnum(InteractionHand.class);
        int count = buf.readVarInt();
        count = Math.min(count, MAX_PAGES);
        List<String> pages = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            pages.add(buf.readUtf(MAX_PAGE_LENGTH));
        }
        return new ContractUpdatePagesPacket(hand, pages);
    }

    public static void handle(ContractUpdatePagesPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (!AutorizacaoServidor.isAutorizado()) return;
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            ItemStack stack = player.getItemInHand(pkt.hand);
            if (!(stack.getItem() instanceof ContractBookItem)) return;

            ContractBookItem.ContractState state = ContractBookItem.getState(stack);
            if (state != ContractBookItem.ContractState.BLANK) return;

            ContractBookItem.setPages(stack, pkt.pages);
        });
        ctx.get().setPacketHandled(true);
    }
}
