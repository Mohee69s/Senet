package structure;

import java.util.List;
import java.util.Random;
import java.util.Scanner;

import structure.ColorType;
import structure.GameState;
import structure.SpecialSquares;
import structure.Stone;

import static structure.MoveLogic.moveStoneToReBirth;

public class PlayGame {

    private Scanner scanner = new Scanner(System.in);
    private Random random = new Random();
    private static final int EXPECTIMINIMAX_DEPTH = 3; // Adjust for difficulty

    // =========================
    // MAIN LOOP
    // =========================
    public void start(GameState state) {

        while (!isGameOver(state)) {

            System.out.println("\n====================");
            System.out.println("Current board:\n");
            System.out.println(state);

            System.out.println("\n====================");
            System.out.println("Current player: " + state.currentPlayer);

            // 1. رمي العصي
            int steps = throwSticks();
            System.out.println("Sticks result: " + steps);

            // إذا ما في حركة
            if (steps == 0) {
                System.out.println("No moves, turn skipped.");
                switchTurn(state);
                continue;
            }

            // التحقق التلقائي من الحجارة على المربعات الخاصة 28 و 29
            handleSpecialSquaresAutoValidation(state, steps);

            // 2. اختيار الحجر
            Stone chosenStone;
            if (state.currentPlayer == ColorType.WHITE) {
                chosenStone = chooseStoneFromUser(state, steps);
            } else {
                // Computer uses Expectiminimax
                System.out.println("Computer is thinking...");
                chosenStone = chooseStoneWithExpectiminimax(state, steps);
            }

            // 3. تنفيذ الحركة
            if (chosenStone != null) {
                MoveLogic.moveStone(state, chosenStone, steps);
                System.out.println("Moved stone from " + 
                    (chosenStone.position - steps) + " to " + chosenStone.position);
            } else {
                System.out.println("No valid move available.");
            }

            // 4. تبديل الدور
            switchTurn(state);
        }

        printWinner(state);
    }

    // =========================
    // رمي العصي (4 عصي)
    // =========================
    private int throwSticks() {
        return MoveProbability.throwSticks();
    }

    // =========================
    // اختيار حجر المستخدم
    // =========================
    private Stone chooseStoneFromUser(GameState state, int steps) {

        List<Stone> stones = state.whiteStones;
        Stone selected = null;

        // Find valid moves
        List<Stone> validStones = new java.util.ArrayList<>();
        for (Stone s : stones) {
            if (!s.isOut && isValidMoveForStone(state, s, steps)) {
                validStones.add(s);
            }
        }

        if (validStones.isEmpty()) {
            System.out.println("No valid moves available.");
            return null;
        }

        while (selected == null) {
            System.out.println("Your stones with valid moves:");
            for (Stone s : validStones) {
                System.out.print(s.position + " ");
            }
            System.out.println();

            System.out.print("Choose stone position: ");
            int pos = scanner.nextInt();

            for (Stone s : validStones) {
                if (s.position == pos) {
                    selected = s;
                    break;
                }
            }

            if (selected == null) {
                System.out.println("Invalid stone or move, try again.");
            }
        }
        return selected;
    }

    // =========================
    // اختيار حجر باستخدام Expectiminimax
    // =========================
    private Stone chooseStoneWithExpectiminimax(GameState state, int steps) {
        
        List<Stone> stones = state.blackStones;
        Stone bestStone = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        // Evaluate each valid move
        for (Stone stone : stones) {
            if (stone.isOut) continue;
            
            if (!isValidMoveForStone(state, stone, steps)) continue;

            // Simulate the move
            GameState simulatedState = state.copy();
            Stone simulatedStone = findStoneInCopiedState(simulatedState, stone);
            
            if (simulatedStone == null) continue;
            
            MoveLogic.moveStone(simulatedState, simulatedStone, steps);
            
            // Evaluate using expectiminimax
            simulatedState.currentPlayer = ColorType.WHITE; // Next turn
            double score = expectiminimaxEval(simulatedState, EXPECTIMINIMAX_DEPTH, false);
            
            if (score > bestScore) {
                bestScore = score;
                bestStone = stone;
            }
        }

        if (bestStone != null) {
            System.out.println("Computer chose stone at position: " + bestStone.position);
            System.out.println("Expected value: " + String.format("%.2f", bestScore));
        }

        return bestStone;
    }

    /**
     * Simplified expectiminimax evaluation
     */
    private double expectiminimaxEval(GameState state, int depth, boolean isMaximizing) {
        if (depth == 0 || isGameOver(state)) {
            return evaluateState(state);
        }

        double expectedValue = 0.0;

        // Average over all possible dice outcomes
        for (int steps = 1; steps <= 5; steps++) {
            double probability = MoveProbability.getProbability(steps);
            
            if (isMaximizing) {
                double max = Double.NEGATIVE_INFINITY;
                for (Stone stone : state.blackStones) {
                    if (stone.isOut || !isValidMoveForStone(state, stone, steps)) continue;
                    
                    GameState newState = state.copy();
                    Stone newStone = findStoneInCopiedState(newState, stone);
                    MoveLogic.moveStone(newState, newStone, steps);
                    newState.currentPlayer = ColorType.WHITE;
                    
                    double value = expectiminimaxEval(newState, depth - 1, false);
                    max = Math.max(max, value);
                }
                if (max != Double.NEGATIVE_INFINITY) {
                    expectedValue += probability * max;
                }
            } else {
                double min = Double.POSITIVE_INFINITY;
                for (Stone stone : state.whiteStones) {
                    if (stone.isOut || !isValidMoveForStone(state, stone, steps)) continue;
                    
                    GameState newState = state.copy();
                    Stone newStone = findStoneInCopiedState(newState, stone);
                    MoveLogic.moveStone(newState, newStone, steps);
                    newState.currentPlayer = ColorType.BLACK;
                    
                    double value = expectiminimaxEval(newState, depth - 1, true);
                    min = Math.min(min, value);
                }
                if (min != Double.POSITIVE_INFINITY) {
                    expectedValue += probability * min;
                }
            }
        }

        return expectedValue;
    }

    /**
     * Evaluate game state
     */
    private double evaluateState(GameState state) {
        if (state.blackStonesOut == 7) return 10000.0;
        if (state.whiteStonesOut == 7) return -10000.0;

        double score = 0.0;
        
        // Stones out
        score += state.blackStonesOut * 1000;
        score -= state.whiteStonesOut * 1000;
        
        // Progress
        for (Stone s : state.blackStones) {
            if (!s.isOut) score += s.position * 10;
        }
        for (Stone s : state.whiteStones) {
            if (!s.isOut) score -= s.position * 10;
        }
        
        return score;
    }

    /**
     * Check if a move is valid for a specific stone
     */
    private boolean isValidMoveForStone(GameState state, Stone stone, int steps) {
        if (stone.isOut) return false;

        int newPos = stone.position + steps;

        // Can exit board
        if (newPos > 30) return true;

        // Must stop at 26 first
        if (stone.position < 26 && newPos > 26) return false;

        // Special square rules
        if (stone.position == SpecialSquares.threeTruths && steps != 3) return false;
        if (stone.position == SpecialSquares.reAtoum && steps != 2) return false;

        // Can't move to same color
        Stone target = state.board.stones[newPos];
        if (target.color == stone.color) return false;

        return true;
    }

    /**
     * Find stone in copied state
     */
    private Stone findStoneInCopiedState(GameState copiedState, Stone originalStone) {
        List<Stone> stones = (originalStone.color == ColorType.BLACK) ? 
            copiedState.blackStones : copiedState.whiteStones;
        
        for (Stone s : stones) {
            if (s.position == originalStone.position && !s.isOut) {
                return s;
            }
        }
        return null;
    }

    // =========================
    // تبديل الدور
    // =========================
    private void switchTurn(GameState state) {
        if (state.currentPlayer == ColorType.WHITE)
            state.currentPlayer = ColorType.BLACK;
        else
            state.currentPlayer = ColorType.WHITE;
    }

    // =========================
    // التحقق التلقائي من المربعات الخاصة 28 و 29
    // =========================
    private static void handleSpecialSquaresAutoValidation(
            GameState state, int steps) {

        List<Stone> currentPlayerStones = state.currentPlayer == ColorType.WHITE
                ? state.whiteStones
                : state.blackStones;

        for (Stone stone : currentPlayerStones) {
            if (stone.isOut)
                continue;

            // إذا الحجر على مربع 28 (threeTruths) يجب أن يكون الرمي 3 بالضبط
            if (stone.position == SpecialSquares.threeTruths) {
                if (steps != 3) {
                    System.out.println(
                            "Stone on square 28 requires exactly 3. Got " + steps + ". Moving back to rebirth.");
                    moveStoneToReBirth(state, stone);
                }
            }

            // إذا الحجر على مربع 29 (reAtoum) يجب أن يكون الرمي 2 بالضبط للخروج
            if (stone.position == SpecialSquares.reAtoum) {
                if (steps != 2) {
                    System.out.println("Stone on square 29 requires exactly 2 to exit. Got " + steps
                            + ". Moving back to rebirth.");
                    moveStoneToReBirth(state, stone);
                }
            }
        }
    }

    // =========================
    // نهاية اللعبة
    // =========================
    private boolean isGameOver(GameState state) {
        return state.whiteStonesOut == 7 || state.blackStonesOut == 7;
    }

    private void printWinner(GameState state) {
        if (state.whiteStonesOut == 7)
            System.out.println("\nWHITE PLAYER WINS!");
        else
            System.out.println("\nBLACK PLAYER WINS!");
    }
}