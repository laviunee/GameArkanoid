package Entities.Power;

import Entities.Paddle;
import Entities.Ball;
import UI.GameScene;
import javafx.scene.image.Image;

public class PowerUpExtraLive extends PowerUp {

    private GameScene gameScene;

    public PowerUpExtraLive(double x, double y, GameScene gameScene) {
        super("ExtraLive", x, y);
        this.gameScene = gameScene;

        // Load icon - thêm xử lý lỗi
        try {
            Image icon = new Image(getClass().getResourceAsStream("/images/powerup/extralive.png"));
            setSprite(icon);
        } catch (Exception e) {
            System.err.println("Error loading extralive icon: " + e.getMessage());
            // Nếu không load được ảnh, power-up vẫn hoạt động bình thường
        }
    }

    @Override
    public void applyEffect(Paddle paddle, Ball ball) {
        System.out.println("PowerUp: +1 Live");
        if (gameScene != null) {
            gameScene.addLive();
        } else {
            System.err.println("Error: GameScene is null in PowerUpExtraLive");
        }
        collect();
    }

    // THÊM: Getter cho GameScene (nếu cần)
    public GameScene getGameScene() {
        return gameScene;
    }
}