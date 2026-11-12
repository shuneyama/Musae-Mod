package net.shune.musae.race;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.shune.musae.MusaeMod;
import net.shune.musae.auth.AutorizacaoServidor;

import java.util.Set;

@Mod.EventBusSubscriber(modid = MusaeMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RaceTickHandler {

    private static final Set<Block> COLD_BLOCKS = Set.of(
            Blocks.SNOW, Blocks.SNOW_BLOCK, Blocks.POWDER_SNOW,
            Blocks.ICE, Blocks.PACKED_ICE, Blocks.BLUE_ICE, Blocks.FROSTED_ICE,
            Blocks.WHITE_CARPET
    );

    private static final TagKey<Biome> MINECRAFT_IS_COLD = TagKey.create(Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath("minecraft", "is_cold"));
    private static final TagKey<Biome> FORGE_IS_COLD = TagKey.create(Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath("forge", "is_cold"));
    private static final TagKey<Biome> C_IS_COLD = TagKey.create(Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath("c", "is_cold"));
    private static final TagKey<Biome> FORGE_IS_SNOWY = TagKey.create(Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath("forge", "is_snowy"));

    private static final Set<ResourceLocation> COLD_BIOMES_FALLBACK = Set.of(
            ResourceLocation.fromNamespaceAndPath("minecraft", "snowy_plains"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "ice_spikes"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "snowy_taiga"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "snowy_beach"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "snowy_slopes"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "frozen_peaks"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "jagged_peaks"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "frozen_river"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "frozen_ocean"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "deep_frozen_ocean"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "grove")
    );

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;

        RaceData data = RaceCapability.resolve(player);
        RaceType race = data.getRace();
        if (race == RaceType.NENHUMA) return;

        data.decrementCooldown();

        switch (race) {
            case DEMONIO -> tickDemonio(player, data);
            case HUMANO -> tickHumano(player, data);
            case QUIMERA -> tickQuimeraBase(player, data);
            case QUIMERA_ALADA -> tickQuimeraAlada(player, data);
            case QUIMERA_INSETO -> tickQuimeraInseto(player, data);
            case AQUARIANO -> tickAquariano(player, data);
            case ANJO -> tickAnjo(player, data);
            default -> {}
        }
    }

    private static void tickDemonio(ServerPlayer player, RaceData data) {
        player.clearFire();

        if (player.isInLava() && player.tickCount % 100 == 0) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 300, 1, false, false, false));
        }

        if (player.tickCount % 20 == 0) {
            if (isInWaterEnvironment(player)) {
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0, false, false, true));
            }
        }

        if (player.tickCount % 100 == 0) {
            if (isInColdEnvironment(player)) {
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 300, 0, false, false, true));
            }
        }

        if (data.isIraAtiva()) {
            if (isInColdEnvironment(player)) {
                data.setIraAtiva(false);
                player.removeEffect(MobEffects.MOVEMENT_SPEED);
                player.removeEffect(MobEffects.JUMP);
                player.removeEffect(MobEffects.DAMAGE_BOOST);
                player.removeEffect(MobEffects.DAMAGE_RESISTANCE);
                player.removeEffect(MobEffects.REGENERATION);
            }
        }
    }

    private static void tickHumano(ServerPlayer player, RaceData data) {
        if (data.getEspiritoCooldown() > 0) {
            data.decrementEspiritoCooldown();
        }

        if (data.isEspiritoAtivo()) {
            data.decrementEspiritoTimer();
            if (data.getEspiritoTimer() <= 0) {
                data.setEspiritoAtivo(false);
                data.setEspiritoCooldown(RaceConfig.HUMANO_SPIRIT_COOLDOWN.get());
                RaceManager.syncToClient(player, data);
            }
        }

        if (!data.isEspiritoAtivo()
                && data.getEspiritoCooldown() <= 0
                && player.getHealth() <= RaceConfig.HUMANO_SPIRIT_THRESHOLD_HP.get()) {
            int dur = RaceConfig.HUMANO_SPIRIT_DURATION.get();
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, dur, 1, false, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, dur, 0, false, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, dur, 0, false, false, true));
            data.setEspiritoAtivo(true);
            data.setEspiritoTimer(dur);
            RaceManager.syncToClient(player, data);
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§6Espírito Indomável ativado!"), true);
        }
    }

    private static void tickQuimeraBase(ServerPlayer player, RaceData data) {
    }

    private static void tickQuimeraAlada(ServerPlayer player, RaceData data) {
        if (!data.hasRaceElytra()) {
            RaceManager.applyRace(player, data, RaceType.QUIMERA_ALADA);
        }
    }

    private static void tickQuimeraInseto(ServerPlayer player, RaceData data) {
        if (data.isVenenoProximoAtaque()) {
            data.decrementVenenoExpireTimer();
            if (data.getVenenoExpireTimer() <= 0) {
                data.setVenenoProximoAtaque(false);
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("§cVeneno expirou sem uso."), true);
            }
        }
    }

    private static final Set<ResourceLocation> HOT_BIOMES = Set.of(
            ResourceLocation.fromNamespaceAndPath("minecraft", "desert"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "badlands"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "eroded_badlands"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "wooded_badlands"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "warm_ocean"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "mangrove_swamp"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "savanna"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "savanna_plateau"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "windswept_savanna")
    );

    private static void tickAquariano(ServerPlayer player, RaceData data) {
        if (player.tickCount % 40 == 0) {
            player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 80, 0, false, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 80, 0, false, false, false));
        }

        if (player.isUnderWater()) {
            net.minecraft.world.phys.Vec3 motion = player.getDeltaMovement();
            boolean sneaking = player.isShiftKeyDown();

            if (sneaking) {
                player.setDeltaMovement(motion.x, Math.min(motion.y, -0.2), motion.z);
            } else if (motion.y < -0.01) {
                player.setDeltaMovement(motion.x, 0.0, motion.z);
            }

            player.setNoGravity(true);
        } else {
            player.setNoGravity(false);
        }

        if (player.tickCount % 100 == 0) {
            if (isInHotEnvironment(player)) {
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 300, 0, false, false, true));
            }
        }
    }

    private static void tickAnjo(ServerPlayer player, RaceData data) {
        if (data.getAnjoCharges() > 0) {
            if (player.tickCount % 40 == 0) {
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 80, 0, false, false, false));
            }
        } else {
            player.removeEffect(MobEffects.SLOW_FALLING);
            if (player.tickCount % 100 == 0) {
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 300, 0, false, false, true));
            }
        }

        if (data.getAnjoCharges() < RaceConfig.ANJO_MAX_CHARGES.get()) {
            data.decrementAnjoRegenTimer();
            if (data.getAnjoRegenTimer() <= 0) {
                data.setAnjoCharges(data.getAnjoCharges() + 1);
                data.setAnjoRegenTimer(RaceConfig.ANJO_CHARGE_REGEN_TICKS.get());
                RaceManager.syncToClient(player, data);
            }
        }
    }

    private static boolean isOnColdBlock(ServerPlayer player) {
        BlockPos below = player.blockPosition().below();
        BlockState state = player.level().getBlockState(below);
        return COLD_BLOCKS.contains(state.getBlock());
    }

    private static boolean isInColdBiome(ServerPlayer player) {
        var biomeHolder = player.level().getBiome(player.blockPosition());

        if (biomeHolder.is(MINECRAFT_IS_COLD)) return true;
        if (biomeHolder.is(FORGE_IS_COLD)) return true;
        if (biomeHolder.is(C_IS_COLD)) return true;
        if (biomeHolder.is(FORGE_IS_SNOWY)) return true;

        for (ResourceLocation id : COLD_BIOMES_FALLBACK) {
            if (biomeHolder.is(id)) return true;
        }
        return false;
    }

    public static boolean isInColdEnvironment(ServerPlayer player) {
        return isOnColdBlock(player) || isInColdBiome(player);
    }

    private static boolean isInWaterEnvironment(ServerPlayer player) {
        return player.isInWaterOrRain() || player.isInWaterOrBubble();
    }

    private static boolean isInHotEnvironment(ServerPlayer player) {
        if (player.isOnFire()) return true;
        if (player.isInLava()) return true;
        if (player.level().dimension() == Level.NETHER) return true;
        var biomeHolder = player.level().getBiome(player.blockPosition());
        for (ResourceLocation id : HOT_BIOMES) {
            if (biomeHolder.is(id)) return true;
        }
        return false;
    }
}