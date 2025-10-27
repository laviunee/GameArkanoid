package Entities.Power;

import Entities.Paddle;
import Entities.Ball;
import Utils.Config;

public class FastBall extends PowerUp {
    private static final double SPEED_BOOST = 1.5;

    public FastBall(double x, double y) {
        super("FastBall", x, y);
    }

    @Override
    public void start() {
        super.start(); // Gọi parent start()
    }

    @Override
    public void applyEffect(Paddle paddle, Ball ball) {
        System.out.println("⚡ Fast Ball activated!");

        ball.getVelocity().x *= SPEED_BOOST;
        ball.getVelocity().y *= SPEED_BOOST;

        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        ball.getVelocity().x /= SPEED_BOOST;
                        ball.getVelocity().y /= SPEED_BOOST;
                        System.out.println("⚡ Fast Ball deactivated");
                    }
                },
                8000
        );
    }
}