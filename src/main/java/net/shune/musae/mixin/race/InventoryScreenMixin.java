package net.shune.musae.mixin.race;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.shune.musae.auth.AutorizacaoServidor;
import net.shune.musae.client.ClientRaceHandler;
import net.shune.musae.race.RaceType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends EffectRenderingInventoryScreen<InventoryMenu> {

    @Unique
    private static final ResourceLocation SLOT_TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/container/inventory.png");

    @Unique private static final int SECONDARY_OFFSET_X = 20;
    @Unique private static final int SECONDARY_SLOT_SIZE = 18;

    @Unique private boolean musae_isHoveringChestSlot = false;
    @Unique private boolean musae_isHoveringSecondarySlot = false;
    @Unique private float musae_secondarySlotAlpha = 0.0f;

    public InventoryScreenMixin(InventoryMenu menu, net.minecraft.world.entity.player.Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Unique
    private Slot musae_findChestSlot() {
        java.util.List<Slot> slots = this.menu.slots;
        if (slots.size() > 6) {
            Slot slot = slots.get(6);
            if (slot.getSlotIndex() == 38) {
                return slot;
            }
        }
        for (Slot slot : slots) {
            if (slot.getSlotIndex() == 38 && slot.container instanceof net.minecraft.world.entity.player.Inventory) {
                return slot;
            }
        }
        return null;
    }

    @Inject(method = "renderBg", at = @At("TAIL"))
    private void musae_renderSecondarySlot(GuiGraphics graphics, float partialTick, int mouseX, int mouseY, CallbackInfo ci) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        if (ClientRaceHandler.getRace() != RaceType.QUIMERA_ALADA || !ClientRaceHandler.hasRaceElytra()) {
            musae_secondarySlotAlpha = 0.0f;
            return;
        }

        Slot chestSlot = musae_findChestSlot();
        if (chestSlot == null) {
            musae_secondarySlotAlpha = 0.0f;
            return;
        }

        int chestScreenX = this.leftPos + chestSlot.x;
        int chestScreenY = this.topPos + chestSlot.y;

        musae_isHoveringChestSlot = mouseX >= chestScreenX - 1 && mouseX < chestScreenX + 17
                && mouseY >= chestScreenY - 1 && mouseY < chestScreenY + 17;

        int secX = chestScreenX + SECONDARY_OFFSET_X;
        int secY = chestScreenY;

        musae_isHoveringSecondarySlot = mouseX >= secX - 1 && mouseX < secX + SECONDARY_SLOT_SIZE - 1
                && mouseY >= secY - 1 && mouseY < secY + SECONDARY_SLOT_SIZE - 1;

        boolean shouldShow = musae_isHoveringChestSlot || musae_isHoveringSecondarySlot;
        if (shouldShow) {
            musae_secondarySlotAlpha = Math.min(1.0f, musae_secondarySlotAlpha + partialTick * 0.3f);
        } else {
            musae_secondarySlotAlpha = Math.max(0.0f, musae_secondarySlotAlpha - partialTick * 0.3f);
        }

        if (musae_secondarySlotAlpha <= 0.01f) return;

        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 100);

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, musae_secondarySlotAlpha);

        graphics.blit(SLOT_TEXTURE, secX - 1, secY - 1, 7, 25, 18, 18);

        ItemStack elytraIcon = new ItemStack(Items.ELYTRA);
        if (musae_secondarySlotAlpha > 0.5f) {
            graphics.renderItem(elytraIcon, secX, secY);

            if (musae_isHoveringSecondarySlot) {
                graphics.fill(secX, secY, secX + 16, secY + 16,
                        0x80FFFFFF);
            }
        }

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();

        graphics.pose().popPose();
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void musae_renderSecondarySlotTooltip(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        if (ClientRaceHandler.getRace() != RaceType.QUIMERA_ALADA || !ClientRaceHandler.hasRaceElytra()) return;
        if (!musae_isHoveringSecondarySlot || musae_secondarySlotAlpha < 0.8f) return;

        java.util.List<Component> tooltip = new java.util.ArrayList<>();
        tooltip.add(Component.literal("§dAsas"));

        graphics.renderTooltip(this.font, tooltip, java.util.Optional.empty(), mouseX, mouseY);
    }
}