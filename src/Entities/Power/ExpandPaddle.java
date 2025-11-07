package Entities.Power;

import Entities.Ball;
import Entities.Paddle;

public class ExpandPaddle extends PowerUp {
    private static final double EXPAND_FACTOR = 1.5;

    public ExpandPaddle(double x, double y) {
        super("ExpandPaddle", x, y);
    }

    @Override
    public void start() {
        super.start(); // Gọi parent start()
    }

    @Override
    public void applyEffect(Paddle paddle, Ball ball) {
        System.out.println("Expand Paddle activated!");
        paddle.expand(EXPAND_FACTOR);

        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        paddle.resetSize();
                        System.out.println("Expand Paddle deactivated");
                    }
                },
                10000
        );
    }
}