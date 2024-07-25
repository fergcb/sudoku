package uk.fergcb.sudoku.util;

public final class Base36 {

    private static final char[] digits = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    private Base36() {
        throw new IllegalStateException("Base36 is a static utility class and cannot be instantiated");
    }

    public static char toDigit(int n) {
        if (n < 0 || n > 35)
            throw new IllegalArgumentException(
                    String.format("Integer %d cannot be represented as a single base 36 digit.", n));

        return digits[n];
    }
}
