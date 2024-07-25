package uk.fergcb.sudoku.validation;

import uk.fergcb.sudoku.Board;

public interface Validator {
    ValidationResult validate(Board board);
}
