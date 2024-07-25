package uk.fergcb.sudoku.util;

public enum Color {

    RESET("\u001B[0m", "\u001B[0m"),
    BLACK("\u001B[30m", "\u001B[40m"),
    RED("\u001B[31m", "\u001B[41m"),
    GREEN("\u001B[32m", "\u001B[42m"),
    YELLOW("\u001B[33m", "\u001B[43m"),
    BLUE("\u001B[34m", "\u001B[44m"),
    PURPLE("\u001B[35m", "\u001B[45m"),
    CYAN("\u001B[36m", "\u001B[46m"),
    WHITE("\u001B[37m", "\u001B[47m"),
    BOLD("\u001B[1m", "\u001B[1m");

    public final String fg;
    public final String bg;

    Color(String fg, String bg) {
        this.fg = fg;
        this.bg = bg;
    }
}
