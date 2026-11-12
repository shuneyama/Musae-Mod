package net.shune.musae.compatibility;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.shune.musae.auth.AutorizacaoServidor;
import net.shune.musae.item.RelicCompatHandler;
import team.creative.playerrevive.PlayerRevive;
import team.creative.playerrevive.api.IBleeding;
import team.creative.playerrevive.server.PlayerReviveServer;

public class PlayerReviveCompat {

    public static void tickReviveBoost(TickEvent.ServerTickEvent event) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            IBleeding bleeding = PlayerReviveServer.getBleeding(player);
            if (!bleeding.isBleeding()) continue;

            float extraProgress = 0;
            for (Player helper : bleeding.revivingPlayers()) {
                if (RelicCompatHandler.hasRelicMedic(helper)) {
                    extraProgress += PlayerRevive.CONFIG.revive.progressPerPlayer * 0.5f;
                }
            }

            if (extraProgress > 0) {
                try {
                    var field = bleeding.getClass().getDeclaredField("progress");
                    field.setAccessible(true);
                    float current = field.getFloat(bleeding);
                    field.setFloat(bleeding, current + extraProgress);
                } catch (Exception ignored) {}
            }
        }
    }
}
