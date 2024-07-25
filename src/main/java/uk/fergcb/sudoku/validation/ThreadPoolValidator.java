package uk.fergcb.sudoku.validation;

import uk.fergcb.sudoku.Board;
import uk.fergcb.sudoku.util.Position;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolValidator implements Validator {

    private static final ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    @Override
    public ValidationResult validate(Board board) {
        var result = ValidationResult.valid();

        try (var pool = Executors.newFixedThreadPool(3)) {
            final var cs = new ExecutorCompletionService<ValidationResult>(pool);
            cs.submit(() -> validateRows(board));
            cs.submit(() -> validateCols(board));
            cs.submit(() -> validateBoxes(board));

            for (int i = 0; i < 3; i++) {
                result = result.merge(cs.take().get());
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    private ValidationResult validateRows(Board board) {
        final var errors = new ArrayList<ValidationError>();

        for (int r = 0; r < board.getSize(); r++) {
            final var seen = new HashMap<Integer, Position>();
            for (int c = 0; c < board.getSize(); c++) {
                final var value = board.getGrid()[r][c];
                if (value == 0) continue;
                final var currentPos = new Position(r, c);
                if (seen.containsKey(value))
                    errors.add(new ValidationError(ValidationType.ROW, List.of(seen.get(value), currentPos)));
                seen.put(value, currentPos);
            }
        }

        if (!errors.isEmpty()) return ValidationResult.invalid(errors);
        return ValidationResult.valid();
    }

    private ValidationResult validateCols(Board board) {
        final var errors = new ArrayList<ValidationError>();

        for (int c = 0; c < board.getSize(); c++) {
            final var seen = new HashMap<Integer, Position>();
            for (int r = 0; r < board.getSize(); r++) {
                final var value = board.getGrid()[r][c];
                if (value == 0) continue;
                final var currentPos = new Position(r, c);
                if (seen.containsKey(value))
                    errors.add(new ValidationError(ValidationType.COL, List.of(seen.get(value), currentPos)));
                seen.put(value, currentPos);
            }
        }

        if (!errors.isEmpty()) return ValidationResult.invalid(errors);
        return ValidationResult.valid();
    }

    private ValidationResult validateBoxes(Board board) {
        final var errors = new ArrayList<ValidationError>();
        final var boxSize = board.getBoxSize();

        // Iterate over the boxes
        for (int br = 0; br < boxSize; br++) {
            for (int bc = 0; bc < boxSize; bc++) {
                // Find the origin of the box
                final var ro = br * boxSize;
                final var co = bc * boxSize;
                // Iterate over each cell in the box
                final var seen = new HashMap<Integer, Position>();
                for (int r = ro; r < ro + boxSize; r++) {
                    for (int c = co; c < co + boxSize; c++) {
                        final var value = board.getGrid()[r][c];
                        if (value == 0) continue;
                        final var currentPos = new Position(r, c);
                        if (seen.containsKey(value))
                            errors.add(new ValidationError(ValidationType.BOX, List.of(seen.get(value), currentPos)));
                        seen.put(value, currentPos);
                    }
                }
            }
        }

        if (!errors.isEmpty()) return ValidationResult.invalid(errors);
        return ValidationResult.valid();
    }
}
