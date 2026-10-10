package net.shune.musae.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.shune.musae.MusaeMod;
import net.shune.musae.auth.AutorizacaoServidor;
import net.shune.musae.race.RaceConfig;
import net.shune.musae.race.RaceType;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = MusaeMod.MODID, value = Dist.CLIENT)
public class RaceHudOverlay {

    private static final ResourceLocation ICONS = ResourceLocation.fromNamespaceAndPath(MusaeMod.MODID, "textures/gui/race_icons.png");

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        RaceType race = ClientRaceHandler.getRace();
        if (race != RaceType.ANJO) return;

        GuiGraphics graphics = event.getGuiGraphics();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int maxCharges = RaceConfig.ANJO_MAX_CHARGES.get();
        int currentCharges = ClientRaceHandler.getAnjoCharges();

        int orbSize = 9;
        int spacing = 1;
        int totalWidth = maxCharges * (orbSize + spacing) - spacing;

        int x = screenWidth / 2 - 91;
        int y = screenHeight - 49 - 10;

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        for (int i = 0; i < maxCharges; i++) {
            int orbX = x + i * (orbSize + spacing);
            boolean filled = i < currentCharges;
            boolean pulsing = currentCharges == 0 && (System.currentTimeMillis() / 500) % 2 == 0;

            if (filled) {
                graphics.blit(ICONS, orbX, y, 0, 0, orbSize, orbSize, 32, 32);
            } else if (pulsing) {
                RenderSystem.setShaderColor(1.0F, 0.3F, 0.3F, 1.0F);
                graphics.blit(ICONS, orbX, y, 9, 0, orbSize, orbSize, 32, 32);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            } else {
                graphics.blit(ICONS, orbX, y, 9, 0, orbSize, orbSize, 32, 32);
            }
        }
    }
}
