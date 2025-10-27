package Entities.Power;

import java.util.Random;

public class PowerFactory {
    private static final Random random = new Random();

    public static PowerUp createRandomPowerUp(double x, double y) {
        int type = random.nextInt(2); // 0 hoặc 1

        switch (type) {
            case 0:
                return new ExpandPaddle(x, y);
            case 1:
                return new FastBall(x, y);
            default:
                return new ExpandPaddle(x, y);
        }
    }
}