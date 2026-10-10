package net.shune.musae.itens;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class ItensBanidosUtil {

    public static boolean isBanido(MinecraftServer servidor, ItemStack stack) {
        if (servidor == null || stack.isEmpty()) return false;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id != null && ConfiguracaoItensBanidos.isBanido(servidor, id);
    }

    public static boolean isBanido(ServerPlayer player, ItemStack stack) {
        if (player == null || player.getServer() == null || stack.isEmpty()) return false;

        MinecraftServer servidor = player.getServer();
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id == null || !ConfiguracaoItensBanidos.isBanido(servidor, id)) return false;

        return !PermissoesItensBanidos.isPermitido(servidor, player.getUUID(), id);
    }
}
