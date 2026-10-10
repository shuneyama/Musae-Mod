package net.shune.musae.race;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.shune.musae.MusaeMod;
import net.shune.musae.auth.AutorizacaoServidor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Mod.EventBusSubscriber(modid = MusaeMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class QuimeraFoodHandler {

    private record FoodValue(int nutrition, float saturation) {}

    private static final FoodValue BONE_VALUE = new FoodValue(2, 0.3f);
    private static final FoodValue LEATHER_VALUE = new FoodValue(2, 0.3f);
    private static final FoodValue MUSHROOM_VALUE = new FoodValue(3, 0.4f);
    private static final FoodValue LEAF_VALUE = new FoodValue(1, 0.1f);
    private static final FoodValue FUNNY_VALUE = new FoodValue(0, 0.0f);

    private static final Map<Item, FoodValue> EXPLICIT_FOODS = new HashMap<>();

    private static final Set<Item> FUNNY_ITEMS = Set.of(
            Items.COBBLESTONE, Items.STONE, Items.DEEPSLATE, Items.COBBLED_DEEPSLATE,
            Items.ANDESITE, Items.DIORITE, Items.GRANITE, Items.TUFF,
            Items.CALCITE, Items.GRAVEL, Items.NETHERRACK, Items.FLINT, Items.CLAY_BALL
    );

    static {
        EXPLICIT_FOODS.put(Items.BONE, BONE_VALUE);
        EXPLICIT_FOODS.put(Items.LEATHER, LEATHER_VALUE);
        EXPLICIT_FOODS.put(Items.BROWN_MUSHROOM, MUSHROOM_VALUE);
        EXPLICIT_FOODS.put(Items.RED_MUSHROOM, MUSHROOM_VALUE);
    }

    private static FoodValue resolveFood(ItemStack stack) {
        Item item = stack.getItem();

        FoodValue explicit = EXPLICIT_FOODS.get(item);
        if (explicit != null) return explicit;

        if (stack.is(ItemTags.LEAVES)) return LEAF_VALUE;

        if (FUNNY_ITEMS.contains(item)) return FUNNY_VALUE;

        return null;
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        RaceData data = RaceCapability.resolve(serverPlayer);
        if (!data.getRace().isQuimera()) return;

        ItemStack held = event.getItemStack();
        FoodValue food = resolveFood(held);
        if (food == null) return;

        if (food.nutrition > 0 && serverPlayer.getFoodData().getFoodLevel() >= 20) return;

        if (food.nutrition > 0) {
            serverPlayer.getFoodData().eat(food.nutrition, food.saturation);
        }

        if (!serverPlayer.getAbilities().instabuild) {
            held.shrink(1);
        }

        serverPlayer.level().playSound(null,
                serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                SoundEvents.GENERIC_EAT, SoundSource.PLAYERS,
                0.8f, 0.8f + serverPlayer.getRandom().nextFloat() * 0.4f);

        serverPlayer.getCooldowns().addCooldown(held.getItem(), 10);

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }
}