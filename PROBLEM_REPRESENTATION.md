# Senet Game - Problem Representation

## 1. Problem Representation

### State Representation
A **state** in Senet is represented by:
- **Board configuration**: Positions of all stones (1-30)
- **Current player**: WHITE or BLACK
- **Stones out**: Count of stones that have exited the board (0-7 for each player)
- **Stone positions**: Each stone's position on the board or marked as "out"

**State Structure**: `GameState` class containing:
- `Board board`: Array of 31 positions (0-30)
- `ColorType currentPlayer`: WHITE or BLACK
- `List<Stone> whiteStones`: List of white stones
- `List<Stone> blackStones`: List of black stones
- `int whiteStonesOut`: Count of white stones that exited
- `int blackStonesOut`: Count of black stones that exited

### Transition Function
The **transition function** `T(state, action, diceValue) → newState`:
- **Input**: Current state, chosen stone to move, dice/stick throw value (1-5)
- **Output**: New state after applying the move
- **Rules**:
  - Move stone forward by dice value
  - Handle special squares (water, happiness, threeTruths, reAtoum)
  - Swap positions if landing on single opponent stone
  - Block if 2+ opponent stones in a row
  - Exit board if reaching position 30
  - Send back to reBirth if failing special square requirements

### Cost Function
The **cost** (evaluation function) for a state:
- **Heuristic**: `evaluation(state) = (blackStonesOut * 100) - (whiteStonesOut * 100) + positionScore`
- **Position Score**: Sum of positions of all stones (higher positions = better)
- **Goal**: Maximize for BLACK (computer), minimize for WHITE (player)
- **Terminal states**: +1000 if BLACK wins, -1000 if WHITE wins

### Starting State
- **Initial state**: 
  - White stones at positions: 1, 3, 5, 7, 9, 11, 13
  - Black stones at positions: 2, 4, 6, 8, 10, 12, 14
  - Current player: WHITE
  - Stones out: 0 for both players

### Ending State
- **Terminal states**:
  - White wins: `whiteStonesOut == 7`
  - Black wins: `blackStonesOut == 7`

## 2. Stick Throw Probabilities

### Mathematical Calculation

With 4 sticks, each has 2 outcomes (flat side up or down):
- Total possible outcomes: 2^4 = 16

**Probability Distribution:**
- **0 flat sides**: 1/16 = 0.0625 (6.25%) → Move 4
- **1 flat side**: 4/16 = 0.25 (25%) → Move 3
- **2 flat sides**: 6/16 = 0.375 (37.5%) → Move 2
- **3 flat sides**: 4/16 = 0.25 (25%) → Move 1
- **4 flat sides**: 1/16 = 0.0625 (6.25%) → Move 5 (extra turn)

**Expected value**: (4×0.0625) + (3×0.25) + (2×0.375) + (1×0.25) + (5×0.0625) = 2.5

## 3. Expectiminimax Algorithm

### Algorithm Structure
- **Max nodes**: Computer's turn (BLACK) - maximize evaluation
- **Min nodes**: Player's turn (WHITE) - minimize evaluation  
- **Chance nodes**: After stick throw - weighted average of all possible outcomes
- **Depth (Δ)**: Number of moves to look ahead

### Node Types
1. **MAX node**: Computer chooses best move
2. **MIN node**: Player chooses worst move (for computer)
3. **CHANCE node**: Expected value over all possible stick throws (weighted by probability)
