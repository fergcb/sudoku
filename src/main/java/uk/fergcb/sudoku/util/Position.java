package uk.fergcb.sudoku.util;

public record Position(int row, int col) {
    public Position box(int boxSize) {
        return new Position(this.row / boxSize, this.col / boxSize);
    }
}
