package Entities;

import Core.MovableObject;
import Utils.Config;
import javafx.scene.paint.Color;

/**
 * Entity Paddle - người chơi điều khiển
 */
public class Paddle extends MovableObject {
    private double originalWidth;
    private double width, height;
    private Color color;

    public Paddle(double x, double y) {
        super("Paddle", x, y);
        this.width = Config.PADDLE_STD_WIDTH;
        this.originalWidth = Config.PADDLE_STD_WIDTH;
        this.height = Config.PADDLE_STD_HEIGHT;
        this.color = Color.LIGHTGRAY;
    }

    // EXPAND
    public void expand(double factor) {
        this.width = originalWidth * factor;
        System.out.println("Paddle expanded to: " + width);
    }

    // RESET
    public void resetSize() {
        this.width = originalWidth;
        System.out.println("Paddle reset to normal size");
    }

    @Override
    public void start() {
        System.out.println("Paddle initialized at " + getPosition());
    }

    @Override
    public void update(double deltaTime) {
        super.update(deltaTime);

        // Giới hạn paddle trong màn hình
        if (position.x < Config.INSET + width/2) {
            position.x = Config.INSET + width/2;
            velocity.x = 0; // Dừng lại khi chạm biên
        }
        if (position.x > Config.SCREEN_WIDTH - Config.INSET - width/2) {
            position.x = Config.SCREEN_WIDTH - Config.INSET - width/2;
            velocity.x = 0; // Dừng lại khi chạm biên
        }
    }

    // Movement methods
    public void moveLeft() {
        setVelocity(-Config.PADDLE_SPEED, 0);
    }

    public void moveRight() {
        setVelocity(Config.PADDLE_SPEED, 0);
    }

    public void stop() {
        setVelocity(0, 0);
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public Color getColor() {
        return color;
    }
}