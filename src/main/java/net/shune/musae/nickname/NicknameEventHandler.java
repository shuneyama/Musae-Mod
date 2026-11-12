package net.shune.musae.nickname;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.shune.musae.MusaeMod;
import net.shune.musae.auth.AutorizacaoServidor;
import net.shune.musae.race.RaceEmoji;

@Mod.EventBusSubscriber(modid = MusaeMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class NicknameEventHandler {

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        if (event.getEntity() instanceof ServerPlayer player && player.getServer() != null) {
            player.getServer().execute(() -> {
                NicknameData data = NicknameData.get(player.getServer());

                data.syncToPlayer(player);
                net.shune.musae.compatibility.TabModCompat.syncToTab(player, data);

                for (ServerPlayer online : player.getServer().getPlayerList().getPlayers()) {
                    if (data.getNickname(online.getUUID()) != null || !RaceEmoji.of(online).isEmpty()) {
                        online.refreshTabListName();
                        var packet = new net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket(
                                net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME,
                                online
                        );
                        player.connection.send(packet);
                    }
                }

                RaceEmoji.sendAllTo(player);
                RaceEmoji.refresh(player);
            });
        }
    }

    @SubscribeEvent
    public static void onTabListName(PlayerEvent.TabListNameFormat event) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        if (event.getEntity() instanceof ServerPlayer player && player.getServer() != null) {
            NicknameData data = NicknameData.get(player.getServer());
            MutableComponent styled = data.getStyledNickname(player.getUUID());
            String emoji = RaceEmoji.of(player);
            if (styled == null && emoji.isEmpty()) return;
            Component base = styled != null ? styled
                    : (event.getDisplayName() != null ? event.getDisplayName()
                    : Component.literal(player.getGameProfile().getName()));
            event.setDisplayName(RaceEmoji.prefix(emoji, base));
        }
    }

    @SubscribeEvent
    public static void onNameFormat(PlayerEvent.NameFormat event) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        if (event.getEntity() instanceof ServerPlayer player && player.getServer() != null) {
            NicknameData data = NicknameData.get(player.getServer());
            MutableComponent styled = data.getStyledNickname(player.getUUID());
            String emoji = RaceEmoji.of(player);
            if (styled == null && emoji.isEmpty()) return;
            Component base = styled != null ? styled : event.getDisplayname();
            event.setDisplayname(RaceEmoji.prefix(emoji, base));
        }
    }
}