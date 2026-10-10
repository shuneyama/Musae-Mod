package net.shune.musae.beverage;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.shune.musae.MusaeMod;
import net.shune.musae.auth.AutorizacaoServidor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = MusaeMod.MODID)
public class DrunkennessHandler {

    private static final long TIMEOUT_TICKS = 20L * 60L * 5L;

    private static final int DRUNK_DURATION_TICKS = 20 * 30;

    private static final Map<UUID, Counter> COUNTERS = new HashMap<>();
    private static final Map<UUID, Long> DRUNK_UNTIL = new HashMap<>();

    private static class Counter {
        int count;
        long lastDrinkTick;
        Counter(int count, long tick) { this.count = count; this.lastDrinkTick = tick; }
    }

    public static void onDrink(Player player, BeverageType type) {
        if (player.level().isClientSide()) return;
        long now = player.level().getGameTime();
        UUID id = player.getUUID();

        Counter c = COUNTERS.computeIfAbsent(id, k -> new Counter(0, now));
        c.count++;
        c.lastDrinkTick = now;

        if (c.count >= type.drunkThreshold) {
            applyDrunk(player, now);
            c.count = 0;
        }
    }

    private static void applyDrunk(Player player, long now) {
        DRUNK_UNTIL.put(player.getUUID(), now + DRUNK_DURATION_TICKS);
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, DRUNK_DURATION_TICKS, 0, false, true, false));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, DRUNK_DURATION_TICKS, 1, false, true, false));
        player.addEffect(new MobEffectInstance(ModEffects.BEBADO.get(), DRUNK_DURATION_TICKS, 0, false, false, true));
    }

    public static boolean isDrunk(Player player) {
        Long until = DRUNK_UNTIL.get(player.getUUID());
        if (until == null) return false;
        return player.level().getGameTime() < until;
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        if (event.phase != TickEvent.Phase.END) return;
        if (event.getServer().getTickCount() % 20 != 0) return;

        long now = event.getServer().overworld().getGameTime();
        Iterator<Map.Entry<UUID, Counter>> it = COUNTERS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Counter> e = it.next();
            if (now - e.getValue().lastDrinkTick > TIMEOUT_TICKS) {
                it.remove();
            }
        }

        Iterator<Map.Entry<UUID, Long>> dit = DRUNK_UNTIL.entrySet().iterator();
        while (dit.hasNext()) {
            Map.Entry<UUID, Long> e = dit.next();
            if (now > e.getValue()) {
                dit.remove();
                MusaeMod.LOGGER.debug("[MusaeMod] Drunk effect expired for {}", e.getKey());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        COUNTERS.remove(event.getEntity().getUUID());
    }
}
