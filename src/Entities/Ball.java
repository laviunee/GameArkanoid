package Entities;

import Core.MovableObject;
import Utils.Config;
import javafx.scene.paint.Color;

public class Ball extends MovableObject {
    private double radius;
    private boolean active;
    private boolean onPaddle;
    private Color color;

    public Ball(double x, double y) {
        super("Ball", x, y);
        this.radius = Config.BALL_SIZE / 2;
        this.active = false;
        this.onPaddle = true;
        this.color = Color.WHITE;

        setVelocity(0, 0);
    }

    @Override
    public void start() {
        System.out.println("Ball initialized at " + getPosition());
    }

    @Override
    public void update(double deltaTime) {
        if (!active) return;

        // debug: Hiển thị thông tin ball
        if (Math.random() < 0.005) {
            System.out.println("Ball - Pos: (" + String.format("%.1f", position.x) + ", " +
                    String.format("%.1f", position.y) + ") Vel: (" +
                    String.format("%.1f", velocity.x) + ", " +
                    String.format("%.1f", velocity.y) + ")");
        }

        super.update(deltaTime);

        // Đảm bảo ball không đi quá ngang
        if (Math.abs(velocity.x) < 50 && Math.abs(velocity.y) > 0) {
            velocity.x = (velocity.x >= 0) ? 50 : -50;
        }
    }

    // KHI BALL ĐƯỢC KÍCH HOẠT (LAUNCH)
    public void setActive(boolean active) {
        this.active = active;
        this.onPaddle = !active; // ← KHI ACTIVE THÌ KHÔNG CÒN Ở TRÊN PADDLE
        if (active) {
            System.out.println("🎾 Ball launched!");
        }
    }

    // CẬP NHẬT VỊ TRÍ KHI THEO PADDLE
    public void followPaddle(double paddleX, double paddleY, double paddleHeight) {
        if (onPaddle) {
            position.x = paddleX;
            position.y = paddleY - paddleHeight/2 - radius - 1;
        }
    }

    public boolean isActive() {
        return active;
    }

    public boolean isOnPaddle() {
        return onPaddle;
    }

    public double getRadius() {
        return radius;
    }

    public Color getColor() {
        return color;
    }
}