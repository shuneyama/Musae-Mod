package net.shune.musae.mixin.give;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.commands.GiveCommand;
import net.minecraft.server.level.ServerPlayer;
import net.shune.musae.MusaeMod;
import net.shune.musae.auth.AutorizacaoServidor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(GiveCommand.class)
public abstract class GiveCommandMixin {

    @Inject(method = "giveItem", at = @At("HEAD"), cancellable = true)
    private static void musae_bloquearGiveDeItensDoMod(CommandSourceStack fonte, ItemInput item,
            Collection<ServerPlayer> alvos, int quantidade, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        if (AutorizacaoServidor.isAutorizado()) return;

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item.getItem());
        if (id != null && id.getNamespace().equals(MusaeMod.MODID)) {
            cir.setReturnValue(0);
        }
    }
}
