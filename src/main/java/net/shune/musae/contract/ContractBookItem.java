package net.shune.musae.contract;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.shune.musae.auth.AutorizacaoServidor;
import net.shune.musae.nickname.NicknameData;

import javax.annotation.Nullable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ContractBookItem extends Item {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault());

    public ContractBookItem(Properties properties) {
        super(properties.fireResistant());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!AutorizacaoServidor.isAutorizado()) return InteractionResultHolder.pass(stack);

        if (level.isClientSide()) {
            ContractState state = getState(stack);
            List<String> pages = getPages(stack);
            CompoundTag contractData = getOrCreateContractData(stack);

            String authorUuid = contractData.contains("AuthorUUID") ? contractData.getString("AuthorUUID") : "";
            String authorName = contractData.contains("AuthorName") ? contractData.getString("AuthorName") : "";
            String cosignerUuid = contractData.contains("CosignerUUID") ? contractData.getString("CosignerUUID") : "";
            String cosignerName = contractData.contains("CosignerName") ? contractData.getString("CosignerName") : "";

            boolean isAuthor = player.getStringUUID().equals(authorUuid) || authorUuid.isEmpty();

            ContractBookScreen.open(pages, state, hand, isAuthor,
                    authorName, cosignerName,
                    contractData.contains("AuthorTimestamp") ? contractData.getString("AuthorTimestamp") : "",
                    contractData.contains("CosignerTimestamp") ? contractData.getString("CosignerTimestamp") : "");
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return getState(stack) == ContractState.SIGNED;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        ContractState state = getState(stack);
        CompoundTag data = getOrCreateContractData(stack);

        switch (state) {
            case BLANK -> tooltip.add(Component.translatable("item.musae.contract_book.blank")
                    .withStyle(ChatFormatting.GRAY));
            case PENDING -> {
                String authorDisplay = resolveDisplayName(data, "AuthorUUID", "AuthorName", level);
                tooltip.add(Component.translatable("item.musae.contract_book.pending")
                        .withStyle(ChatFormatting.YELLOW));
                tooltip.add(Component.translatable("item.musae.contract_book.author", authorDisplay)
                        .withStyle(ChatFormatting.GRAY));
            }
            case SIGNED -> {
                String authorDisplay = resolveDisplayName(data, "AuthorUUID", "AuthorName", level);
                String cosignerDisplay = resolveDisplayName(data, "CosignerUUID", "CosignerName", level);
                tooltip.add(Component.translatable("item.musae.contract_book.signed")
                        .withStyle(ChatFormatting.GREEN));
                tooltip.add(Component.translatable("item.musae.contract_book.author", authorDisplay)
                        .withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.translatable("item.musae.contract_book.cosigner", cosignerDisplay)
                        .withStyle(ChatFormatting.GRAY));
                if (data.contains("AuthorTimestamp")) {
                    tooltip.add(Component.translatable("item.musae.contract_book.date",
                            data.getString("AuthorTimestamp")).withStyle(ChatFormatting.DARK_GRAY));
                }
            }
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        ContractState state = getState(stack);
        MutableComponent name = Component.translatable(this.getDescriptionId(stack));
        return switch (state) {
            case BLANK -> name.withStyle(ChatFormatting.WHITE);
            case PENDING -> name.withStyle(ChatFormatting.YELLOW);
            case SIGNED -> name.withStyle(ChatFormatting.GREEN);
        };
    }

    private String resolveDisplayName(CompoundTag data, String uuidKey, String nameKey, @Nullable Level level) {
        String fallback = data.contains(nameKey) ? data.getString(nameKey) : "???";
        if (level == null || level.isClientSide()) {
            return fallback;
        }
        if (!data.contains(uuidKey)) return fallback;
        try {
            UUID uuid = UUID.fromString(data.getString(uuidKey));
            if (level.getServer() != null) {
                NicknameData nickData = NicknameData.get(level.getServer());
                NicknameData.NicknameEntry entry = nickData.getNickname(uuid);
                if (entry != null) {
                    return entry.nickname();
                }
            }
        } catch (Exception ignored) {
        }
        return fallback;
    }

    public static CompoundTag getOrCreateContractData(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains("ContractData", Tag.TAG_COMPOUND)) {
            CompoundTag data = new CompoundTag();
            data.putString("State", ContractState.BLANK.name());
            data.put("Pages", new ListTag());
            tag.put("ContractData", data);
        }
        return tag.getCompound("ContractData");
    }

    public static ContractState getState(ItemStack stack) {
        CompoundTag data = getOrCreateContractData(stack);
        try {
            return ContractState.valueOf(data.getString("State"));
        } catch (Exception e) {
            return ContractState.BLANK;
        }
    }

    public static List<String> getPages(ItemStack stack) {
        CompoundTag data = getOrCreateContractData(stack);
        ListTag pagesTag = data.getList("Pages", Tag.TAG_STRING);
        List<String> pages = new ArrayList<>();
        for (int i = 0; i < pagesTag.size(); i++) {
            pages.add(pagesTag.getString(i));
        }
        return pages;
    }

    public static void setPages(ItemStack stack, List<String> pages) {
        CompoundTag data = getOrCreateContractData(stack);
        ListTag pagesTag = new ListTag();
        for (String page : pages) {
            pagesTag.add(StringTag.valueOf(page));
        }
        data.put("Pages", pagesTag);
    }

    public static void signAsAuthor(ItemStack stack, ServerPlayer player) {
        CompoundTag data = getOrCreateContractData(stack);
        if (!getState(stack).equals(ContractState.BLANK)) return;

        data.putString("State", ContractState.PENDING.name());
        data.putString("AuthorUUID", player.getStringUUID());

        String displayName = resolvePlayerName(player);
        data.putString("AuthorName", displayName);
        data.putString("AuthorTimestamp", DATE_FORMAT.format(Instant.now()));
    }

    public static boolean signAsCosigner(ItemStack stack, ServerPlayer player) {
        CompoundTag data = getOrCreateContractData(stack);
        if (!getState(stack).equals(ContractState.PENDING)) return false;

        String authorUuid = data.getString("AuthorUUID");
        if (player.getStringUUID().equals(authorUuid)) return false;

        data.putString("State", ContractState.SIGNED.name());
        data.putString("CosignerUUID", player.getStringUUID());

        String displayName = resolvePlayerName(player);
        data.putString("CosignerName", displayName);
        data.putString("CosignerTimestamp", DATE_FORMAT.format(Instant.now()));
        return true;
    }

    private static String resolvePlayerName(ServerPlayer player) {
        if (player.getServer() != null) {
            NicknameData nickData = NicknameData.get(player.getServer());
            NicknameData.NicknameEntry entry = nickData.getNickname(player.getUUID());
            if (entry != null) {
                return entry.nickname();
            }
        }
        return player.getGameProfile().getName();
    }

    public static ItemStack createCopy(ItemStack original) {
        ItemStack copy = original.copy();
        return copy;
    }

    public enum ContractState {
        BLANK, PENDING, SIGNED
    }
}
