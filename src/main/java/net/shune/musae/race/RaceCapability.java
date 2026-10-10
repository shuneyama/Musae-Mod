package net.shune.musae.race;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.shune.musae.MusaeMod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RaceCapability {

    public static final Capability<RaceData> RACE_CAP = CapabilityManager.get(new CapabilityToken<>() {});
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(MusaeMod.MODID, "race_data");

    public static LazyOptional<RaceData> get(Entity entity) {
        return entity.getCapability(RACE_CAP);
    }

    public static RaceData resolve(Player player) {
        return get(player).orElse(new RaceData());
    }

    public static class Provider implements ICapabilitySerializable<CompoundTag> {

        private final RaceData data = new RaceData();
        private final LazyOptional<RaceData> optional = LazyOptional.of(() -> data);

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            if (cap == RACE_CAP) return optional.cast();
            return LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            return data.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            data.deserializeNBT(nbt);
        }

        public void invalidate() {
            optional.invalidate();
        }
    }
}
