package net.shune.musae.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.shune.musae.MusaeMod;
import net.shune.musae.auth.AutorizacaoServidor;
import net.shune.musae.race.RaceType;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = MusaeMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class QuimeraAladaFlightHandler {

    private static boolean wasJumpDown = false;
    private static boolean jumpReleasedSinceFalling = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        long window = mc.getWindow().getWindow();
        int jumpKey = mc.options.keyJump.getKey().getValue();
        boolean jumpDown = jumpKey >= 0 && GLFW.glfwGetKey(window, jumpKey) == GLFW.GLFW_PRESS;

        boolean inAir = !player.onGround();
        boolean falling = player.getDeltaMovement().y < 0.0;
        boolean canTrigger = inAir
                && falling
                && !player.isFallFlying()
                && !player.isInWater()
                && !player.getAbilities().flying;

        if (!inAir) {
            jumpReleasedSinceFalling = false;
        } else if (!jumpDown) {
            jumpReleasedSinceFalling = true;
        }

        boolean justPressedJumpInAir = jumpDown && !wasJumpDown && canTrigger && jumpReleasedSinceFalling;

        if (justPressedJumpInAir
                && ClientRaceHandler.getRace() == RaceType.QUIMERA_ALADA
                && ClientRaceHandler.hasRaceElytra()
                && mc.getConnection() != null) {
            mc.getConnection().send(new ServerboundPlayerCommandPacket(
                    player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
        }

        wasJumpDown = jumpDown;
    }
}