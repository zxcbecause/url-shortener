package io.github.zxcbecause.shortener.link;

import java.security.SecureRandom;
import java.util.random.RandomGenerator;

/**
 * Base62 alphabet helpers (0-9, a-z, A-Z).
 */
public final class Base62 {

    static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = ALPHABET.length();

    private Base62() {
    }

    public static String encode(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("value must not be negative");
        }
        if (value == 0) {
            return "0";
        }
        StringBuilder sb = new StringBuilder();
        while (value > 0) {
            sb.append(ALPHABET.charAt((int) (value % BASE)));
            value /= BASE;
        }
        return sb.reverse().toString();
    }

    public static long decode(String text) {
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("text must not be empty");
        }
        long result = 0;
        for (char c : text.toCharArray()) {
            int digit = ALPHABET.indexOf(c);
            if (digit < 0) {
                throw new IllegalArgumentException("Invalid Base62 character: " + c);
            }
            result = Math.addExact(Math.multiplyExact(result, BASE), digit);
        }
        return result;
    }

    /** Random code of the given length. 62^7 is about 3.5 trillion combinations. */
    public static String random(int length, RandomGenerator random) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(BASE)));
        }
        return sb.toString();
    }

    public static String random(int length) {
        return random(length, new SecureRandom());
    }
}
