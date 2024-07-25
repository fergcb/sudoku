package uk.fergcb.sudoku.solving;

import uk.fergcb.sudoku.Board;

public interface Solver {
    SolveResult solve(Board board);
}
