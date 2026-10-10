package net.shune.musae.nickname;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.shune.musae.MusaeMod;
import net.shune.musae.auth.AutorizacaoServidor;

import java.util.*;

@Mod.EventBusSubscriber(modid = MusaeMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class NicknameArgumentResolver {

    @SubscribeEvent
    public static void onCommand(CommandEvent event) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        if (event.getParseResults().getContext().getSource().getServer() == null) return;

        MinecraftServer server = event.getParseResults().getContext().getSource().getServer();
        NicknameData data = NicknameData.get(server);

        if (data.getAllNicknames().isEmpty()) return;

        String input = event.getParseResults().getReader().getString();

        int firstSpace = input.indexOf(' ');
        if (firstSpace == -1) return;

        List<Token> tokens = tokenize(input);
        if (tokens.size() <= 1) return;

        int maxNickWords = 1;
        for (NicknameData.NicknameEntry entry : data.getAllNicknames().values()) {
            int w = entry.nickname().trim().split("\\s+").length;
            if (w > maxNickWords) maxNickWords = w;
        }

        boolean modified = false;
        StringBuilder newInput = new StringBuilder();

        int i = 0;
        while (i < tokens.size()) {
            Token tk = tokens.get(i);

            if (i == 0 || tk.quoted || tk.text.startsWith("@") || tk.text.startsWith("/")) {
                if (newInput.length() > 0) newInput.append(" ");
                newInput.append(tk.original);

                if (tk.quoted && i > 0) {
                    UUID uuid = data.getUUIDByNickname(tk.text);
                    if (uuid != null) {
                        ServerPlayer realPlayer = server.getPlayerList().getPlayer(uuid);
                        if (realPlayer != null) {
                            newInput.setLength(newInput.length() - tk.original.length());
                            newInput.append(realPlayer.getGameProfile().getName());
                            modified = true;
                        }
                    }
                }
                i++;
                continue;
            }

            int matchedLen = 0;
            String matchedRealName = null;
            int maxTry = Math.min(maxNickWords, tokens.size() - i);
            for (int len = maxTry; len >= 1; len--) {
                StringBuilder candidate = new StringBuilder();
                boolean canMerge = true;
                for (int j = 0; j < len; j++) {
                    Token t = tokens.get(i + j);
                    if (t.quoted || t.text.startsWith("@") || t.text.startsWith("/")) {
                        canMerge = false;
                        break;
                    }
                    if (j > 0) candidate.append(" ");
                    candidate.append(t.text);
                }
                if (!canMerge) continue;

                UUID uuid = data.getUUIDByNickname(candidate.toString());
                if (uuid != null) {
                    ServerPlayer realPlayer = server.getPlayerList().getPlayer(uuid);
                    if (realPlayer != null) {
                        matchedLen = len;
                        matchedRealName = realPlayer.getGameProfile().getName();
                        break;
                    }
                }
            }

            if (matchedLen > 0) {
                if (newInput.length() > 0) newInput.append(" ");
                newInput.append(matchedRealName);
                i += matchedLen;
                modified = true;
            } else {
                if (newInput.length() > 0) newInput.append(" ");
                newInput.append(tk.original);
                i++;
            }
        }

        if (modified) {
            var dispatcher = server.getCommands().getDispatcher();
            var source = event.getParseResults().getContext().getSource();
            event.setParseResults(dispatcher.parse(newInput.toString(), source));
        }
    }

    private static List<Token> tokenize(String input) {
        List<Token> result = new ArrayList<>();
        int n = input.length();
        int i = 0;
        while (i < n) {
            while (i < n && Character.isWhitespace(input.charAt(i))) i++;
            if (i >= n) break;

            char c = input.charAt(i);
            if (c == '"') {
                int start = i;
                i++;
                StringBuilder inner = new StringBuilder();
                while (i < n && input.charAt(i) != '"') {
                    inner.append(input.charAt(i));
                    i++;
                }
                if (i < n) i++;
                result.add(new Token(inner.toString(), input.substring(start, i), true));
            } else {
                int start = i;
                while (i < n && !Character.isWhitespace(input.charAt(i))) i++;
                String raw = input.substring(start, i);
                result.add(new Token(raw, raw, false));
            }
        }
        return result;
    }

    private static final class Token {
        final String text;
        final String original;
        final boolean quoted;
        Token(String text, String original, boolean quoted) {
            this.text = text;
            this.original = original;
            this.quoted = quoted;
        }
    }

    public static Collection<String> getPlayerNamesWithNicknames(MinecraftServer server) {
        Set<String> names = new LinkedHashSet<>();

        NicknameData data = NicknameData.get(server);
        for (NicknameData.NicknameEntry entry : data.getAllNicknames().values()) {
            names.add(entry.nickname());
        }

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            names.add(player.getGameProfile().getName());
        }

        return names;
    }
}