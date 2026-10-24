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
}
