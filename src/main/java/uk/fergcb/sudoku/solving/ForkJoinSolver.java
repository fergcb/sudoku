package uk.fergcb.sudoku.solving;

import uk.fergcb.sudoku.Board;
import uk.fergcb.sudoku.util.Position;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class ForkJoinSolver implements Solver {

    private final boolean singleSolution;

    public ForkJoinSolver() {
        this(true);
    }

    public ForkJoinSolver(boolean singleSolution) {
        this.singleSolution = singleSolution;
    }

    @Override
    public SolveResult solve(Board board) {
        final var processors = Runtime.getRuntime().availableProcessors();
        try (var pool = new ForkJoinPool(processors)) {
            final var task = new Task(board, singleSolution, new CancellationFlag());
            return pool.invoke(task);
        }
    }

    /**
     * Used to signal between recursive tasks whether to give up
     * (e.g. if a concurrent task has already found a solution)
     */
    private static class CancellationFlag {
        private volatile boolean isCancelled = false;

        public boolean isCancelled() {
            return isCancelled;
        }

        public void cancel() {
            this.isCancelled = true;
        }
    }

    private static class Task extends RecursiveTask<SolveResult> {

        private final Board board;
        private final Set<Position> empty;
        private final boolean singleSolution;
        private final CancellationFlag flag;

        private Task(Board board, boolean singleSolution, CancellationFlag flag, Set<Position> empty) {
            this.board = board;
            this.singleSolution = singleSolution;
            this.flag = flag;
            this.empty = empty;
        }

        public Task(Board board, boolean singleSolution, CancellationFlag flag) {
            this(
                    board,
                    singleSolution,
                    flag, Set.of(board.getEmptyCells().toArray(new Position[0]))
            );
        }

        @Override
        protected SolveResult compute() {
            // Base case - another task wants us to give up
            if (flag.isCancelled()) return SolveResult.deadEnd();
            // Base case - the board is full, we've found a solution
            if (empty.isEmpty()) return SolveResult.solution(board);

            // Pick the next cell at random
            final var rand = new Random();
            final var nextCell = empty.toArray(new Position[0])[rand.nextInt(empty.size())];

            // Base case - we've run out of valid options
            final var options = board.getValidValues(nextCell);
            if (options.isEmpty()) return SolveResult.deadEnd();

            // Get the list of cells which will be empty in the recursive case
            final var nextEmpty = new HashSet<>(empty);
            nextEmpty.remove(nextCell);

            // Create tasks for all possible branches
            final var branches = new ArrayList<Task>();
            for (var option : options) {
                final var nextBoard = board.with(option, nextCell);
                final var branch = new Task(nextBoard, singleSolution, flag, nextEmpty);
                branches.add(branch);
            }

            // Retain one task to keep on this thread
            final var trunk = branches.remove(0);

            // Fork the rest of the branches
            for (var branch : branches) {
                branch.fork();
            }

            // Compute this task
            var result = trunk.compute();
            if (singleSolution && result.hasSolutions()) {
                flag.cancel();
                return result;
            }

            // Wait for branches to finish
            for (var branch : branches) {
                final var branchResult = branch.join();
                if (singleSolution && branchResult.hasSolutions()) {
                    flag.cancel();
                    return branchResult;
                }
                result = result.merge(branchResult);
            }

            return result;
        }
    }
}
