package net.shune.musae.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.shune.musae.MusaeMod;

import net.shune.musae.beverage.BeverageItem;
import net.shune.musae.beverage.ModBeverages;
import net.shune.musae.contract.ContractBookItem;
import net.shune.musae.item.sword.AbyssalWatcherScythe;
import net.shune.musae.item.sword.CreationWeaverSword;
import net.shune.musae.item.sword.ThornedBladeSword;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MusaeMod.MODID);

    public static final RegistryObject<Item> RING_BLACKSMITH = ITEMS.register("ring_blacksmith",
            () -> new RelicRingItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE),
                    RelicRingItem.RingType.BLACKSMITH));

    public static final RegistryObject<Item> RING_KNIGHT = ITEMS.register("ring_knight",
            () -> new RelicRingItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE),
                    RelicRingItem.RingType.KNIGHT));

    public static final RegistryObject<Item> RING_MAGE = ITEMS.register("ring_mage",
            () -> new RelicRingItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE),
                    RelicRingItem.RingType.MAGE));

    public static final RegistryObject<Item> RING_ROGUE = ITEMS.register("ring_rogue",
            () -> new RelicRingItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE),
                    RelicRingItem.RingType.ROGUE));

    public static final RegistryObject<Item> RING_SWORDSMAN = ITEMS.register("ring_swordsman",
            () -> new RelicRingItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE),
                    RelicRingItem.RingType.SWORDSMAN));

    public static final RegistryObject<Item> RING_MEDIC = ITEMS.register("ring_medic",
            () -> new RelicRingItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE),
                    RelicRingItem.RingType.MEDIC));

    public static final RegistryObject<Item> CONTRACT_BOOK = ITEMS.register("contract_book",
            () -> new ContractBookItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> CREATION_WEAVER = ITEMS.register("lamina_lobo_branco",
            () -> new CreationWeaverSword(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant()));

    public static final RegistryObject<Item> THORNED_BLADE = ITEMS.register("devoradora",
            () -> new ThornedBladeSword(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant()));

    public static final RegistryObject<Item> ABYSSAL_WATCHER = ITEMS.register("veredito",
            () -> new AbyssalWatcherScythe(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant()));

    public static final RegistryObject<Item> VINHO_SANGUE_GALINHA = ITEMS.register("vinho_sangue_galinha",
            () -> new BeverageItem(ModBeverages.VINHO_SANGUE_GALINHA_BLOCK,
                    new Item.Properties().stacksTo(16), ModBeverages.VINHO_SANGUE_GALINHA_TYPE));

    public static final RegistryObject<Item> VINHO_QUENTINHO_INFERNO = ITEMS.register("vinho_quentinho_inferno",
            () -> new BeverageItem(ModBeverages.VINHO_QUENTINHO_INFERNO_BLOCK,
                    new Item.Properties().stacksTo(16), ModBeverages.VINHO_QUENTINHO_INFERNO_TYPE));

    public static final RegistryObject<Item> JOAO_DANIEL = ITEMS.register("joao_daniel",
            () -> new BeverageItem(ModBeverages.JOAO_DANIEL_BLOCK,
                    new Item.Properties().stacksTo(16), ModBeverages.JOAO_DANIEL_TYPE));

    public static final RegistryObject<Item> CONHAQUE_PRESIDENTA_DILMA = ITEMS.register("conhaque_presidenta_dilma",
            () -> new BeverageItem(ModBeverages.CONHAQUE_PRESIDENTA_DILMA_BLOCK,
                    new Item.Properties().stacksTo(16), ModBeverages.CONHAQUE_PRESIDENTA_DILMA_TYPE));

    public static final RegistryObject<Item> CACHACA_PITA = ITEMS.register("cachaca_pita",
            () -> new BeverageItem(ModBeverages.CACHACA_PITA_BLOCK,
                    new Item.Properties().stacksTo(16), ModBeverages.CACHACA_PITA_TYPE));

    public static final RegistryObject<Item> BALAZEDA = ITEMS.register("balazeda",
            () -> new BeverageItem(ModBeverages.BALAZEDA_BLOCK,
                    new Item.Properties().stacksTo(16), ModBeverages.BALAZEDA_TYPE));

    public static final RegistryObject<Item> CACHACA_52 = ITEMS.register("cachaca_52",
            () -> new BeverageItem(ModBeverages.CACHACA_52_BLOCK,
                    new Item.Properties().stacksTo(16), ModBeverages.CACHACA_52_TYPE));

    public static final RegistryObject<Item> CAMPARTY = ITEMS.register("camparty",
            () -> new BeverageItem(ModBeverages.CAMPARTY_BLOCK,
                    new Item.Properties().stacksTo(16), ModBeverages.CAMPARTY_TYPE));

    public static final RegistryObject<Item> FRIOZAO = ITEMS.register("friozao",
            () -> new BeverageItem(ModBeverages.FRIOZAO_BLOCK,
                    new Item.Properties().stacksTo(16), ModBeverages.FRIOZAO_TYPE));

    public static final RegistryObject<Item> MIJO_EGUA = ITEMS.register("mijo_egua",
            () -> new BeverageItem(ModBeverages.MIJO_EGUA_BLOCK,
                    new Item.Properties().stacksTo(16), ModBeverages.MIJO_EGUA_TYPE));

    public static final RegistryObject<Item> SUBURBANINHA = ITEMS.register("suburbaninha",
            () -> new BeverageItem(ModBeverages.SUBURBANINHA_BLOCK,
                    new Item.Properties().stacksTo(16), ModBeverages.SUBURBANINHA_TYPE));

    public static final RegistryObject<Item> MUG_DOLL = ITEMS.register("mug_doll",
            () -> new net.minecraft.world.item.BlockItem(net.shune.musae.doll.ModDollBlocks.MUG_DOLL.get(),
                    new Item.Properties()));

    public static final RegistryObject<Item> BONECO_COM_PET = ITEMS.register("boneco_com_pet",
            () -> new net.minecraft.world.item.BlockItem(net.shune.musae.doll.ModDollBlocks.BONECO_COM_PET.get(),
                    new Item.Properties()));
}
