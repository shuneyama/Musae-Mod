package net.shune.musae.comandos;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(modid = net.shune.musae.MusaeMod.MODID)
public final class ComandosEvents {
    private static Boolean modAntigoPresente;

    public static boolean ativo() {
        if (modAntigoPresente == null) {
            net.minecraftforge.fml.ModList lista = net.minecraftforge.fml.ModList.get();
            if (lista == null) return true;
            modAntigoPresente = lista.isLoaded("musaebackpack");
        }
        return !modAntigoPresente;
    }

    private ComandosEvents() {
    }

    @SubscribeEvent
    public static void aoTick(TickEvent.ServerTickEvent event) {
        if (!ativo()) return;
        if (event.phase != TickEvent.Phase.END) return;
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null || server.getTickCount() % 20 != 0) return;
        try {
            Canon.tick(server, System.currentTimeMillis());
        } catch (Exception e) {
            net.shune.musae.MusaeMod.LOGGER.error("[musae] erro no tick", e);
        }
    }

    @SubscribeEvent
    public static void aoMorrer(LivingDeathEvent event) {
        if (!ativo()) return;
        try {
            Canon.aoMorrer(event);
        } catch (Exception e) {
            net.shune.musae.MusaeMod.LOGGER.error("[musae] erro ao registrar morte", e);
        }
    }

    @SubscribeEvent
    public static void aoEntrar(PlayerEvent.PlayerLoggedInEvent event) {
        if (!ativo()) return;
        if (event.getEntity() instanceof ServerPlayer p) {
            Canon.aoEntrar(p);
        }
    }

    @SubscribeEvent
    public static void aoParar(ServerStoppedEvent event) {
        if (!ativo()) return;
        Canon.limparMemoria();
        Evento.limparMemoria();
        Help.limparMemoria();
        Teams.limparCache();
    }
}
