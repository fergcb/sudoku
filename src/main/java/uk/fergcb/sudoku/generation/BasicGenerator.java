package uk.fergcb.sudoku.generation;

import uk.fergcb.sudoku.Board;
import uk.fergcb.sudoku.solving.ForkJoinSolver;
import uk.fergcb.sudoku.solving.Solver;
import uk.fergcb.sudoku.util.Position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.IntStream;

public class BasicGenerator implements Generator {

    private final int size;
    private final double coverage;
    private final Solver solver = new ForkJoinSolver(true);

    public BasicGenerator(int size, double coverage) {
        if (size < 4 || Math.sqrt(size) % 1 != 0)
            throw new IllegalArgumentException("Puzzle size must be a square number, 4 or more.");
        if (coverage < 0 || coverage > 1)
            throw new IllegalArgumentException("Puzzle coverage must satisfy 0 <= coverage <= 1.");
        this.size = size;
        this.coverage = coverage;
    }

    @Override
    public Puzzle generate() {
        final var seedBoard = generateSeedBoard();
        final var solution = generateSolution(seedBoard);
        final var puzzle = hideCells(solution);
        return new Puzzle(puzzle, solution);
    }

    /**
     * Generate a grid with some cells randomly allocated, from which to generate a solved board.
     *
     * @return the seed board
     */
    private Board generateSeedBoard() {
        final var seedGrid = new int[size][size];

        final var boxSize = (int) Math.sqrt(size);
        final var diagonalBoxes = IntStream
                .range(0, boxSize)
                .mapToObj(i -> new ArrayList<>(Board.getBoxCells(new Position(i, i), boxSize)));
        diagonalBoxes.forEach(box -> {
            Collections.shuffle(box);
            for (int i = 0; i < size; i++) {
                final var pos = box.get(i);
                seedGrid[pos.row()][pos.col()] = i + 1;
            }
        });

        return Board.from(seedGrid);
    }

    /**
     * Take a seed board and find a possible solution for it.
     *
     * @param seedBoard The starting point for finding a solution.
     * @return a solved sudoku board
     */
    private Board generateSolution(Board seedBoard) {
        final var solveResult = solver.solve(seedBoard);
        if (!solveResult.hasSolutions())
            throw new IllegalStateException("Failed to solve seed board.");
        return solveResult.getSolution();
    }

    /**
     * Hide a random selection of cells in a solved grid.
     * <p>
     * The pattern of hidden cells are rotationally symmetrical over 180 degrees.
     *
     * @param solution A solved sudoku to hide cells on
     * @return A sudoku with hidden cells
     */
    private Board hideCells(Board solution) {
        var puzzle = solution;
        final var numCellsToHide = (int) (size * size * (1 - coverage));
        final var allCells = new ArrayList<>(Board.getCellPositions(size));
        Collections.shuffle(allCells);
        final var cellsToHide = allCells.subList(0, numCellsToHide / 2);
        for (var cell : cellsToHide) {
            puzzle = puzzle.with(0, cell);
            puzzle = puzzle.with(0, new Position(size - cell.row() - 1, size - cell.col() - 1));
        }
        return puzzle;
    }
}
