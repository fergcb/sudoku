package uk.fergcb.sudoku.generation;

import uk.fergcb.sudoku.Board;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public record Puzzle(Board puzzle, Board solution) {
    /**
     * Display both the puzzle and solution side-by-side
     * @return the pretty string
     */
    public String toSideBySideString() {
        final var puzzleString = puzzle.toPrettyString();
        final var solutionString = solution.toPrettyString();
        final var puzzleLines = puzzleString.split("\n");
        final var solutionLines = solutionString.split("\n");
        return IntStream.range(0, puzzleLines.length)
                .mapToObj(i -> puzzleLines[i] + "\t\t\t" + solutionLines[i])
                .collect(Collectors.joining("\n"));
    }
}
