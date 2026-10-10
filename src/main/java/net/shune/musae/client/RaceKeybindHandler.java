package net.shune.musae.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.shune.musae.MusaeMod;
import net.shune.musae.auth.AutorizacaoServidor;
import net.shune.musae.network.NetworkHandler;
import net.shune.musae.network.RaceAbilityPacket;
import net.shune.musae.race.RaceType;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = MusaeMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RaceKeybindHandler {

    public static KeyMapping ABILITY_PRIMARY;
    public static KeyMapping ABILITY_SECONDARY;

    public static void registerKeybinds(RegisterKeyMappingsEvent event) {
        ABILITY_PRIMARY = new KeyMapping(
                "key.musae.race_ability",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "key.categories.musae");
        ABILITY_SECONDARY = new KeyMapping(
                "key.musae.race_ability_secondary",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "key.categories.musae");
        event.register(ABILITY_PRIMARY);
        event.register(ABILITY_SECONDARY);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        RaceType race = ClientRaceHandler.getRace();
        if (race == RaceType.NENHUMA || race == RaceType.HUMANO) return;

        if (ABILITY_PRIMARY != null && ABILITY_PRIMARY.consumeClick()) {
            if (race == RaceType.ANJO && mc.player.isShiftKeyDown()) {
                NetworkHandler.CHANNEL.sendToServer(new RaceAbilityPacket(RaceAbilityPacket.AbilityType.SECONDARY));
            } else {
                NetworkHandler.CHANNEL.sendToServer(new RaceAbilityPacket(RaceAbilityPacket.AbilityType.PRIMARY));
            }
        }

        if (ABILITY_SECONDARY != null && ABILITY_SECONDARY.consumeClick()) {
            if (race == RaceType.ANJO) {
                NetworkHandler.CHANNEL.sendToServer(new RaceAbilityPacket(RaceAbilityPacket.AbilityType.SECONDARY));
            }
        }
    }
}
