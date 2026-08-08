package com.urlshortener;

public class Base62 {
    private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = ALPHABET.length();

    private Base62() {}

    public static String encode(long value) {
        if (value <= 0) return "0";
        StringBuilder sb = new StringBuilder();
        while (value > 0) {
            sb.append(ALPHABET.charAt((int) (value % BASE)));
            value /= BASE;
        }
        return sb.reverse().toString();
    }

    // Used on startup to recover the counter from persisted auto-generated codes.
    public static long decode(String code) {
        long result = 0;
        for (char c : code.toCharArray()) {
            int idx = ALPHABET.indexOf(c);
            if (idx == -1) throw new IllegalArgumentException("Invalid Base62 character: " + c);
            result = result * BASE + idx;
        }
        return result;
    }
}
