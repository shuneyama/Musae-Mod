package net.shune.musae.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.shune.musae.MusaeMod;
import net.shune.musae.auth.AutorizacaoServidor;
import net.shune.musae.compatibility.PlayerReviveCompat;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.List;

@Mod.EventBusSubscriber(modid = MusaeMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RelicEventHandler {

    private static final int HASTE_DURATION = 1200;
    private static final int REFRESH_INTERVAL = 200;
    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        if (event.phase != TickEvent.Phase.END) return;

        var server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        tickCounter++;
        boolean isRefreshTick = tickCounter >= REFRESH_INTERVAL;
        if (isRefreshTick) tickCounter = 0;

        for (Player player : server.getPlayerList().getPlayers()) {
            boolean hasBlacksmith = hasRingEquipped(player, RelicRingItem.RingType.BLACKSMITH);

            if (hasBlacksmith && isRefreshTick) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.DIG_SPEED,
                        HASTE_DURATION,
                        0,
                        true,
                        false,
                        true
                ));
            } else if (!hasBlacksmith && player.hasEffect(MobEffects.DIG_SPEED)) {
                MobEffectInstance current = player.getEffect(MobEffects.DIG_SPEED);
                if (current != null && current.isAmbient()) {
                    player.removeEffect(MobEffects.DIG_SPEED);
                }
            }
        }

        if (!ModList.get().isLoaded("playerrevive")) return;
        if (!ModList.get().isLoaded("curios")) return;

        try {
            PlayerReviveCompat.tickReviveBoost(event);
        } catch (NoClassDefFoundError ignored) {
        }
    }

    public static boolean hasRingEquipped(Player player, RelicRingItem.RingType type) {
        if (!ModList.get().isLoaded("curios")) return false;

        try {
            return CuriosApi.getCuriosInventory(player).resolve().map(handler -> {
                List<SlotResult> results = handler.findCurios(stack ->
                        stack.getItem() instanceof RelicRingItem ring
                                && ring.getRingType() == type);
                return !results.isEmpty();
            }).orElse(false);
        } catch (Exception e) {
            return false;
        }
    }
}