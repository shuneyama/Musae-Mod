package net.shune.musae.doll;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.shune.musae.MusaeMod;

public class ModDollBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MusaeMod.MODID);

    public static final RegistryObject<Block> MUG_DOLL = BLOCKS.register("mug_doll",
            () -> new DollBlock(Block.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(1.0f)
                    .sound(SoundType.WOOL)
                    .noOcclusion()));

    public static final RegistryObject<Block> BONECO_COM_PET = BLOCKS.register("boneco_com_pet",
            () -> new DollBlock(Block.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(1.0f)
                    .sound(SoundType.WOOL)
                    .noOcclusion()));

    public static final RegistryObject<Block> HARU_DOLL = BLOCKS.register("haru_doll",
            () -> new DollBlock(Block.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(1.0f)
                    .sound(SoundType.WOOL)
                    .noOcclusion()));

    public static final RegistryObject<Block> JESS_DOLL = BLOCKS.register("jess_doll",
            () -> new DollBlock(Block.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(1.0f)
                    .sound(SoundType.WOOL)
                    .noOcclusion()));

    public static final RegistryObject<Block> KAEL_DOLL = BLOCKS.register("kael_doll",
            () -> new DollBlock(Block.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(1.0f)
                    .sound(SoundType.WOOL)
                    .noOcclusion()));

    public static final RegistryObject<Block> MUSHNEY_DOLL = BLOCKS.register("mushney_doll",
            () -> new DollBlock(Block.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(1.0f)
                    .sound(SoundType.WOOL)
                    .noOcclusion()));

    public static final RegistryObject<Block> MILKY_DOLL = BLOCKS.register("milky_doll",
            () -> new DollBlock(Block.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(1.0f)
                    .sound(SoundType.WOOL)
                    .noOcclusion()));

    public static final RegistryObject<Block> RELL_DOLL = BLOCKS.register("rell_doll",
            () -> new DollBlock(Block.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(1.0f)
                    .sound(SoundType.WOOL)
                    .noOcclusion()));

    public static final RegistryObject<Block> SOPHIA_DOLL = BLOCKS.register("sophia_doll",
            () -> new DollBlock(Block.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(1.0f)
                    .sound(SoundType.WOOL)
                    .noOcclusion()));

    public static final RegistryObject<Block> TAURON_DOLL = BLOCKS.register("tauron_doll",
            () -> new DollBlock(Block.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(1.0f)
                    .sound(SoundType.WOOL)
                    .noOcclusion()));

    public static final RegistryObject<Block> CARECA_DOLL = BLOCKS.register("careca_doll",
            () -> new DollBlock(Block.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(1.0f)
                    .sound(SoundType.WOOL)
                    .noOcclusion()));

    public static final RegistryObject<Block> DIZZY_DOLL = BLOCKS.register("dizzy_doll",
            () -> new DollBlock(Block.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(1.0f)
                    .sound(SoundType.WOOL)
                    .noOcclusion()));

    public static final RegistryObject<Block> HONEY_DOLL = BLOCKS.register("honey_doll",
            () -> new DollBlock(Block.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(1.0f)
                    .sound(SoundType.WOOL)
                    .noOcclusion()));

    public static final RegistryObject<Block> LILIK_DOLL = BLOCKS.register("lilik_doll",
            () -> new DollBlock(Block.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(1.0f)
                    .sound(SoundType.WOOL)
                    .noOcclusion()));

    public static final RegistryObject<Block> MIKE_DOLL = BLOCKS.register("mike_doll",
            () -> new DollBlock(Block.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(1.0f)
                    .sound(SoundType.WOOL)
                    .noOcclusion()));

    public static final RegistryObject<Block> NOAH_DOLL = BLOCKS.register("noah_doll",
            () -> new DollBlock(Block.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(1.0f)
                    .sound(SoundType.WOOL)
                    .noOcclusion()));

    public static final RegistryObject<Block> TOMMY_DOLL = BLOCKS.register("tommy_doll",
            () -> new DollBlock(Block.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(1.0f)
                    .sound(SoundType.WOOL)
                    .noOcclusion()));

    public static final RegistryObject<Block> VITOR_DOLL = BLOCKS.register("vitor_doll",
            () -> new DollBlock(Block.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(1.0f)
                    .sound(SoundType.WOOL)
                    .noOcclusion()));
}
