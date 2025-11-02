package Entities.Power;

import java.util.Random;
import java.util.List;
import Entities.Ball;
import UI.GameScene;


public class PowerFactory {
    private static final Random random = new Random();

    //Giữ tham chiếu tới danh sách bóng trong GameScene
    private static List<Ball> sharedBalls;

    //Gọi từ GameScene.start() để truyền danh sách bóng
    public static void setBalls(List<Ball> balls) {
        sharedBalls = balls;
    }

    private static GameScene gameScene;

    public static void setGameScene(GameScene scene) {
        gameScene = scene;
    }

    public static PowerUp createRandomPowerUp(double x, double y) {
        int type = random.nextInt(4);

        switch (type) {
            case 0:
                return new ExpandPaddle(x, y);
            case 1:
                return new FastBall(x, y);
            case 2:
                if (sharedBalls != null) {
                    return new PowerUpMultiBall(x, y, sharedBalls);
                } else {
                    System.err.println("PowerFactory: balls list not set!");
                    return null;
                }
            case 3:
                return new PowerUpExtraLive(x, y, gameScene);

            default:
                return null;
        }
    }
}
