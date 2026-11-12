package net.shune.musae.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.shune.musae.MusaeMod;
import net.shune.musae.auth.AutorizacaoServidor;
import net.shune.musae.chat.BalaoDebug;

import java.util.List;

@Mod.EventBusSubscriber(modid = MusaeMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class BalaoRenderHandler {

    public static boolean baloesVisiveis = true;

    private static long ultimoLogGeral = 0;

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        if (!baloesVisiveis) return;
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) return;

        PoseStack poseStack = event.getPoseStack();
        double camX = client.gameRenderer.getMainCamera().getPosition().x;
        double camY = client.gameRenderer.getMainCamera().getPosition().y;
        double camZ = client.gameRenderer.getMainCamera().getPosition().z;

        int jogadoresProcessados = 0;
        int jogadoresNoLevel = 0;

        for (Player player : client.level.players()) {
            jogadoresNoLevel++;
            if (!(player instanceof AbstractClientPlayer clientPlayer)) continue;

            List<BalaoClientManager.BalaoMensagem> mensagens = BalaoClientManager.getBaloes(clientPlayer.getUUID());
            if (mensagens.isEmpty()) continue;

            jogadoresProcessados++;

            double entityX = Mth.lerp(event.getPartialTick(), clientPlayer.xOld, clientPlayer.getX());
            double entityY = Mth.lerp(event.getPartialTick(), clientPlayer.yOld, clientPlayer.getY());
            double entityZ = Mth.lerp(event.getPartialTick(), clientPlayer.zOld, clientPlayer.getZ());

            poseStack.pushPose();
            poseStack.translate(entityX - camX, entityY - camY, entityZ - camZ);

            BalaoRenderer.renderBaloes(poseStack, client.getEntityRenderDispatcher(), client.font,
                    mensagens, clientPlayer.getBbHeight() + 0.5f);

            poseStack.popPose();
        }

        if (jogadoresProcessados > 0 && System.currentTimeMillis() - ultimoLogGeral > 2000) {
            ultimoLogGeral = System.currentTimeMillis();
            BalaoDebug.log("Renderizando balões de " + jogadoresProcessados + " jogador(es) neste frame.");
        } else if (jogadoresProcessados == 0 && BalaoDebug.isAtivo() && System.currentTimeMillis() - ultimoLogGeral > 2000) {
            ultimoLogGeral = System.currentTimeMillis();
            BalaoDebug.log("Nenhum balão ativo neste frame. Jogadores no level: " + jogadoresNoLevel);
        }
    }
}
