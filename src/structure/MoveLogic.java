package structure;

import structure.ColorType;
import structure.GameState;
import structure.SpecialSquares;
import structure.Stone;

public class MoveLogic {

    // =========================
    // تحريك حجر واحد
    // =========================
    public static void moveStone(GameState state, Stone stone, int steps) {

        // =========================
        // تحقق أولي
        // =========================
        if (stone.isOut)
            return;

        if (stone.position == SpecialSquares.water) {
            moveStoneToReBirth(state, stone);
            return;
        }
        // =========================
        // تحقق من البلاطات الخاصة 28 و 29
        // =========================
        // إذا الحجر على مربع 28 (threeTruths) يجب أن يكون الرمي 3 بالضبط
        if (stone.position == SpecialSquares.threeTruths) {
            if (steps != 3) {
                System.out.println("Illegal move: stone on square 28 requires exactly 3. Moving back to rebirth.");
                moveStoneToReBirth(state, stone);
                return;
            }
        }

        // إذا الحجر على مربع 29 (reAtoum) يجب أن يكون الرمي 2 بالضبط للخروج
        if (stone.position == SpecialSquares.reAtoum) {
            if (steps != 2) {
                System.out.println(
                        "Illegal move: stone on square 29 requires exactly 2 to exit. Moving back to rebirth.");
                moveStoneToReBirth(state, stone);
                return;
            }
        }

        int oldPos = stone.position;
        int newPos = oldPos + steps;

        if (stone.position < SpecialSquares.happiness &&
                newPos > SpecialSquares.happiness) {

            System.out.println("Illegal move: must stop at square 26 first.");
            return;
        }

        if (oldPos < 26 && newPos > 26) {
            System.out.println("Illegal move: must stop at square 26 first.");
            return;
        }
        // =========================
        // 1. الخروج من الرقعة
        // =========================
        if (newPos > 30) {
            removeStone(state, stone);
            return;
        }

        Stone target = state.board.stones[newPos];

        // =========================
        // 2. نفس اللون → حركة ممنوعة
        // =========================
        if (target.color == stone.color) {
            System.out.println("Illegal move: same color stone.");
            return;
        }

        // =========================
        // 3. لون مختلف → تبديل أماكن
        // =========================
        if (target.color != ColorType.NONE &&
                target.color != stone.color) {
            // نحط الحجر المتحرك بمكان الهدف
            state.board.stones[newPos] = stone;
            stone.position = newPos;

            // نحط الحجر الآخر بمكان الحجر المتحرك
            state.board.stones[oldPos] = target;
            target.position = oldPos;
            
            // IMPORTANT: Return here to prevent overwriting the swap!
            return;
        }

        // =========================
        // 4. المربع فاضي → حركة عادية
        // =========================
        state.board.stones[oldPos] = new Stone(ColorType.NONE, oldPos);

        stone.position = newPos;
        state.board.stones[newPos] = stone;
    }

    // =========================
    // إخراج حجر
    // =========================
    private static void removeStone(GameState state, Stone stone) {

        state.board.stones[stone.position] = new Stone(ColorType.NONE, stone.position);

        stone.isOut = true;

        if (stone.color == ColorType.WHITE)
            state.whiteStonesOut++;
        else
            state.blackStonesOut++;
    }

    // =========================
    // نقل مباشر لمربع معين
    // =========================
    public static void moveStoneToReBirth(
            GameState state, Stone stone) {

        int targetPos = findRebirthPosition(state);

        if (targetPos == -1) {
            System.out.println("No valid rebirth position!");
            return;
        }

        state.board.stones[stone.position] = new Stone(ColorType.NONE, stone.position);

        stone.position = targetPos;
        state.board.stones[targetPos] = stone;
    }

    private static int findRebirthPosition(GameState state) {

        // إذا 15 فاضية
        if (state.board.stones[SpecialSquares.reBirth].color == ColorType.NONE)
            return SpecialSquares.reBirth;

        // دور على أول بلاطة فاضية قبل 15
        for (int i = SpecialSquares.reBirth - 1; i >= 1; i--) {
            if (state.board.stones[i].color == ColorType.NONE)
                return i;
        }

        // إذا ما لقى (نادر جداً)
        return -1;
    }
}