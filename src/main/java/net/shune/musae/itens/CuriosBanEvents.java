package net.shune.musae.itens;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.shune.musae.MusaeMod;
import net.shune.musae.auth.AutorizacaoServidor;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.event.CurioChangeEvent;

@Mod.EventBusSubscriber(modid = MusaeMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CuriosBanEvents {

    public static boolean isCuriosCarregado() {
        return ModList.get().isLoaded("curios");
    }

    @SubscribeEvent
    public static void onCurioChange(CurioChangeEvent event) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack para = event.getTo();
        if (para.isEmpty()) return;

        if (ItensBanidosUtil.isBanido(player, para)) {
            player.getServer().execute(() -> CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
                String slotId = event.getIdentifier();
                int slotIndex = event.getSlotIndex();
                handler.getStacksHandler(slotId).ifPresent(stacksHandler -> {
                    ItemStack atual = stacksHandler.getStacks().getStackInSlot(slotIndex);
                    if (!atual.isEmpty() && ItensBanidosUtil.isBanido(player, atual)) {
                        stacksHandler.getStacks().setStackInSlot(slotIndex, ItemStack.EMPTY);
                        if (!player.getInventory().add(atual)) {
                            player.drop(atual, false);
                        }
                        player.sendSystemMessage(Component.literal("Você não pode equipar esse item.")
                                .withStyle(ChatFormatting.RED));
                    }
                });
            }));
        }
    }
}
