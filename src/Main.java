import structure.GameState;
import structure.PlayGame;

public class Main {

    public static void main(String[] args) {

        // 1. إنشاء حالة اللعبة
        GameState state = new GameState();

        // 2. تهيئة الرقعة
        Senet senet = new Senet();
        senet.getInitState(state);

        // 3. طباعة البداية
        System.out.println("=== GAME START ===");
        senet.printBoard(state);

        // 4. تشغيل اللعبة

    }
}
