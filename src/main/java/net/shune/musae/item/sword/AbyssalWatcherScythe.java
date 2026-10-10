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
import net.minecraft.world.phys.Vec3;
import net.shune.musae.auth.AutorizacaoServidor;

import java.util.List;

public class AbyssalWatcherScythe extends SwordItem {

    private static final int DASH_COOLDOWN_TICKS = 300;
    private static final double DASH_FORCE = 1.5;
    private static final float DASH_DAMAGE = 12.0f;
    private static final int DASH_FIRE_SECONDS = 6;
    private static final double DASH_HIT_RADIUS = 2.0;
    private static final int PASSIVE_REFRESH_INTERVAL = 100;
    private static final int PASSIVE_DURATION = 400;
    private static final int NIGHT_VISION_DURATION = 500;

    public AbyssalWatcherScythe(Properties properties) {
        super(Tiers.NETHERITE, 15, -2.4f, properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        if (level.isClientSide()) return;
        if (!(entity instanceof Player player)) return;
        if (player.tickCount % PASSIVE_REFRESH_INTERVAL != 0) return;

        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, PASSIVE_DURATION, 0, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, PASSIVE_DURATION, 1, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, PASSIVE_DURATION, 1, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, NIGHT_VISION_DURATION, 1, false, false, true));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!AutorizacaoServidor.isAutorizado()) return InteractionResultHolder.pass(stack);

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        Vec3 look = player.getLookAngle();
        Vec3 dash = new Vec3(look.x * DASH_FORCE, Math.max(0.3, look.y * DASH_FORCE * 0.5), look.z * DASH_FORCE);
        player.setDeltaMovement(dash);
        player.hurtMarked = true;

        if (!level.isClientSide()) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0f, 0.9f);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0f, 1.2f);

            Vec3 origin = player.position();
            Vec3 end = origin.add(look.scale(6.0));
            AABB sweep = new AABB(origin, end).inflate(DASH_HIT_RADIUS);

            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, sweep,
                    e -> e != player && e.isAlive());

            for (LivingEntity target : targets) {
                target.hurt(player.damageSources().playerAttack(player), DASH_DAMAGE);
                target.setSecondsOnFire(DASH_FIRE_SECONDS);
            }

            if (level instanceof net.minecraft.server.level.ServerLevel server) {
                server.sendParticles(net.minecraft.core.particles.ParticleTypes.FLAME,
                        player.getX(), player.getY() + 1.0, player.getZ(),
                        60, 0.5, 0.5, 0.5, 0.1);
                server.sendParticles(net.minecraft.core.particles.ParticleTypes.LARGE_SMOKE,
                        player.getX(), player.getY() + 1.0, player.getZ(),
                        30, 0.6, 0.6, 0.6, 0.05);
            }
        }

        player.getCooldowns().addCooldown(this, DASH_COOLDOWN_TICKS);
        player.swing(hand);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!AutorizacaoServidor.isAutorizado()) return super.hurtEnemy(stack, target, attacker);
        target.setSecondsOnFire(3);
        return super.hurtEnemy(stack, target, attacker);
    }
}