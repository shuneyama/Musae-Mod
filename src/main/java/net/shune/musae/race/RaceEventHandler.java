package net.shune.musae.race;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.shune.musae.MusaeMod;
import net.shune.musae.auth.AutorizacaoServidor;

@Mod.EventBusSubscriber(modid = MusaeMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RaceEventHandler {

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        if (event.getObject() instanceof Player) {
            event.addCapability(RaceCapability.ID, new RaceCapability.Provider());
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        event.getOriginal().reviveCaps();
        RaceData oldData = RaceCapability.resolve(event.getOriginal());
        RaceData newData = RaceCapability.resolve(event.getEntity());
        newData.copyFrom(oldData);
        event.getOriginal().invalidateCaps();
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getServer().execute(() -> RaceManager.reapplyOnRespawnOrJoin(player));
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getServer().execute(() -> RaceManager.reapplyOnRespawnOrJoin(player));
        }
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getServer().execute(() -> RaceManager.reapplyOnRespawnOrJoin(player));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingAttack(LivingAttackEvent event) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        RaceData data = RaceCapability.resolve(player);

        if (data.getRace() == RaceType.DEMONIO) {
            if (event.getSource().is(DamageTypes.IN_FIRE)
                    || event.getSource().is(DamageTypes.ON_FIRE)
                    || event.getSource().is(DamageTypes.LAVA)
                    || event.getSource().is(DamageTypes.HOT_FLOOR)) {
                event.setCanceled(true);
                player.clearFire();
                return;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        RaceData data = RaceCapability.resolve(player);

        if (data.getRace() == RaceType.DEMONIO) {
            if (event.getSource().is(DamageTypes.IN_FIRE)
                    || event.getSource().is(DamageTypes.ON_FIRE)
                    || event.getSource().is(DamageTypes.LAVA)
                    || event.getSource().is(DamageTypes.HOT_FLOOR)) {
                event.setCanceled(true);
                player.clearFire();
                return;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPotionApplicable(MobEffectEvent.Applicable event) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        RaceData data = RaceCapability.get(player).orElse(null);
        if (data == null || data.getRace() != RaceType.DEMONIO) return;

        MobEffect effect = event.getEffectInstance().getEffect();

        if (effect == MobEffects.WATER_BREATHING || effect == MobEffects.DOLPHINS_GRACE) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        RaceData data = RaceCapability.resolve(player);

        if (data.getRace() == RaceType.QUIMERA_INSETO && data.isVenenoProximoAtaque()) {
            if (event.getTarget() instanceof net.minecraft.world.entity.LivingEntity target) {
                target.addEffect(new MobEffectInstance(MobEffects.POISON,
                        RaceConfig.QUIMERA_INSETO_POISON_DURATION.get(), 1));
                data.setVenenoProximoAtaque(false);
                data.setCooldownTicks(RaceConfig.QUIMERA_INSETO_VENOM_COOLDOWN.get());
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("§2Veneno aplicado!"), true);
                RaceManager.syncToClient(player, data);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        Player player = event.getEntity();

        RaceType race;
        if (player.level().isClientSide()) {
            race = net.shune.musae.client.ClientRaceHandler.getRace();
        } else {
            RaceData data = RaceCapability.get(player).orElse(null);
            race = data == null ? null : data.getRace();
        }

        if (race != RaceType.AQUARIANO) return;
        if (!player.isEyeInFluid(net.minecraft.tags.FluidTags.WATER)) return;

        float speed = event.getNewSpeed();

        boolean hasAquaAffinity = net.minecraft.world.item.enchantment.EnchantmentHelper
                .hasAquaAffinity(player);
        if (!hasAquaAffinity) {
            speed *= 5.0f;
        }
        if (!player.onGround()) {
            speed *= 5.0f;
        }

        event.setNewSpeed(speed);
    }
}