package net.shune.musae.itens;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

public class ItensBanidosTickHandler {

    public static void tick(MinecraftServer servidor) {
        boolean curiosCarregado = CuriosBanEvents.isCuriosCarregado();

        for (ServerPlayer player : servidor.getPlayerList().getPlayers()) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (slot.getType() != EquipmentSlot.Type.ARMOR && slot != EquipmentSlot.OFFHAND) continue;

                ItemStack equipado = player.getItemBySlot(slot);
                if (!equipado.isEmpty() && ItensBanidosUtil.isBanido(player, equipado)) {
                    player.setItemSlot(slot, ItemStack.EMPTY);
                    if (!player.getInventory().add(equipado)) {
                        player.drop(equipado, false);
                    }
                    player.sendSystemMessage(Component.literal("Você não pode utilizar esse item.")
                            .withStyle(ChatFormatting.RED));
                }
            }

            if (curiosCarregado) {
                CuriosApi.getCuriosInventory(player).ifPresent(handler ->
                        handler.getCurios().forEach((slotId, stacksHandler) -> {
                            var stacks = stacksHandler.getStacks();
                            for (int i = 0; i < stacks.getSlots(); i++) {
                                ItemStack atual = stacks.getStackInSlot(i);
                                if (!atual.isEmpty() && ItensBanidosUtil.isBanido(player, atual)) {
                                    stacks.setStackInSlot(i, ItemStack.EMPTY);
                                    if (!player.getInventory().add(atual)) {
                                        player.drop(atual, false);
                                    }
                                    player.sendSystemMessage(Component.literal("Você não pode utilizar esse item.")
                                            .withStyle(ChatFormatting.RED));
                                }
                            }
                        }));
            }
        }
    }
}
