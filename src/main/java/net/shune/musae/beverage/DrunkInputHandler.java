package net.shune.musae.beverage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.shune.musae.MusaeMod;
import net.shune.musae.auth.AutorizacaoServidor;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = MusaeMod.MODID, value = Dist.CLIENT)
public class DrunkInputHandler {

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        if (player.hasEffect(ModEffects.BEBADO.get())) {
            var input = event.getInput();
            input.forwardImpulse = -input.forwardImpulse;
            input.leftImpulse = -input.leftImpulse;
            boolean up = input.up;
            input.up = input.down;
            input.down = up;
            boolean left = input.left;
            input.left = input.right;
            input.right = left;
        }
    }
}
