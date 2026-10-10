package net.shune.musae.util;

import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.shune.musae.MusaeMod;
import net.shune.musae.auth.AutorizacaoServidor;

@Mod.EventBusSubscriber(modid = MusaeMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerSizeHandler {

    @SubscribeEvent
    public static void onEntitySize(EntityEvent.Size event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!AutorizacaoServidor.isAutorizado()) return;

        float scale = PlayerScaleData.getScale(player);
        if (Math.abs(scale - 1.0f) <= 0.001f) return;

        Pose pose = event.getPose();
        EntityDimensions dimensoes = PlayerScaleData.dimensoes(pose, scale);
        event.setNewSize(dimensoes, false);
        event.setNewEyeHeight(PlayerScaleData.alturaOlhos(pose, scale));
    }
}
