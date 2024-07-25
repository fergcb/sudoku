package uk.fergcb.sudoku.validation;

import uk.fergcb.sudoku.util.Position;

import java.util.List;

public record ValidationError(ValidationType type, List<Position> culprits) { }
