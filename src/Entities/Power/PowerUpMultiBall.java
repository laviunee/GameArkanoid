package Entities.Power;

import Entities.Paddle;
import Entities.Ball;
import javafx.scene.image.Image;
import java.util.List;
import java.util.ArrayList;

public class PowerUpMultiBall extends PowerUp {
    private List<Ball> balls;
    private Image icon;

    public PowerUpMultiBall(double x, double y, List<Ball> balls) {
        super("MultiBall", x, y);
        this.balls = balls;
        this.icon = new Image("file:assets/images/powerup/ball.png");
        setSprite(icon);
    }

    @Override
    public void applyEffect(Paddle paddle, Ball ball) {
        if (balls == null || balls.isEmpty()) return;

        System.out.println("PowerUp: MultiBall activated!");

        List<Ball> newBalls = new ArrayList<>();

        for (Ball original : balls) {
            if (!original.isActive()) continue;
            Ball clone = new Ball(original.getPosition().x, original.getPosition().y);

            // Tạo hướng bay lệch nhẹ
            double angleOffset = (Math.random() * 0.6 - 0.3); // ~ ±17°
            double vx = original.getVelocity().x;
            double vy = original.getVelocity().y;

            double newVx = vx * Math.cos(angleOffset) - vy * Math.sin(angleOffset);
            double newVy = vx * Math.sin(angleOffset) + vy * Math.cos(angleOffset);

            clone.setVelocity(newVx, newVy);
            clone.setActive(true);
            newBalls.add(clone);
        }

        if (balls.size() + newBalls.size() > 10) {
            System.out.println("Too many balls, limiting MultiBall effect!");
            return;
        }
        balls.addAll(newBalls);
        collect();
    }
}
