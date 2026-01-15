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
                chosenStone = chooseStoneFromUser(state);
            } else {
                chosenStone = chooseStoneForComputer(state);
            }

            // 3. تنفيذ الحركة
            if (chosenStone != null) {
                MoveLogic.moveStone(state, chosenStone, steps);
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
        int sum = 0;
        for (int i = 0; i < 4; i++) {
            sum += random.nextInt(2); // 0 أو 1
        }
        return (sum == 0) ? 5 : sum;
    }

    // =========================
    // اختيار حجر المستخدم
    // =========================
    private Stone chooseStoneFromUser(GameState state) {

        List<Stone> stones = state.whiteStones;
        Stone selected = null;

        while (selected == null) {
            System.out.println("Your stones:");
            for (Stone s : stones) {
                if (!s.isOut) {
                    System.out.print(s.position + " ");
                }
            }
            System.out.println();

            System.out.print("Choose stone position: ");
            int pos = scanner.nextInt();

            for (Stone s : stones) {
                if (!s.isOut && s.position == pos) {
                    selected = s;
                    break;
                }
            }

            if (selected == null) {
                System.out.println("Invalid stone, try again.");
            }
        }
        return selected;
    }

    // =========================
    // اختيار حجر الكمبيوتر
    // =========================
    private Stone chooseStoneForComputer(GameState state) {

        List<Stone> stones = state.blackStones;

        for (Stone s : stones) {
            if (!s.isOut) {
                System.out.println("Computer chose stone at: " + s.position);
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
