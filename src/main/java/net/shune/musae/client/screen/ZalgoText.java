package net.shune.musae.client.screen;

import java.util.Random;

public class ZalgoText {

    private static final char[] ZALGO_UP = {
            '\u030d', '\u030e', '\u0304', '\u0305', '\u033f', '\u0311', '\u0306', '\u0310',
            '\u0352', '\u0357', '\u0351', '\u0307', '\u0308', '\u030a', '\u0342', '\u0343',
            '\u0344', '\u034a', '\u034b', '\u034c', '\u0303', '\u0302', '\u030c', '\u0350',
            '\u0300', '\u0301', '\u030b', '\u030f', '\u0312', '\u0313', '\u0314', '\u033d'
    };

    private static final char[] ZALGO_DOWN = {
            '\u0316', '\u0317', '\u0318', '\u0319', '\u031c', '\u031d', '\u031e', '\u031f',
            '\u0320', '\u0324', '\u0325', '\u0326', '\u0329', '\u032a', '\u032b', '\u032c',
            '\u032d', '\u032e', '\u032f', '\u0330', '\u0331', '\u0332', '\u0333', '\u0339',
            '\u033a', '\u033b', '\u033c', '\u0345', '\u0347', '\u0348', '\u0349', '\u034d'
    };

    private static final char[] ZALGO_MID = {
            '\u0334', '\u0335', '\u0336', '\u0337', '\u0338'
    };

    private static final char[] GLITCH_POOL =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%&*?".toCharArray();

    public static String corrupt(String text, float intensity, Random rng) {
        if (text == null || text.isEmpty()) return text;
        StringBuilder sb = new StringBuilder(text.length() * 4);
        int up = (int) (intensity * 4);
        int down = (int) (intensity * 4);
        int mid = (int) (intensity * 2);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            sb.append(c);
            if (c == ' ' || c == '\n') continue;
            for (int j = 0; j < up; j++) sb.append(ZALGO_UP[rng.nextInt(ZALGO_UP.length)]);
            for (int j = 0; j < down; j++) sb.append(ZALGO_DOWN[rng.nextInt(ZALGO_DOWN.length)]);
            for (int j = 0; j < mid; j++) sb.append(ZALGO_MID[rng.nextInt(ZALGO_MID.length)]);
        }
        return sb.toString();
    }

    public static String reveal(String target, float progress, Random rng) {
        if (target == null || target.isEmpty()) return target;
        progress = Math.max(0f, Math.min(1f, progress));
        int len = target.length();
        int fixed = (int) (len * progress);
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char real = target.charAt(i);
            if (i < fixed || real == ' ' || real == '!' || real == ',' || real == '.') {
                sb.append(real);
            } else {
                sb.append(GLITCH_POOL[rng.nextInt(GLITCH_POOL.length)]);
            }
        }
        return sb.toString();
    }
}