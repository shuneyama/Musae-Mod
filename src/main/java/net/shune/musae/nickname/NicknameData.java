package net.shune.musae.nickname;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.shune.musae.MusaeMod;
import net.shune.musae.network.NetworkHandler;
import net.shune.musae.network.SyncAllNicknamesPacket;
import net.shune.musae.network.SyncNicknamePacket;

import java.util.*;

public class NicknameData extends SavedData {

    private static final String DATA_NAME = "musae_nicknames";

    private final Map<UUID, NicknameEntry> nicknames = new HashMap<>();
    private final Map<String, UUID> nicknameToUUID = new HashMap<>();

    public NicknameData() {
        super();
    }

    public NicknameData(CompoundTag tag) {
        super();
        load(tag);
    }

    private void load(CompoundTag tag) {
        CompoundTag nicks = tag.getCompound("nicknames");
        for (String uuidStr : nicks.getAllKeys()) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                CompoundTag entry = nicks.getCompound(uuidStr);
                String nickname = entry.getString("name");
                String colorsStr = entry.getString("colors");
                List<String> colors = NicknameGradientUtil.deserializeColors(colorsStr);

                NicknameEntry nicknameEntry = new NicknameEntry(nickname, colors);
                nicknames.put(uuid, nicknameEntry);
                nicknameToUUID.put(nickname.toLowerCase(Locale.ROOT), uuid);
            } catch (Exception e) {
                MusaeMod.LOGGER.warn("[MusaeMod] Erro ao carregar nickname para UUID {}: {}", uuidStr, e.getMessage());
            }
        }
        MusaeMod.LOGGER.info("[MusaeMod] {} nicknames carregados.", nicknames.size());
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag nicks = new CompoundTag();
        for (Map.Entry<UUID, NicknameEntry> entry : nicknames.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString("name", entry.getValue().nickname());
            entryTag.putString("colors", NicknameGradientUtil.serializeColors(entry.getValue().colors()));
            nicks.put(entry.getKey().toString(), entryTag);
        }
        tag.put("nicknames", nicks);
        return tag;
    }

    public static NicknameData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                NicknameData::new,
                NicknameData::new,
                DATA_NAME
        );
    }

    public void setNickname(UUID uuid, String nickname, List<String> colors) {
        NicknameEntry old = nicknames.get(uuid);
        if (old != null) {
            nicknameToUUID.remove(old.nickname().toLowerCase(Locale.ROOT));
        }

        NicknameEntry entry = new NicknameEntry(nickname, colors);
        nicknames.put(uuid, entry);
        nicknameToUUID.put(nickname.toLowerCase(Locale.ROOT), uuid);
        setDirty();
    }

    public void removeNickname(UUID uuid) {
        NicknameEntry old = nicknames.remove(uuid);
        if (old != null) {
            nicknameToUUID.remove(old.nickname().toLowerCase(Locale.ROOT));
        }
        setDirty();
    }

    public NicknameEntry getNickname(UUID uuid) {
        return nicknames.get(uuid);
    }

    public boolean isNicknameTaken(String nickname, UUID excludeUUID) {
        UUID existing = nicknameToUUID.get(nickname.toLowerCase(Locale.ROOT));
        return existing != null && !existing.equals(excludeUUID);
    }

    public UUID getUUIDByNickname(String nickname) {
        return nicknameToUUID.get(nickname.toLowerCase(Locale.ROOT));
    }

    public Map<UUID, NicknameEntry> getAllNicknames() {
        return Collections.unmodifiableMap(nicknames);
    }

    public MutableComponent getStyledNickname(UUID uuid) {
        NicknameEntry entry = nicknames.get(uuid);
        if (entry == null) return null;

        if (entry.colors().isEmpty()) {
            return Component.literal(entry.nickname());
        }

        List<Integer> parsedColors = NicknameGradientUtil.parseColors(entry.colors());
        return NicknameGradientUtil.applyGradient(entry.nickname(), parsedColors);
    }

    public void syncToPlayer(ServerPlayer player) {
        Map<UUID, SyncAllNicknamesPacket.NickEntry> entries = new HashMap<>();
        for (Map.Entry<UUID, NicknameEntry> entry : nicknames.entrySet()) {
            entries.put(entry.getKey(), new SyncAllNicknamesPacket.NickEntry(
                    entry.getValue().nickname(),
                    NicknameGradientUtil.serializeColors(entry.getValue().colors())
            ));
        }

        NetworkHandler.sendToPlayer(player, new SyncAllNicknamesPacket(entries));
    }

    public void syncToAll(MinecraftServer server, UUID targetUUID) {
        NicknameEntry entry = nicknames.get(targetUUID);
        SyncNicknamePacket packet;
        if (entry != null) {
            packet = new SyncNicknamePacket(
                    targetUUID,
                    entry.nickname(),
                    NicknameGradientUtil.serializeColors(entry.colors())
            );
        } else {
            packet = new SyncNicknamePacket(targetUUID, "", "");
        }

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            NetworkHandler.sendToPlayer(player, packet);
        }
    }


    public record NicknameEntry(String nickname, List<String> colors) {
        public NicknameEntry {
            colors = List.copyOf(colors);
        }
    }
}