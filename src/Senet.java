import structure.*;

public class Senet {

    public static void main(String[] args) throws Exception {
        GameState state = new GameState();
        Senet senetGame = new Senet();
        senetGame.getInitState(state);
        //senetGame.printBoard(state);
        PlayGame game = new PlayGame();
        game.start(state);

        // 5. نهاية
        System.out.println("=== GAME OVER ===");

    }

    public GameState getInitState(GameState gameState) {
        GameState initState = gameState;
        for (int i = 0; i < 7; i++) {
            Stone whiteStone = new Stone(ColorType.WHITE, 2 * i + 1);
            initState.whiteStones.add(whiteStone);
            initState.board.stones[2 * i + 1] = whiteStone;

            Stone blackStone = new Stone(ColorType.BLACK, 2 * i + 2);
            initState.blackStones.add(blackStone);
            initState.board.stones[2 * i + 2] = blackStone;
        }
        for (int i = 15; i <= 30; i++) {
            Stone specialStone = new Stone(ColorType.NONE, i);
            if (SpecialSquares.isSpecialSquare(i)) {
                initState.board.stones[i] = specialStone;
            }
        }
        initState.currentPlayer = ColorType.WHITE;
        return initState;
    }

    public void printBoard(GameState state) {
        System.out.println(" ----senet board---- ");

        for (int i = 1; i <= 10; i++) {
            printStone(state.board.stones[i]);
        }
        System.out.println();
        for (int i = 20; i >= 11; i--) {
            printStone(state.board.stones[i]);
        }
        System.out.println();
        for (int i = 21; i <= 30; i++) {
            printStone(state.board.stones[i]);
        }
        System.out.println();
        System.out.println(" ------------------- ");
        System.out.println("white stones out:" + state.whiteStonesOut + "/7");
        System.out.println("black stones out:" + state.blackStonesOut + "/7");
        System.out.println("current player:" + state.currentPlayer);
        System.out.println(" ------------------- ");
    }

    public void printStone(Stone stone) {
        if (stone.isEmpty() && SpecialSquares.isSpecialSquare(stone.position) == false) {
            System.out.print("[  " + stone.position + "  ]");
            return;
        }
        if (SpecialSquares.isSpecialSquare(stone.position)) {
            if (stone.color == ColorType.WHITE) {
                System.out.print("[W" + stone.position + "," + SpecialSquares.getSquareType(stone.position) + "]");
            } else if (stone.color == ColorType.BLACK) {
                System.out.print("[B" + stone.position + "," + SpecialSquares.getSquareType(stone.position) + "]");
            } else if (stone.color == ColorType.NONE) {
                System.out.print("[" + SpecialSquares.getSquareType(stone.position) + "," + stone.position + "]");
            }
        } else if (SpecialSquares.isSpecialSquare(stone.position) == false) {
            if (stone.color == ColorType.WHITE) {
                System.out.print("[W , " + stone.position + "]");
            } else if (stone.color == ColorType.BLACK) {
                System.out.print("[B , " + stone.position + "]");
            } else {
                System.out.print("[_ , " + stone.position + "]");
            }

        }

    }
}
