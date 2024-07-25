package uk.fergcb.sudoku;

import uk.fergcb.sudoku.generation.BasicGenerator;
import uk.fergcb.sudoku.solving.ForkJoinSolver;
import uk.fergcb.sudoku.util.Position;
import uk.fergcb.sudoku.validation.ThreadPoolValidator;

import java.util.List;

public class Main {

    private static final List<int[][]> unsolved = List.of(
            new int[][]{
                    {5, 3, 0, 0, 7, 0, 0, 0, 0},
                    {6, 0, 0, 1, 9, 5, 0, 0, 0},
                    {0, 9, 8, 0, 0, 0, 0, 6, 0},
                    {8, 0, 0, 0, 6, 0, 0, 0, 3},
                    {4, 0, 0, 8, 0, 3, 0, 0, 1},
                    {7, 0, 0, 0, 2, 0, 0, 0, 6},
                    {0, 6, 0, 0, 0, 0, 2, 8, 0},
                    {0, 0, 0, 4, 1, 9, 0, 0, 5},
                    {0, 0, 0, 0, 8, 0, 0, 7, 9},
            },
            new int[][]{
                    {0, 0, 6, 5, 0, 0, 0, 0, 8},
                    {0, 9, 5, 0, 0, 0, 0, 2, 0},
                    {7, 0, 0, 9, 0, 0, 3, 0, 0},
                    {0, 0, 0, 0, 4, 0, 2, 7, 0},
                    {0, 0, 0, 8, 7, 3, 0, 0, 0},
                    {0, 7, 9, 0, 5, 0, 0, 0, 0},
                    {0, 0, 2, 0, 0, 8, 0, 0, 9},
                    {0, 5, 0, 0, 0, 0, 8, 1, 0},
                    {3, 0, 0, 0, 0, 5, 4, 0, 0},
            },
            new int[][]{
                    {0, 8, 0, 0, 4, 0, 0, 0, 0},
                    {9, 0, 0, 0, 0, 0, 2, 0, 0},
                    {0, 0, 0, 0, 1, 0, 0, 0, 0},
                    {1, 0, 0, 5, 0, 0, 0, 0, 8},
                    {5, 6, 0, 1, 0, 2, 0, 0, 0},
                    {0, 0, 0, 0, 0, 4, 0, 6, 5},
                    {0, 0, 0, 0, 0, 5, 0, 7, 9},
                    {0, 0, 9, 0, 0, 0, 3, 0, 0},
                    {2, 0, 3, 7, 0, 0, 0, 8, 0},
            }
    );

    public static void main(String[] args) {
//        testSolver();
//        testGenerator();
        testValidator();
    }

    private static void testSolver() {
        final var solver = new ForkJoinSolver(true);

        for (int i = 0; i < unsolved.size(); i++) {
            final var puzzle = Board.from(unsolved.get(i));
            System.out.printf("\n\n=== Puzzle #%d: ===%n", i + 1);
            System.out.println(puzzle.toPrettyString());


            final var result = solver.solve(puzzle);
            if (result.hasSolutions()) {
                System.out.println("\n--- Solution: ---");
                final var solution = result.getSolution();
                System.out.println(solution.toPrettyString());
            } else {
                System.out.println("\nFailed to solve.");
            }
        }
    }

    private static void testGenerator() {
        final var generator = new BasicGenerator(9, 0.3);

        for (int i = 0; i < 10; i++) {
            final var puzzle = generator.generate();
            System.out.printf("\n\n=== Puzzle #%d: ===%n", i + 1);
            System.out.println(puzzle.toSideBySideString());
        }
    }

    private static void testValidator() {
        final var solver = new ForkJoinSolver(true);
        final var puzzle = Board.from(unsolved.get(0));

        final var solutions = List.of(
                puzzle,
                solver.solve(puzzle).getSolution(),
                puzzle
                        .with(5, new Position(8, 6))
                        .with(1, new Position(4, 4))
        );

        final var validator = new ThreadPoolValidator();
        for (int i = 0; i < solutions.size(); i++) {
            System.out.printf("%n%n=== Solution #%d ===%n", i + 1);
            final var solution = solutions.get(i);
            final var result = validator.validate(solution);
            if (result.isValid()) {
                System.out.println("Solution is valid!");
            } else {
                System.out.println("Errors found!");
                System.out.println(solution.toPrettyString(result));
            }
        }
    }
}
