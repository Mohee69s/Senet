package structure;

import java.util.Random;
import java.util.HashMap;
import java.util.Map;

public class MoveProbability {
    private static final Random random = new Random();
    public static int throwSticks() {
        int darkFaces = 0;
        for (int i = 0; i < 4; i++) {
            if (random.nextBoolean()) {
                darkFaces++;
            }
        }
        return (darkFaces == 0) ? 5 : darkFaces;
    }
    public static double getProbability(int moveValue) {
        switch (moveValue) {
            case 1: return 4.0 / 16.0;
            case 2: return 6.0 / 16.0;
            case 3: return 4.0 / 16.0;
            case 4: return 1.0 / 16.0;
            case 5: return 1.0 / 16.0;
            default: return 0.0;
        }
    }
    public static Map<Integer, Double> getAllProbabilities() {
        Map<Integer, Double> probs = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            probs.put(i, getProbability(i));
        }
        return probs;
    }
}