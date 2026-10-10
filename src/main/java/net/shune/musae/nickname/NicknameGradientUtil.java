package net.shune.musae.nickname;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.util.ArrayList;
import java.util.List;


public class NicknameGradientUtil {

    public static int parseHex(String hex) {
        hex = hex.trim();
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }
        if (hex.length() != 6) {
            throw new IllegalArgumentException("Cor hex inválida: " + hex + " (use o formato #RRGGBB)");
        }
        return Integer.parseInt(hex, 16);
    }

    public static int interpolate(int color1, int color2, float ratio) {
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int r = Math.round(r1 + (r2 - r1) * ratio);
        int g = Math.round(g1 + (g2 - g1) * ratio);
        int b = Math.round(b1 + (b2 - b1) * ratio);

        return (r << 16) | (g << 8) | b;
    }

    public static MutableComponent applyGradient(String text, List<Integer> colors) {
        if (text.isEmpty()) {
            return Component.empty();
        }

        if (colors.size() == 1) {
            return Component.literal(text)
                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(colors.get(0))));
        }

        MutableComponent result = Component.empty();
        int length = text.length();

        for (int i = 0; i < length; i++) {
            float position = length == 1 ? 0f : (float) i / (length - 1);

            float segmentLength = 1.0f / (colors.size() - 1);
            int segmentIndex = Math.min((int) (position / segmentLength), colors.size() - 2);
            float segmentRatio = (position - segmentIndex * segmentLength) / segmentLength;

            int color = interpolate(colors.get(segmentIndex), colors.get(segmentIndex + 1), segmentRatio);

            result.append(Component.literal(String.valueOf(text.charAt(i)))
                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(color))));
        }

        return result;
    }

    public static List<Integer> parseColors(List<String> hexList) {
        List<Integer> colors = new ArrayList<>();
        for (String hex : hexList) {
            colors.add(parseHex(hex));
        }
        return colors;
    }

    public static String serializeColors(List<String> hexList) {
        return String.join(",", hexList);
    }

    public static List<String> deserializeColors(String serialized) {
        List<String> colors = new ArrayList<>();
        if (serialized == null || serialized.isEmpty()) {
            return colors;
        }
        for (String part : serialized.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                colors.add(trimmed);
            }
        }
        return colors;
    }
}
