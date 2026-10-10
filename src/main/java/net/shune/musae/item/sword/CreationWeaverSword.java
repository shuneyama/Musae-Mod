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
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.shune.musae.auth.AutorizacaoServidor;

public class CreationWeaverSword extends SwordItem {

    private static final int MAX_HOLD_TICKS = 200;
    private static final int COOLDOWN_TICKS = 1200;
    private static final int PASSIVE_REFRESH_INTERVAL = 100;
    private static final int PASSIVE_DURATION = 400;

    public CreationWeaverSword(Properties properties) {
        super(Tiers.NETHERITE, 15, -2.4f, properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        if (level.isClientSide()) return;
        if (!(entity instanceof Player player)) return;
        if (player.tickCount % PASSIVE_REFRESH_INTERVAL != 0) return;
        if (player.isUsingItem() && player.getUseItem() == stack) return;

        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, PASSIVE_DURATION, 0, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, PASSIVE_DURATION, 0, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, PASSIVE_DURATION, 0, false, false, true));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return MAX_HOLD_TICKS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!AutorizacaoServidor.isAutorizado()) return InteractionResultHolder.pass(stack);

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        player.startUsingItem(hand);
        if (!level.isClientSide()) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingTicks) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        if (level.isClientSide()) return;
        if (!(entity instanceof Player player)) return;

        int usedTicks = getUseDuration(stack) - remainingTicks;

        if (usedTicks > 0 && usedTicks % 40 == 0) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 0.8f, 1.0f);
        }

        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20, 2, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20, 2, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 2, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20, 0, false, false, true));
    }

    private void endChannel(Level level, Player player) {
        player.removeEffect(MobEffects.DAMAGE_RESISTANCE);
        player.removeEffect(MobEffects.REGENERATION);
        player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        player.removeEffect(MobEffects.GLOWING);

        if (!level.isClientSide()) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0f, 1.0f);
        }

        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        if (!(entity instanceof Player player)) return;
        endChannel(level, player);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!AutorizacaoServidor.isAutorizado()) return stack;
        if (entity instanceof Player player) {
            endChannel(level, player);
        }
        return stack;
    }
}