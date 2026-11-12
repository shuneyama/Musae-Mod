package net.shune.musae.client;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.shune.musae.MusaeMod;
import net.shune.musae.auth.AutorizacaoServidor;

@Mod.EventBusSubscriber(modid = MusaeMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ItensBanidosTooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem());
        if (itemId == null) return;

        String id = itemId.toString();
        if (!ItensBanidosClientCache.isBanido(id)) return;

        if (ItensBanidosClientCache.isPermitido(id)) {
            event.getToolTip().add(Component.literal("Você tem permissão para usar esse item.")
                    .withStyle(ChatFormatting.GREEN));
        } else {
            event.getToolTip().add(Component.literal("Você não tem permissão para usar esse item.")
                    .withStyle(ChatFormatting.RED));
        }
    }
}
