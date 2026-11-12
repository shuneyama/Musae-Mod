package net.shune.musae.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.shune.musae.MusaeMod;

public class ModCreativeTab {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MusaeMod.MODID);

    public static final RegistryObject<CreativeModeTab> MUSAE_TAB = CREATIVE_TABS.register("musae_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.musae"))
                    .icon(() -> new ItemStack(ModItems.CONTRACT_BOOK.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.RING_BLACKSMITH.get());
                        output.accept(ModItems.RING_KNIGHT.get());
                        output.accept(ModItems.RING_MAGE.get());
                        output.accept(ModItems.RING_ROGUE.get());
                        output.accept(ModItems.RING_SWORDSMAN.get());
                        output.accept(ModItems.RING_MEDIC.get());
                        output.accept(ModItems.CONTRACT_BOOK.get());
                        output.accept(ModItems.CREATION_WEAVER.get());
                        output.accept(ModItems.THORNED_BLADE.get());
                        output.accept(ModItems.ABYSSAL_WATCHER.get());
                        output.accept(ModItems.VINHO_SANGUE_GALINHA.get());
                        output.accept(ModItems.VINHO_QUENTINHO_INFERNO.get());
                        output.accept(ModItems.JOAO_DANIEL.get());
                        output.accept(ModItems.CONHAQUE_PRESIDENTA_DILMA.get());
                        output.accept(ModItems.CACHACA_PITA.get());
                        output.accept(ModItems.BALAZEDA.get());
                        output.accept(ModItems.CACHACA_52.get());
                        output.accept(ModItems.CAMPARTY.get());
                        output.accept(ModItems.FRIOZAO.get());
                        output.accept(ModItems.MIJO_EGUA.get());
                        output.accept(ModItems.SUBURBANINHA.get());
                        output.accept(ModItems.MUG_DOLL.get());
                        output.accept(ModItems.BONECO_COM_PET.get());
                        output.accept(ModItems.HARU_DOLL.get());
                        output.accept(ModItems.JESS_DOLL.get());
                        output.accept(ModItems.KAEL_DOLL.get());
                        output.accept(ModItems.MUSHNEY_DOLL.get());
                        output.accept(ModItems.MILKY_DOLL.get());
                        output.accept(ModItems.RELL_DOLL.get());
                        output.accept(ModItems.SOPHIA_DOLL.get());
                        output.accept(ModItems.TAURON_DOLL.get());
                        output.accept(ModItems.CARECA_DOLL.get());
                        output.accept(ModItems.DIZZY_DOLL.get());
                        output.accept(ModItems.HONEY_DOLL.get());
                        output.accept(ModItems.LILIK_DOLL.get());
                        output.accept(ModItems.MIKE_DOLL.get());
                        output.accept(ModItems.NOAH_DOLL.get());
                        output.accept(ModItems.TOMMY_DOLL.get());
                        output.accept(ModItems.VITOR_DOLL.get());
                    })
                    .build()
    );
}
