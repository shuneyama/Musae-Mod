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

/**
 * Event handler para efeitos dos anéis que não podem ser feitos via attribute modifiers.
 *
 * - Anel dos Ferreiros: aplica Haste I enquanto equipado (via tick)
 * - Anel do Médico: boost de 50% no progresso de revive do PlayerRevive via tick
 */
@Mod.EventBusSubscriber(modid = MusaeMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RelicEventHandler {

    private static final int HASTE_DURATION = 1200;   // 60 segundos em ticks
    private static final int REFRESH_INTERVAL = 200;   // 10 segundos em ticks
    private static int tickCounter = 0;

    /**
     * Anel dos Ferreiros: aplica Haste I (60s) e renova a cada 10s.
     * Ao desequipar, o efeito é removido imediatamente.
     *
     * Anel do Médico: boost no progresso de revive do PlayerRevive.
     */
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
                // Aplica/renova Haste I por 60 segundos, sem partículas
                player.addEffect(new MobEffectInstance(
                        MobEffects.DIG_SPEED,
                        HASTE_DURATION,
                        0,
                        true,
                        false,
                        true
                ));
            } else if (!hasBlacksmith && player.hasEffect(MobEffects.DIG_SPEED)) {
                // Remove Haste se o anel foi desequipado
                MobEffectInstance current = player.getEffect(MobEffects.DIG_SPEED);
                if (current != null && current.isAmbient()) {
                    player.removeEffect(MobEffects.DIG_SPEED);
                }
            }
        }

        // --- Anel do Médico ---
        if (!ModList.get().isLoaded("playerrevive")) return;
        if (!ModList.get().isLoaded("curios")) return;

        try {
            PlayerReviveCompat.tickReviveBoost(event);
        } catch (NoClassDefFoundError ignored) {
            // PlayerRevive não está presente
        }
    }

    /**
     * Verifica se o jogador tem um anel de tipo específico equipado via Curios.
     */
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