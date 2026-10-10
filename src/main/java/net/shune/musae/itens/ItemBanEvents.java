package net.shune.musae.itens;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.server.level.ServerPlayer;
import net.shune.musae.MusaeMod;
import net.shune.musae.auth.AutorizacaoServidor;

@Mod.EventBusSubscriber(modid = MusaeMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ItemBanEvents {

    private static final String MENSAGEM_BANIDO = "Você não pode utilizar esse item.";

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onItemUseStart(LivingEntityUseItemEvent.Start event) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (ItensBanidosUtil.isBanido(player, event.getItem())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onItemUseTick(LivingEntityUseItemEvent.Tick event) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (ItensBanidosUtil.isBanido(player, event.getItem())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (ItensBanidosUtil.isBanido(player, event.getItem())) {
            event.setResultStack(ItemStack.EMPTY);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (ItensBanidosUtil.isBanido(player, event.getItemStack())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (ItensBanidosUtil.isBanido(player, event.getItemStack())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onAttack(AttackEntityEvent event) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (ItensBanidosUtil.isBanido(player, player.getMainHandItem())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (ItensBanidosUtil.isBanido(player, player.getMainHandItem())) {
            event.setNewSpeed(0.0f);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack para = event.getTo();
        if (para.isEmpty()) return;

        EquipmentSlot slot = event.getSlot();
        if (slot.getType() != EquipmentSlot.Type.ARMOR && slot != EquipmentSlot.OFFHAND) return;

        if (ItensBanidosUtil.isBanido(player, para)) {
            player.getServer().execute(() -> {
                ItemStack equipado = player.getItemBySlot(slot);
                if (!equipado.isEmpty() && ItensBanidosUtil.isBanido(player, equipado)) {
                    player.setItemSlot(slot, ItemStack.EMPTY);
                    if (!player.getInventory().add(equipado)) {
                        player.drop(equipado, false);
                    }
                    player.sendSystemMessage(Component.literal(MENSAGEM_BANIDO).withStyle(ChatFormatting.RED));
                }
            });
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onCraft(PlayerEvent.ItemCraftedEvent event) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack craftado = event.getCrafting();
        if (ItensBanidosUtil.isBanido(player, craftado)) {
            craftado.setCount(0);
            player.sendSystemMessage(Component.literal("Você não pode craftar este item.").withStyle(ChatFormatting.RED));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPickup(EntityItemPickupEvent event) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (ItensBanidosUtil.isBanido(player, event.getItem().getItem())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onItemAttributes(net.minecraftforge.event.ItemAttributeModifierEvent event) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        if (musae_servidorAtual == null) return;

        ItemStack stack = event.getItemStack();
        ServerPlayer dono = encontrarDono(stack);

        boolean banido = dono != null
                ? ItensBanidosUtil.isBanido(dono, stack)
                : ItensBanidosUtil.isBanido(musae_servidorAtual, stack);

        if (!banido) return;

        for (net.minecraft.world.entity.ai.attributes.Attribute atributo : event.getOriginalModifiers().keySet()) {
            event.removeAttribute(atributo);
        }
    }

    private static ServerPlayer encontrarDono(ItemStack stack) {
        for (ServerPlayer player : musae_servidorAtual.getPlayerList().getPlayers()) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (player.getItemBySlot(slot) == stack) {
                    return player;
                }
            }
        }
        return null;
    }

    private static net.minecraft.server.MinecraftServer musae_servidorAtual;

    @SubscribeEvent
    public static void onServerStarting(net.minecraftforge.event.server.ServerStartingEvent event) {
        musae_servidorAtual = event.getServer();
    }
}
