package uk.fergcb.sudoku.solving;

import uk.fergcb.sudoku.Board;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class SolveResult {

    // Static convenience functions
    public static SolveResult deadEnd() {
        return new DeadEnd();
    }

    public static SolveResult solution(Board board) {
        return new SolutionFound(List.of(board));
    }

    /**
     * Check whether the result has at least one solution (i.e. is not a failure)
     *
     * @return true if result has at least one solution, else false
     */
    public abstract boolean hasSolutions();

    /**
     * Check whether the result represents a "proper" sudoku, i.e. one with only one solution.
     *
     * @return true if result has only one solution, else false
     */
    public abstract boolean isProper();

    /**
     * Return the solutions found.
     *
     * @return the list of solutions
     * @throws IllegalStateException if result contains no solutions
     */
    public abstract List<Board> getSolutions() throws IllegalStateException;

    /**
     * Return the first available solution (for use with proper solutions)
     *
     * @return the first available solution
     * @throws IllegalStateException if result contains no solutions
     */
    public abstract Board getSolution() throws IllegalStateException;

    public SolveResult merge(SolveResult other) {
        if (this.hasSolutions() && other.hasSolutions()) return new SolutionFound(
                Stream.concat(this.getSolutions().stream(), other.getSolutions().stream())
                        .collect(Collectors.toList()));
        if (this.hasSolutions() && !other.hasSolutions()) return new SolutionFound(this.getSolutions());
        if (!this.hasSolutions() && other.hasSolutions()) return new SolutionFound(other.getSolutions());
        return deadEnd();
    }

    /**
     * Represents a failure to find a solution on the current path.
     * <p>
     * May also be used to represent a task giving up early.
     */
    public static class DeadEnd extends SolveResult {
        @Override
        public boolean hasSolutions() {
            return false;
        }

        @Override
        public boolean isProper() {
            return false;
        }

        @Override
        public List<Board> getSolutions() {
            throw new IllegalStateException("Cannot get solutions for dead end.");
        }

        @Override
        public Board getSolution() {
            throw new IllegalStateException("Cannot get solutions for dead end.");
        }
    }

    /**
     * Represents one or more solutions being found on the current path.
     */
    public static class SolutionFound extends SolveResult {

        private final List<Board> solutions;

        public SolutionFound(List<Board> solutions) {
            if (solutions.isEmpty())
                throw new IllegalArgumentException("A SolutionFound solve result must contain at least one solution.");
            this.solutions = solutions;
        }

        @Override
        public boolean hasSolutions() {
            return true;
        }

        @Override
        public boolean isProper() {
            return solutions.size() == 1;
        }


        @Override
        public List<Board> getSolutions() {
            return solutions;
        }

        @Override
        public Board getSolution() {
            return solutions.get(0);
        }
    }
}
