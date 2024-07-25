package uk.fergcb.sudoku;

import uk.fergcb.sudoku.util.Base36;
import uk.fergcb.sudoku.util.Color;
import uk.fergcb.sudoku.util.Highlight;
import uk.fergcb.sudoku.util.Position;
import uk.fergcb.sudoku.validation.ValidationResult;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.function.Function.identity;

public class Board {
    private final int size;
    private final int boxSize;
    private final int[][] grid;

    private Board(int[][] grid) {
        this.size = grid.length;
        this.boxSize = (int) Math.sqrt(size);
        this.grid = grid;
    }

    public int getSize() {
        return size;
    }

    public int getBoxSize() {
        return boxSize;
    }

    public int[][] getGrid() {
        return grid;
    }

    public static Board from(int[][] values) {
        final var size = values.length;
        if (Math.sqrt(size) % 1 != 0)
            throw new IllegalArgumentException("Board size must be a square number.");
        for (var row : values) {
            if (row.length != size)
                throw new IllegalArgumentException("Board must be square.");
        }

        return new Board(values);
    }

    public Board with(int value, Position pos) {
        int[][] newGrid = new int[size][size];
        for (int r = 0; r < size; r++) {
            System.arraycopy(grid[r], 0, newGrid[r], 0, size);
        }
        newGrid[pos.row()][pos.col()] = value;
        return new Board(newGrid);
    }

    public static Set<Position> getCellPositions(int size) {
        final var positions = new HashSet<Position>();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                positions.add(new Position(r, c));
            }
        }
        return positions;
    }

    public static Set<Integer> getCellRange(int size) {
        return IntStream.rangeClosed(1, size)
                .boxed()
                .collect(Collectors.toSet());
    }

    public static Set<Position> getInfluencedCells(Position cell, int size, int boxSize) {
        final var influencedCells = getBoxCells(cell.box(boxSize), boxSize);
        influencedCells.addAll(getRowCells(cell, size));
        influencedCells.addAll(getColCells(cell, size));
        influencedCells.remove(cell);
        return influencedCells;
    }

    public static Set<Position> getBoxCells(Position box, int boxSize) {
        int ro = box.row() * boxSize;
        int co = box.col() * boxSize;
        final var cells = new HashSet<Position>();
        for (int r = 0; r < boxSize; r++) {
            for (int c = 0; c < boxSize; c++) {
                cells.add(new Position(ro + r, co + c));
            }
        }
        return cells;
    }

    public static Set<Position> getRowCells(Position cell, int size) {
        return IntStream.range(0, size)
                .mapToObj(c -> new Position(cell.row(), c))
                .collect(Collectors.toSet());

    }

    public static Set<Position> getColCells(Position cell, int size) {
        return IntStream.range(0, size)
                .mapToObj(r -> new Position(r, cell.col()))
                .collect(Collectors.toSet());

    }

    public Set<Position> getCellPositions() {
        return Board.getCellPositions(size);
    }

    public Set<Position> getEmptyCells() {
        return getCellPositions()
                .stream()
                .filter(pos -> grid[pos.row()][pos.col()] == 0)
                .collect(Collectors.toSet());
    }

    public Set<Integer> getCellRange() {
        return Board.getCellRange(size);
    }

    public Set<Integer> getValidValues(Position cell) {
        final var values = getCellRange();
        final var influencedValues = getInfluencedCells(cell, size, boxSize)
                .stream()
                .map(pos -> grid[pos.row()][pos.col()])
                .collect(Collectors.toSet());
        values.removeAll(influencedValues);
        return values;
    }

    public String toPrettyString() {
        return toPrettyString(List.of());
    }

    public String toPrettyString(ValidationResult result) {
        if (result.isValid()) return toPrettyString();
        final var errors = result.getErrors();
        final var highlights = Stream.concat(
                        errors
                                .stream()
                                .map(error -> switch (error.type()) {
                                    case ROW -> new Highlight(Highlight.HighlightType.ROW, Color.RED,
                                            new Position(error.culprits().getFirst().row(), 0));
                                    case COL -> new Highlight(Highlight.HighlightType.COL, Color.RED,
                                            new Position(0, error.culprits().getFirst().col()));
                                    case BOX -> new Highlight(Highlight.HighlightType.BOX, Color.RED,
                                            new Position(error.culprits().getFirst().row(), error.culprits().getFirst().col()));
                                }),
                        errors
                                .stream()
                                .flatMap(err -> err.culprits().stream())
                                .map(pos -> new Highlight(Highlight.HighlightType.CELL, Color.BOLD, pos))
                )
                .collect(Collectors.toList());
        return toPrettyString(highlights);
    }

    public String toPrettyString(List<Highlight> highlights) {
        final var sb = new StringBuilder();

        final var rowHighlights = highlights.stream()
                .filter(h -> h.type() == Highlight.HighlightType.ROW)
                .collect(Collectors.toMap(h -> h.position().row(), identity()));
        final var colHighlights = highlights.stream()
                .filter(h -> h.type() == Highlight.HighlightType.COL)
                .collect(Collectors.toMap(h -> h.position().col(), identity()));
        final var boxHighlights = highlights.stream()
                .filter(h -> h.type() == Highlight.HighlightType.BOX)
                .collect(Collectors.toMap(h -> h.position().box(boxSize), identity()));
        final var cellHighlights = highlights.stream()
                .filter(h -> h.type() == Highlight.HighlightType.CELL)
                .distinct()
                .collect(Collectors.toMap(Highlight::position, identity()));

        sb.append("┏");
        for (int c = 0; c < size; c++) {
            sb.append("━━━");
            if (c != size - 1) {
                sb.append((c + 1) % boxSize == 0 ? "┳" : "┯");
            }
        }
        sb.append("┓\n");

        for (int r = 0; r < size; r++) {
            final var row = grid[r];
            final var rowHighlighted = rowHighlights.containsKey(r);
            for (int c = 0; c < size; c++) {
                final var cell = row[c];
                final var pos = new Position(r, c);
                sb.append(c % boxSize == 0 ? "┃" : "│");
                final var boxHighlighted = boxHighlights.containsKey(pos.box(boxSize));
                if (boxHighlighted) {
                    final var h = boxHighlights.get(pos.box(boxSize));
                    sb.append(h.color().bg);
                }
                if (rowHighlighted) {
                    final var h = rowHighlights.get(r);
                    sb.append(h.color().bg);
                }
                final var colHighlighted = colHighlights.containsKey(c);
                if (colHighlighted) {
                    final var h = colHighlights.get(c);
                    sb.append(h.color().bg);
                }
                if (rowHighlighted || colHighlighted || boxHighlighted) {
                    sb.append(Color.BLACK.fg);
                }
                final var cellHighlighted = cellHighlights.containsKey(pos);
                if (cellHighlighted) {
                    final var h = cellHighlights.get(pos);
                    sb.append(h.color().fg);
                }

                sb.append(" ")
                        .append(cell == 0 ? " " : Base36.toDigit(cell))
                        .append(" ");

                sb.append(Color.RESET.fg);
            }

            final var lastRow = r == size - 1;
            final var boxNextRow = (r + 1) % boxSize == 0;

            sb.append("┃\n");
            sb.append(lastRow ? "┗" : (boxNextRow ? "┣" : "┠"));
            for (int c = 0; c < size; c++) {
                sb.append(boxNextRow ? "━━━" : "───");
                if (c != size - 1) {
                    if (lastRow) {
                        sb.append((c + 1) % boxSize == 0 ? "┻" : "┷");
                    } else {
                        if (boxNextRow)
                            sb.append((c + 1) % boxSize == 0 ? "╋" : "┿");
                        else
                            sb.append((c + 1) % boxSize == 0 ? "╂" : "┼");
                    }
                }
            }
            sb.append(lastRow ? "┛" : (boxNextRow ? "┫" : "┨"));
            sb.append("\n");
        }

        return sb.toString();
    }
}
