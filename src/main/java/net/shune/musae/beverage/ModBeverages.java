package net.shune.musae.beverage;

import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.shune.musae.MusaeMod;

public class ModBeverages {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MusaeMod.MODID);

    public static final BeverageType VINHO_SANGUE_GALINHA_TYPE =
            new BeverageType("vinho_sangue_galinha", 2, 0.3f, 5);
    public static final BeverageType VINHO_QUENTINHO_INFERNO_TYPE =
            new BeverageType("vinho_quentinho_inferno", 2, 0.3f, 5);
    public static final BeverageType JOAO_DANIEL_TYPE =
            new BeverageType("joao_daniel", 1, 0.2f, 3);
    public static final BeverageType CONHAQUE_PRESIDENTA_DILMA_TYPE =
            new BeverageType("conhaque_presidenta_dilma", 1, 0.2f, 3);
    public static final BeverageType CACHACA_PITA_TYPE =
            new BeverageType("cachaca_pita", 1, 0.2f, 3);
    public static final BeverageType BALAZEDA_TYPE =
            new BeverageType("balazeda", 1, 0.2f, 3);
    public static final BeverageType CACHACA_52_TYPE =
            new BeverageType("cachaca_52", 1, 0.2f, 3);
    public static final BeverageType CAMPARTY_TYPE =
            new BeverageType("camparty", 1, 0.3f, 7);
    public static final BeverageType FRIOZAO_TYPE =
            new BeverageType("friozao", 1, 0.3f, 7);
    public static final BeverageType MIJO_EGUA_TYPE =
            new BeverageType("mijo_egua", 1, 0.2f, 4);
    public static final BeverageType SUBURBANINHA_TYPE =
            new BeverageType("suburbaninha", 1, 0.3f, 8);

    public static final RegistryObject<Block> VINHO_SANGUE_GALINHA_BLOCK = BLOCKS.register(
            "vinho_sangue_galinha",
            () -> new BeverageBlock(BeverageBlock.Profile.BOTTLE,
                    () -> net.shune.musae.item.ModItems.VINHO_SANGUE_GALINHA.get()));

    public static final RegistryObject<Block> VINHO_QUENTINHO_INFERNO_BLOCK = BLOCKS.register(
            "vinho_quentinho_inferno",
            () -> new BeverageBlock(BeverageBlock.Profile.BOTTLE,
                    () -> net.shune.musae.item.ModItems.VINHO_QUENTINHO_INFERNO.get()));

    public static final RegistryObject<Block> JOAO_DANIEL_BLOCK = BLOCKS.register(
            "joao_daniel",
            () -> new BeverageBlock(BeverageBlock.Profile.BOTTLE,
                    () -> net.shune.musae.item.ModItems.JOAO_DANIEL.get()));

    public static final RegistryObject<Block> CONHAQUE_PRESIDENTA_DILMA_BLOCK = BLOCKS.register(
            "conhaque_presidenta_dilma",
            () -> new BeverageBlock(BeverageBlock.Profile.BOTTLE,
                    () -> net.shune.musae.item.ModItems.CONHAQUE_PRESIDENTA_DILMA.get()));

    public static final RegistryObject<Block> CACHACA_PITA_BLOCK = BLOCKS.register(
            "cachaca_pita",
            () -> new BeverageBlock(BeverageBlock.Profile.BOTTLE,
                    () -> net.shune.musae.item.ModItems.CACHACA_PITA.get()));

    public static final RegistryObject<Block> BALAZEDA_BLOCK = BLOCKS.register(
            "balazeda",
            () -> new BeverageBlock(BeverageBlock.Profile.BOTTLE,
                    () -> net.shune.musae.item.ModItems.BALAZEDA.get()));

    public static final RegistryObject<Block> CACHACA_52_BLOCK = BLOCKS.register(
            "cachaca_52",
            () -> new BeverageBlock(BeverageBlock.Profile.BOTTLE,
                    () -> net.shune.musae.item.ModItems.CACHACA_52.get()));

    public static final RegistryObject<Block> CAMPARTY_BLOCK = BLOCKS.register(
            "camparty",
            () -> new BeverageBlock(BeverageBlock.Profile.GLASS,
                    () -> net.shune.musae.item.ModItems.CAMPARTY.get()));

    public static final RegistryObject<Block> FRIOZAO_BLOCK = BLOCKS.register(
            "friozao",
            () -> new BeverageBlock(BeverageBlock.Profile.GLASS,
                    () -> net.shune.musae.item.ModItems.FRIOZAO.get()));

    public static final RegistryObject<Block> MIJO_EGUA_BLOCK = BLOCKS.register(
            "mijo_egua",
            () -> new BeverageBlock(BeverageBlock.Profile.GLASS,
                    () -> net.shune.musae.item.ModItems.MIJO_EGUA.get()));

    public static final RegistryObject<Block> SUBURBANINHA_BLOCK = BLOCKS.register(
            "suburbaninha",
            () -> new BeverageBlock(BeverageBlock.Profile.GLASS,
                    () -> net.shune.musae.item.ModItems.SUBURBANINHA.get()));
}
