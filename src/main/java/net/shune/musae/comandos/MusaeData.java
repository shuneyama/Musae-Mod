package net.shune.musae.comandos;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

public class MusaeData extends SavedData {
    private static final String NAME = "musae";

    private CompoundTag root = new CompoundTag();

    public static MusaeData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(MusaeData::load, MusaeData::new, NAME);
    }

    public static MusaeData load(CompoundTag tag) {
        MusaeData d = new MusaeData();
        d.root = tag.getCompound("root");
        return d;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.put("root", root);
        return tag;
    }

    public CompoundTag root(String key) {
        if (!root.contains(key, 10)) {
            root.put(key, new CompoundTag());
        }
        setDirty();
        return root.getCompound(key);
    }
}
