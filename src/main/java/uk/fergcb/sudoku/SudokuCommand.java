package uk.fergcb.sudoku;

import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;
import uk.fergcb.sudoku.generation.BasicGenerator;

@Command(name = "sudoku", mixinStandardHelpOptions = true,
        description = "Generates, validates and solves sudoku puzzles.")
public class SudokuCommand {

    @Spec
    CommandSpec spec;

    @Command(name = "generate")
    public int generate(
            @Option(names = {"-s", "--size"}, defaultValue = "9",
                    description = "side length of the grid") int size,
            @Option(names = {"-c", "--coverage"}, defaultValue = "30",
                    description = "percentage of cells to remain visible (0-100)") int coverage,
            @Option(names = {"-S", "--include-solution"},
                    description = "print solution alongside puzzle") boolean includeSolution
    ) {
        if (coverage < 0 || coverage > 100) {
            throw new ParameterException(spec.commandLine(), String.format("Invalid value '%d' for option '--coverage': " +
                    "value must be a percentage between 0-100 (inclusive).", coverage));
        }
        if (Math.sqrt(size) % 1 != 0) {
            throw new ParameterException(spec.commandLine(), String.format("Invalid value '%d' for option '--size': " +
                    "value must be a square number.", size));
        }
        if (size < 4 || size > 25) {
            throw new ParameterException(spec.commandLine(), String.format("Invalid value '%d' for option '--size': " +
                    "value must be in range 4-25 (inclusive).", size));
        }
        final var generator = new BasicGenerator(size, coverage / 100D);
        final var puzzle = generator.generate();

        System.out.println("=== Puzzle: ===");
        System.out.println(puzzle.puzzle().toPrettyString());

        if (includeSolution) {
            System.out.println("\n\n=== Solution: ===");
            System.out.println(puzzle.solution().toPrettyString());
        }

        return 0;
    }
}
