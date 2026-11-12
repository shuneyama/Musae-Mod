package net.shune.musae.item.sword;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.shune.musae.auth.AutorizacaoServidor;

import java.util.List;

public class ThornedBladeSword extends SwordItem {

    private static final int ACTIVE_COOLDOWN_TICKS = 1200;
    private static final double AOE_RADIUS = 6.0;
    private static final int PASSIVE_REFRESH_INTERVAL = 100;
    private static final int PASSIVE_DURATION = 400;

    public ThornedBladeSword(Properties properties) {
        super(Tiers.NETHERITE, 15, -2.4f, properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        if (level.isClientSide()) return;
        if (!(entity instanceof Player player)) return;
        if (player.tickCount % PASSIVE_REFRESH_INTERVAL != 0) return;

        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, PASSIVE_DURATION, 0, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, PASSIVE_DURATION, 0, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, PASSIVE_DURATION, 0, false, false, true));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!AutorizacaoServidor.isAutorizado()) return super.hurtEnemy(stack, target, attacker);
        target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1, false, true, true));
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!AutorizacaoServidor.isAutorizado()) return InteractionResultHolder.pass(stack);

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide()) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.6f, 0.7f);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.WARDEN_ROAR, SoundSource.PLAYERS, 1.0f, 0.8f);

            AABB area = player.getBoundingBox().inflate(AOE_RADIUS);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
                    e -> e != player);

            for (LivingEntity target : targets) {
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 2, false, true, true));
                target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 200, 0, false, true, true));
            }

            if (level instanceof net.minecraft.server.level.ServerLevel server) {
                server.sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE,
                        player.getX(), player.getY() + 1.0, player.getZ(),
                        80, AOE_RADIUS * 0.5, 1.0, AOE_RADIUS * 0.5, 0.05);
                server.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL,
                        player.getX(), player.getY() + 1.0, player.getZ(),
                        40, AOE_RADIUS * 0.4, 0.8, AOE_RADIUS * 0.4, 0.02);
            }
        }

        player.getCooldowns().addCooldown(this, ACTIVE_COOLDOWN_TICKS);
        return InteractionResultHolder.success(stack);
    }
}