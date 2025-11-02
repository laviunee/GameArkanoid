package Entities.Power;

import Entities.Paddle;
import Entities.Ball;
import UI.GameScene; // cần import để chỉnh mạng
import javafx.scene.image.Image;

public class PowerUpExtraLive extends PowerUp {

    private GameScene gameScene;
    private Image icon;

    public PowerUpExtraLive(double x, double y, GameScene gameScene) {
        super("ExtraLive", x, y);
        this.gameScene = gameScene;
        this.icon = new Image("file:assets/images/powerup/ball.png");
        setSprite(icon);
    }

    @Override
    public void applyEffect(Paddle paddle, Ball ball) {
        System.out.println("PowerUp: +1 Live");
        gameScene.addLive();
        collect();
    }
}
