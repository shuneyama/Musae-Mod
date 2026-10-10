package net.shune.musae.contract;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.shune.musae.MusaeMod;
import net.shune.musae.auth.AutorizacaoServidor;

@Mod.EventBusSubscriber(modid = MusaeMod.MODID)
public class ContractBookEvents {

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        if (!(event.getEntity() instanceof ItemEntity itemEntity)) return;
        if (!(itemEntity.getItem().getItem() instanceof ContractBookItem)) return;
        itemEntity.setInvulnerable(true);
    }
}
