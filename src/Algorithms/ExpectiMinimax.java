package Algorithms;
import structure.*;
import java.util.List;

public class ExpectiMinimax {

    private static final double WIN_SCORE = 10000.0;
    private static final double LOSE_SCORE = -10000.0;

    /**
     * Find the best move for the computer player using Expectiminimax
     * @param state Current game state
     * @param depth Search depth
     * @return Best stone to move, or null if no valid moves
     */
    public static Stone findBestMove(GameState state, int depth) {
        Stone bestStone = null;
        double bestValue = Double.NEGATIVE_INFINITY;
        
        ColorType player = state.currentPlayer;
        List<Stone> playerStones = (player == ColorType.BLACK) ? 
            state.blackStones : state.whiteStones;

        // Try each possible move with each possible dice roll
        for (int steps = 1; steps <= 5; steps++) {
            double probability = MoveProbability.getProbability(steps);
            
            for (Stone stone : playerStones) {
                if (stone.isOut) continue;
                
                // Check if this move is valid
                if (!isValidMove(state, stone, steps)) continue;
                
                // Simulate the move
                GameState newState = state.copy();
                Stone newStone = findStoneInState(newState, stone);
                
                if (newStone == null) continue;
                
                MoveLogic.moveStone(newState, newStone, steps);
                
                // Evaluate this move
                double value = expectiminimax(newState, depth - 1, false) * probability;
                
                if (value > bestValue) {
                    bestValue = value;
                    bestStone = stone;
                }
            }
        }
        
        return bestStone;
    }

    /**
     * Expectiminimax recursive function
     * @param state Current game state
     * @param depth Remaining search depth
     * @param isMaximizing True if maximizing player's turn
     * @return Evaluated score
     */
    private static double expectiminimax(GameState state, int depth, boolean isMaximizing) {
        
        // Terminal conditions
        if (depth == 0 || isTerminal(state)) {
            return evaluate(state);
        }

        // Chance node - calculate expected value over all dice outcomes
        return chanceNode(state, depth, isMaximizing);
    }

    /**
     * Chance node - handles probability of dice rolls
     */
    private static double chanceNode(GameState state, int depth, boolean isMaximizing) {
        double expectedValue = 0.0;
        
        // For each possible dice outcome
        for (int steps = 1; steps <= 5; steps++) {
            double probability = MoveProbability.getProbability(steps);
            
            if (isMaximizing) {
                double value = maxNode(state, steps, depth);
                expectedValue += probability * value;
            } else {
                double value = minNode(state, steps, depth);
                expectedValue += probability * value;
            }
        }
        
        return expectedValue;
    }

    /**
     * Maximizing node (computer's turn)
     */
    private static double maxNode(GameState state, int steps, int depth) {
        double maxValue = Double.NEGATIVE_INFINITY;
        boolean foundMove = false;
        
        List<Stone> stones = (state.currentPlayer == ColorType.BLACK) ? 
            state.blackStones : state.whiteStones;
        
        for (Stone stone : stones) {
            if (stone.isOut) continue;
            
            if (!isValidMove(state, stone, steps)) continue;
            
            GameState newState = state.copy();
            Stone newStone = findStoneInState(newState, stone);
            
            if (newStone == null) continue;
            
            MoveLogic.moveStone(newState, newStone, steps);
            newState.currentPlayer = (newState.currentPlayer == ColorType.BLACK) ? 
                ColorType.WHITE : ColorType.BLACK;
            
            foundMove = true;
            double value = expectiminimax(newState, depth - 1, false);
            maxValue = Math.max(maxValue, value);
        }
        
        // If no valid moves found, return current evaluation
        if (!foundMove) {
            return evaluate(state);
        }
        
        return maxValue;
    }

    /**
     * Minimizing node (opponent's turn)
     */
    private static double minNode(GameState state, int steps, int depth) {
        double minValue = Double.POSITIVE_INFINITY;
        boolean foundMove = false;
        
        List<Stone> stones = (state.currentPlayer == ColorType.BLACK) ? 
            state.blackStones : state.whiteStones;
        
        for (Stone stone : stones) {
            if (stone.isOut) continue;
            
            if (!isValidMove(state, stone, steps)) continue;
            
            GameState newState = state.copy();
            Stone newStone = findStoneInState(newState, stone);
            
            if (newStone == null) continue;
            
            MoveLogic.moveStone(newState, newStone, steps);
            newState.currentPlayer = (newState.currentPlayer == ColorType.BLACK) ? 
                ColorType.WHITE : ColorType.BLACK;
            
            foundMove = true;
            double value = expectiminimax(newState, depth - 1, true);
            minValue = Math.min(minValue, value);
        }
        
        // If no valid moves found, return current evaluation
        if (!foundMove) {
            return evaluate(state);
        }
        
        return minValue;
    }

    /**
     * Evaluation function for board state
     */
    private static double evaluate(GameState state) {
        // Terminal states
        if (state.blackStonesOut == 7) return WIN_SCORE;
        if (state.whiteStonesOut == 7) return LOSE_SCORE;
        
        double score = 0.0;
        
        // 1. Stones out (most important)
        score += (state.blackStonesOut * 1000);
        score -= (state.whiteStonesOut * 1000);
        
        // 2. Progress on board (further = better)
        for (Stone stone : state.blackStones) {
            if (!stone.isOut) {
                score += stone.position * 10;
                
                // Bonus for being close to exit
                if (stone.position >= 26) score += 100;
            }
        }
        
        for (Stone stone : state.whiteStones) {
            if (!stone.isOut) {
                score -= stone.position * 10;
                
                // Penalty if opponent close to exit
                if (stone.position >= 26) score -= 100;
            }
        }
        
        // 3. Special square considerations
        for (Stone stone : state.blackStones) {
            if (stone.isOut) continue;
            
            // Bonus for being on happiness square (26)
            if (stone.position == SpecialSquares.happiness) {
                score += 50;
            }
            
            // Penalty for dangerous squares
            if (stone.position == SpecialSquares.water) {
                score -= 200;
            }
        }
        
        for (Stone stone : state.whiteStones) {
            if (stone.isOut) continue;
            
            if (stone.position == SpecialSquares.happiness) {
                score -= 50;
            }
            
            if (stone.position == SpecialSquares.water) {
                score += 200;
            }
        }
        
        return score;
    }

    /**
     * Check if move is valid
     */
    private static boolean isValidMove(GameState state, Stone stone, int steps) {
        if (stone.isOut) return false;
        
        int newPos = stone.position + steps;
        
        // Check if exceeding board
        if (newPos > 30) {
            return true; // Valid - stone exits
        }
        
        // Must stop at happiness square (26) first
        if (stone.position < 26 && newPos > 26) {
            return false;
        }
        
        // Check special square rules
        if (stone.position == SpecialSquares.threeTruths && steps != 3) {
            return false;
        }
        
        if (stone.position == SpecialSquares.reAtoum && steps != 2) {
            return false;
        }
        
        // Check if target square has same color stone
        Stone target = state.board.stones[newPos];
        if (target.color == stone.color) {
            return false;
        }
        
        return true;
    }

    /**
     * Check if game is over
     */
    private static boolean isTerminal(GameState state) {
        return state.whiteStonesOut == 7 || state.blackStonesOut == 7;
    }

    /**
     * Find corresponding stone in copied state
     */
    private static Stone findStoneInState(GameState state, Stone originalStone) {
        List<Stone> stones = (originalStone.color == ColorType.BLACK) ? 
            state.blackStones : state.whiteStones;
        
        for (Stone s : stones) {
            if (s.position == originalStone.position && 
                s.color == originalStone.color && 
                s.isOut == originalStone.isOut) {
                return s;
            }
        }
        return null;
    }
}