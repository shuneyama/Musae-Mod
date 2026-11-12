package net.shune.musae.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.shune.musae.auth.AutorizacaoServidor;
import net.shune.musae.race.*;

import java.util.List;
import java.util.function.Supplier;

public class RaceAbilityPacket {

    public enum AbilityType {
        PRIMARY, SECONDARY
    }

    private final AbilityType type;

    public RaceAbilityPacket(AbilityType type) {
        this.type = type;
    }

    public static void encode(RaceAbilityPacket pkt, FriendlyByteBuf buf) {
        buf.writeEnum(pkt.type);
    }

    public static RaceAbilityPacket decode(FriendlyByteBuf buf) {
        return new RaceAbilityPacket(buf.readEnum(AbilityType.class));
    }

    public static void handle(RaceAbilityPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (!AutorizacaoServidor.isAutorizado()) return;
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            RaceData data = RaceCapability.resolve(player);
            RaceType race = data.getRace();
            if (race == RaceType.NENHUMA) return;

            switch (race) {
                case DEMONIO -> handleDemonio(player, data);
                case QUIMERA -> handleQuimeraFarejar(player, data);
                case QUIMERA_ALADA -> handleQuimeraAladaDash(player, data);
                case QUIMERA_INSETO -> handleQuimeraInsetoVeneno(player, data);
                case AQUARIANO -> handleAquarianoDash(player, data);
                case ANJO -> {
                    if (pkt.type == AbilityType.PRIMARY) {
                        handleAnjoBencaoArea(player, data);
                    } else {
                        handleAnjoBencaoIndividual(player, data);
                    }
                }
                default -> {}
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static void handleDemonio(ServerPlayer player, RaceData data) {
        if (data.getCooldownTicks() > 0) {
            player.displayClientMessage(Component.literal("§cIra em cooldown: "
                    + (data.getCooldownTicks() / 20) + "s"), true);
            return;
        }

        if (RaceTickHandler.isInColdEnvironment(player)) {
            player.displayClientMessage(Component.literal("§cVocê não pode ativar a Ira em ambiente gelado!"), true);
            return;
        }

        int dur = RaceConfig.DEMONIO_IRA_DURATION.get();
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, dur, 1, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.JUMP, dur, 1, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, dur, 1, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, dur, 0, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, dur, 0, false, false, true));

        data.setIraAtiva(true);
        data.setCooldownTicks(RaceConfig.DEMONIO_IRA_COOLDOWN.get());
        player.displayClientMessage(Component.literal("§4Ira ativada!"), true);
        RaceManager.syncToClient(player, data);
    }

    private static void handleQuimeraFarejar(ServerPlayer player, RaceData data) {
        if (data.getCooldownTicks() > 0) {
            player.displayClientMessage(Component.literal("§cFarejar em cooldown: "
                    + (data.getCooldownTicks() / 20) + "s"), true);
            return;
        }

        double radius = RaceConfig.QUIMERA_FAREJAR_RADIUS.get();
        int dur = RaceConfig.QUIMERA_FAREJAR_DURATION.get();
        AABB area = player.getBoundingBox().inflate(radius);
        List<LivingEntity> entities = player.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive());

        for (LivingEntity entity : entities) {
            entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, dur, 0, false, false, false));
        }

        data.setCooldownTicks(RaceConfig.QUIMERA_FAREJAR_COOLDOWN.get());
        player.displayClientMessage(Component.literal("§eFarejando... " + entities.size()
                + " entidades detectadas!"), true);
        RaceManager.syncToClient(player, data);
    }

    private static void handleQuimeraAladaDash(ServerPlayer player, RaceData data) {
        if (data.getCooldownTicks() > 0) {
            player.displayClientMessage(Component.literal("§cDash em cooldown: "
                    + (data.getCooldownTicks() / 20) + "s"), true);
            return;
        }

        Vec3 look = player.getLookAngle();
        double force = RaceConfig.QUIMERA_ALADA_DASH_FORCE.get();
        player.setDeltaMovement(look.x * force, look.y * force, look.z * force);
        player.hurtMarked = true;

        data.setCooldownTicks(RaceConfig.QUIMERA_ALADA_DASH_COOLDOWN.get());
        RaceManager.syncToClient(player, data);
    }

    private static void handleQuimeraInsetoVeneno(ServerPlayer player, RaceData data) {
        if (data.getCooldownTicks() > 0) {
            player.displayClientMessage(Component.literal("§cVeneno em cooldown: "
                    + (data.getCooldownTicks() / 20) + "s"), true);
            return;
        }

        if (data.isVenenoProximoAtaque()) {
            player.displayClientMessage(Component.literal("§eVeneno já está carregado!"), true);
            return;
        }

        data.setVenenoProximoAtaque(true);
        data.setVenenoExpireTimer(RaceConfig.QUIMERA_INSETO_VENOM_EXPIRE.get());
        player.displayClientMessage(Component.literal("§2Veneno carregado! Ataque para aplicar."), true);
        RaceManager.syncToClient(player, data);
    }

    private static void handleAquarianoDash(ServerPlayer player, RaceData data) {
        if (data.getCooldownTicks() > 0) {
            player.displayClientMessage(Component.literal("§cDash em cooldown: "
                    + (data.getCooldownTicks() / 20) + "s"), true);
            return;
        }

        if (!player.isInWater()) {
            player.displayClientMessage(Component.literal("§cVocê precisa estar na água!"), true);
            return;
        }

        Vec3 look = player.getLookAngle();
        double force = RaceConfig.AQUARIANO_DASH_FORCE.get();
        player.setDeltaMovement(look.x * force, look.y * force, look.z * force);
        player.hurtMarked = true;

        data.setCooldownTicks(RaceConfig.AQUARIANO_DASH_COOLDOWN.get());
        RaceManager.syncToClient(player, data);
    }

    private static void handleAnjoBencaoArea(ServerPlayer player, RaceData data) {
        if (data.getAnjoCharges() < RaceConfig.ANJO_MAX_CHARGES.get()) {
            player.displayClientMessage(Component.literal("§cVocê precisa de "
                    + RaceConfig.ANJO_MAX_CHARGES.get() + " cargas! Atual: " + data.getAnjoCharges()), true);
            return;
        }

        double radius = RaceConfig.ANJO_AREA_RADIUS.get();
        int dur = RaceConfig.ANJO_AREA_DURATION.get();
        AABB area = player.getBoundingBox().inflate(radius);
        List<Player> players = player.level().getEntitiesOfClass(Player.class, area, Entity::isAlive);

        for (Player target : players) {
            target.addEffect(new MobEffectInstance(MobEffects.REGENERATION, dur, 1, false, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, dur, 0, false, false, true));
            target.getActiveEffects().stream()
                    .filter(e -> e.getEffect().getCategory() == MobEffectCategory.HARMFUL)
                    .map(e -> e.getEffect())
                    .toList()
                    .forEach(target::removeEffect);
        }

        data.setAnjoCharges(0);
        data.setAnjoRegenTimer(RaceConfig.ANJO_CHARGE_REGEN_TICKS.get());
        player.displayClientMessage(Component.literal("§eBenção em Área! "
                + players.size() + " jogadores abençoados."), true);
        RaceManager.syncToClient(player, data);
    }

    private static void handleAnjoBencaoIndividual(ServerPlayer player, RaceData data) {
        if (data.getAnjoCharges() < 1) {
            player.displayClientMessage(Component.literal("§cSem cargas de energia!"), true);
            return;
        }

        double range = RaceConfig.ANJO_SINGLE_RANGE.get();
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        Vec3 endPos = eyePos.add(lookVec.scale(range));

        AABB searchArea = player.getBoundingBox().expandTowards(lookVec.scale(range)).inflate(2.0);
        List<Player> candidates = player.level().getEntitiesOfClass(Player.class, searchArea,
                p -> p != player && p.isAlive());

        Player closest = null;
        double closestDist = Double.MAX_VALUE;

        for (Player candidate : candidates) {
            AABB bb = candidate.getBoundingBox().inflate(0.5);
            var optional = bb.clip(eyePos, endPos);
            if (optional.isPresent()) {
                double dist = eyePos.distanceToSqr(optional.get());
                if (dist < closestDist) {
                    closestDist = dist;
                    closest = candidate;
                }
            }
        }

        if (closest == null) {
            player.displayClientMessage(Component.literal("§cNenhum jogador encontrado na mira."), true);
            return;
        }

        int dur = RaceConfig.ANJO_SINGLE_DURATION.get();
        closest.addEffect(new MobEffectInstance(MobEffects.REGENERATION, dur, 1, false, false, true));
        closest.addEffect(new MobEffectInstance(MobEffects.GLOWING, dur, 0, false, false, true));
        closest.getActiveEffects().stream()
                .filter(e -> e.getEffect().getCategory() == MobEffectCategory.HARMFUL)
                .map(e -> e.getEffect())
                .toList()
                .forEach(closest::removeEffect);

        data.setAnjoCharges(data.getAnjoCharges() - 1);
        if (data.getAnjoCharges() < RaceConfig.ANJO_MAX_CHARGES.get()) {
            data.setAnjoRegenTimer(RaceConfig.ANJO_CHARGE_REGEN_TICKS.get());
        }

        player.displayClientMessage(Component.literal("§eBenção aplicada em "
                + closest.getGameProfile().getName() + "!"), true);
        RaceManager.syncToClient(player, data);
    }
}