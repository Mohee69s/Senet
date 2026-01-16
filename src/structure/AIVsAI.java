package structure;

import java.util.List;
import java.util.Random;

import structure.ColorType;
import structure.GameState;
import structure.SpecialSquares;
import structure.Stone;
import Algorithms.ExpectiMinimax;

public class AIVsAI {

    private Random random = new Random();
    private static final int EXPECTIMINIMAX_DEPTH = 3;

    public void start(GameState state) {

        while (!isGameOver(state)) {

            System.out.println("\n====================");
            System.out.println("Current board:\n");
            System.out.println(state);

            System.out.println("\n====================");
            System.out.println("Current player: " + state.currentPlayer);

            // 1. رمي العصي
            int steps = MoveProbability.throwSticks();
            System.out.println("Sticks result: " + steps);

            if (steps == 0) {
                System.out.println("No moves, turn skipped.");
                switchTurn(state);
                continue;
            }

            // Handle special squares auto validation (28 and 29)
            handleSpecialSquaresAutoValidation(state, steps);

            // Choose stone using Expectiminimax-like local evaluator
            Stone chosenStone = chooseStoneWithExpectiminimax(state, steps);

            if (chosenStone != null) {
                MoveLogic.moveStone(state, chosenStone, steps);
                System.out.println("Moved stone from " + (chosenStone.position - steps) + " to " + chosenStone.position);
            } else {
                System.out.println("No valid move available for player " + state.currentPlayer);
            }

            switchTurn(state);
        }

        printWinner(state);
    }

    private Stone chooseStoneWithExpectiminimax(GameState state, int steps) {
        List<Stone> stones = (state.currentPlayer == ColorType.BLACK) ? state.blackStones : state.whiteStones;
        Stone bestStone = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        // Evaluate each valid move for the rolled steps
        for (Stone stone : stones) {
            if (stone.isOut) continue;
            if (!isValidMoveForStone(state, stone, steps)) continue;

            GameState simulatedState = state.copy();
            Stone simulatedStone = findStoneInCopiedState(simulatedState, stone);
            if (simulatedStone == null) continue;

            MoveLogic.moveStone(simulatedState, simulatedStone, steps, true); // silent
            // set to opponent
            simulatedState.currentPlayer = (state.currentPlayer == ColorType.BLACK) ? ColorType.WHITE : ColorType.BLACK;

            double score = expectiminimaxEval(simulatedState, EXPECTIMINIMAX_DEPTH, false);

            if (score > bestScore) {
                bestScore = score;
                bestStone = stone;
            }
        }

        if (bestStone != null) {
            System.out.println(state.currentPlayer + " AI chose stone at position: " + bestStone.position + " (score=" + String.format("%.2f", bestScore) + ")");
        }

        return bestStone;
    }

    private double expectiminimaxEval(GameState state, int depth, boolean isMaximizing) {
        if (depth == 0 || isGameOver(state)) {
            return evaluateState(state);
        }

        double expectedValue = 0.0;

        for (int s = 1; s <= 5; s++) {
            double probability = MoveProbability.getProbability(s);

            if (isMaximizing) {
                double max = Double.NEGATIVE_INFINITY;
                for (Stone stone : state.blackStones) {
                    if (stone.isOut || !isValidMoveForStone(state, stone, s)) continue;
                    GameState newState = state.copy();
                    Stone newStone = findStoneInCopiedState(newState, stone);
                    MoveLogic.moveStone(newState, newStone, s, true);
                    newState.currentPlayer = ColorType.WHITE;
                    double value = expectiminimaxEval(newState, depth - 1, false);
                    max = Math.max(max, value);
                }
                if (max != Double.NEGATIVE_INFINITY) expectedValue += probability * max;
            } else {
                double min = Double.POSITIVE_INFINITY;
                for (Stone stone : state.whiteStones) {
                    if (stone.isOut || !isValidMoveForStone(state, stone, s)) continue;
                    GameState newState = state.copy();
                    Stone newStone = findStoneInCopiedState(newState, stone);
                    MoveLogic.moveStone(newState, newStone, s, true);
                    newState.currentPlayer = ColorType.BLACK;
                    double value = expectiminimaxEval(newState, depth - 1, true);
                    min = Math.min(min, value);
                }
                if (min != Double.POSITIVE_INFINITY) expectedValue += probability * min;
            }
        }

        return expectedValue;
    }

    private double evaluateState(GameState state) {
        if (state.blackStonesOut == 7) return 10000.0;
        if (state.whiteStonesOut == 7) return -10000.0;

        double score = 0.0;
        score += state.blackStonesOut * 1000;
        score -= state.whiteStonesOut * 1000;

        for (Stone s : state.blackStones) if (!s.isOut) score += s.position * 10;
        for (Stone s : state.whiteStones) if (!s.isOut) score -= s.position * 10;

        return score;
    }

    private boolean isValidMoveForStone(GameState state, Stone stone, int steps) {
        if (stone.isOut) return false;
        int newPos = stone.position + steps;
        if (newPos > 30) return true;
        if (stone.position < 26 && newPos > 26) return false;
        if (stone.position == SpecialSquares.threeTruths && steps != 3) return false;
        if (stone.position == SpecialSquares.reAtoum && steps != 2) return false;
        Stone target = state.board.stones[newPos];
        if (target.color == stone.color) return false;
        return true;
    }

    private Stone findStoneInCopiedState(GameState copiedState, Stone originalStone) {
        List<Stone> stones = (originalStone.color == ColorType.BLACK) ? copiedState.blackStones : copiedState.whiteStones;
        for (Stone s : stones) {
            if (s.position == originalStone.position && !s.isOut) return s;
        }
        return null;
    }

    private void handleSpecialSquaresAutoValidation(GameState state, int steps) {
        List<Stone> currentPlayerStones = state.currentPlayer == ColorType.WHITE ? state.whiteStones : state.blackStones;

        for (Stone stone : currentPlayerStones) {
            if (stone.isOut) continue;
            if (stone.position == SpecialSquares.threeTruths && steps != 3) {
                System.out.println("Stone on square 28 requires exactly 3. Moving back to rebirth.");
                MoveLogic.moveStoneToReBirth(state, stone);
            }
            if (stone.position == SpecialSquares.reAtoum && steps != 2) {
                System.out.println("Stone on square 29 requires exactly 2 to exit. Moving back to rebirth.");
                MoveLogic.moveStoneToReBirth(state, stone);
            }
        }
    }

    private void switchTurn(GameState state) {
        if (state.currentPlayer == ColorType.WHITE)
            state.currentPlayer = ColorType.BLACK;
        else
            state.currentPlayer = ColorType.WHITE;
    }

    private boolean isGameOver(GameState state) {
        return state.whiteStonesOut == 7 || state.blackStonesOut == 7;
    }

    private void printWinner(GameState state) {
        if (state.whiteStonesOut == 7)
            System.out.println("\nWHITE AI WINS!");
        else
            System.out.println("\nBLACK AI WINS!");
    }
}
