package net.shune.musae.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.scores.Team;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.shune.musae.MusaeMod;
import net.shune.musae.auth.AutorizacaoServidor;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = MusaeMod.MODID, value = Dist.CLIENT)
public class ClientRenderEvents {

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderLivingEvent.Pre<?, ?> event) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        if (!(event.getEntity() instanceof AbstractClientPlayer player)) return;
        float scale = ClientScaleHandler.getScale(player.getUUID());
        if (Math.abs(scale - 1.0f) > 0.001f) {
            PoseStack poseStack = event.getPoseStack();
            poseStack.pushPose();
            poseStack.scale(scale, scale, scale);
        }
    }

    @SubscribeEvent
    public static void onRenderPlayerPost(RenderLivingEvent.Post<?, ?> event) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        if (!(event.getEntity() instanceof AbstractClientPlayer player)) return;
        float scale = ClientScaleHandler.getScale(player.getUUID());
        if (Math.abs(scale - 1.0f) > 0.001f) {
            event.getPoseStack().popPose();
        }
    }

    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        if (!(event.getEntity() instanceof AbstractClientPlayer player)) return;

        float scale = ClientScaleHandler.getScale(player.getUUID());
        if (Math.abs(scale - 1.0f) <= 0.001f) return;

        float baseHeight = 1.8f;
        float currentLocalY = baseHeight * scale + 0.5f;
        float desiredLocalY = baseHeight + 0.5f / scale;
        float deltaY = desiredLocalY - currentLocalY;

        event.getPoseStack().translate(0.0f, deltaY, 0.0f);
    }

    /**
     * Reforca a visibilidade de nametag por time do vanilla (/team ... nametagVisibility)
     * com prioridade LOWEST, pra ter a ultima palavra mesmo se outro mod instalado no
     * servidor forcar RenderNameTagEvent.ALLOW pra jogadores com nome customizado
     * (padrao comum em mods de cosmetico/vanish), o que ignoraria o /team do vanilla.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderNameTagVisibility(RenderNameTagEvent event) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        if (event.getResult() == Event.Result.DENY) return;
        if (!(event.getEntity() instanceof AbstractClientPlayer player)) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer local = mc.player;
        if (local == null || player == local) return;

        Team team = player.getTeam();
        if (team == null) return;

        Team localTeam = local.getTeam();
        boolean flag = !player.isInvisibleTo(local);
        boolean shouldShow;
        switch (team.getNameTagVisibility()) {
            case ALWAYS -> shouldShow = flag;
            case NEVER -> shouldShow = false;
            case HIDE_FOR_OTHER_TEAMS -> shouldShow = localTeam == null
                    ? flag
                    : team.isAlliedTo(localTeam) && (team.canSeeFriendlyInvisibles() || flag);
            case HIDE_FOR_OWN_TEAM -> shouldShow = localTeam == null
                    ? flag
                    : !team.isAlliedTo(localTeam) && flag;
            default -> shouldShow = true;
        }

        if (!shouldShow) {
            event.setResult(Event.Result.DENY);
        }
    }
}