package Entities.Power;

import Entities.Ball;
import Entities.Paddle;
import javafx.scene.image.Image;

public class PowerUpPierceBall extends PowerUp {

    public PowerUpPierceBall(double x, double y) {
        super("PierceBall", x, y);
        setSprite(new Image("file:assets/images/powerup/pierceball.png"));
    }

    @Override
    public void applyEffect(Paddle paddle, Ball ball) {
        System.out.println("PowerUp: Pierce Ball Activated!");
        ball.setPierce(true);

        // Optional: đổi màu bóng để dễ nhận biết
        ball.setColor(javafx.scene.paint.Color.RED);

        collect();
    }
}
