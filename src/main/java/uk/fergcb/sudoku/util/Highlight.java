package uk.fergcb.sudoku.util;

public record Highlight(HighlightType type, Color color, Position position) {
    public enum HighlightType {
        ROW, COL, BOX, CELL
    }
}
