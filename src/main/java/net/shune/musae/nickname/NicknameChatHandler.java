package net.shune.musae.nickname;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.shune.musae.MusaeMod;
import net.shune.musae.auth.AutorizacaoServidor;
import net.shune.musae.chat.ChatEntrada;

@Mod.EventBusSubscriber(modid = MusaeMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class NicknameChatHandler {

    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        ServerPlayer player = event.getPlayer();
        if (player.getServer() == null) return;

        event.setCanceled(true);

        ChatEntrada.receber(player, event.getRawText());
    }
}
