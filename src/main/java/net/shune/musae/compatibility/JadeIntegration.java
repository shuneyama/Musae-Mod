package net.shune.musae.compatibility;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.shune.musae.auth.AutorizacaoServidor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin
public class JadeIntegration implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(
                PlayerNameHider.INSTANCE,
                Player.class
        );
    }

    public enum PlayerNameHider implements IEntityComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
            if (!AutorizacaoServidor.isAutorizado()) return;
            if (accessor.getEntity() instanceof Player) {
                tooltip.clear();
                tooltip.add(Component.literal("???")
                        .withStyle(ChatFormatting.OBFUSCATED)
                        .withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        @Override
        public ResourceLocation getUid() {
            return ResourceLocation.fromNamespaceAndPath("musae", "player_name_hider");
        }

        @Override
        public boolean isRequired() {
            return true;
        }

        @Override
        public int getDefaultPriority() {
            return Integer.MAX_VALUE;
        }
    }
}