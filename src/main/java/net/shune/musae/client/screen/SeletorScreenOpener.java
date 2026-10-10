package net.shune.musae.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class SeletorScreenOpener {

    private SeletorScreenOpener() {
    }

    public static void abrir() {
        Minecraft.getInstance().setScreen(new SeletorScreen());
    }
}
