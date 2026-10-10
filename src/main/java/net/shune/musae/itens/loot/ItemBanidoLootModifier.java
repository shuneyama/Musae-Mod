package net.shune.musae.itens.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.shune.musae.auth.AutorizacaoServidor;
import net.shune.musae.itens.ConfiguracaoItensBanidos;

import java.util.Iterator;

public class ItemBanidoLootModifier extends LootModifier {

    private static final float CHANCE_DE_REDUZIR = 0.5F;

    public static final Codec<ItemBanidoLootModifier> CODEC =
            RecordCodecBuilder.create(instance -> codecStart(instance).apply(instance, ItemBanidoLootModifier::new));

    public ItemBanidoLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!AutorizacaoServidor.isAutorizado()) return generatedLoot;
        var servidor = context.getLevel().getServer();
        if (servidor == null) return generatedLoot;

        RandomSource random = context.getRandom();
        Iterator<ItemStack> iterator = generatedLoot.iterator();

        while (iterator.hasNext()) {
            ItemStack stack = iterator.next();
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());

            if (ConfiguracaoItensBanidos.isBanido(servidor, itemId) && random.nextFloat() < CHANCE_DE_REDUZIR) {
                iterator.remove();
            }
        }

        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return ModLootModifiers.ITEM_BANIDO_REDUTOR.get();
    }
}
