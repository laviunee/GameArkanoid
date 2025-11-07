package Entities.Bricks;

import Core.GameObject;

/**
 * Lớp cha cho tất cả các loại gạch
 */
public abstract class Brick extends GameObject {
    protected double width, height;
    protected int hitPoints;
    protected int maxHitPoints;
    protected int scoreValue;
    protected boolean toBeRemoved = false;

    public Brick(String name, double x, double y, double width, double height,
                 int hitPoints, int scoreValue) {
        super(name, x, y);
        this.width = width;
        this.height = height;
        this.hitPoints = hitPoints;
        this.maxHitPoints = hitPoints;
        this.scoreValue = scoreValue;
    }

    @Override
    public void start() {
        System.out.println("brick" + name + " created at " + position);
    }

    @Override
    public void update(double deltaTime) {
        // Brick thường không cần update
    }

    // Khi bóng chạm vào gạch
    public void onHit() {
        hitPoints--;
        System.out.println("break " + name + " hit! HP: " + hitPoints + "/" + maxHitPoints);

        if (hitPoints <= 0) {
            toBeRemoved = true;
            System.out.println("break " + name + " destroyed! +" + scoreValue + " points");
        }
    }

    // Getter methods
    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public int getHitPoints() {
        return hitPoints;
    }

    public int getScoreValue() {
        return scoreValue;
    }

    public boolean isToBeRemoved() {
        return toBeRemoved;
    }

    // Kiểm tra va chạm đơn giản
    public boolean collidesWith(double ballX, double ballY, double ballRadius) {
        return ballX + ballRadius >= position.x &&
                ballX - ballRadius <= position.x + width &&
                ballY + ballRadius >= position.y &&
                ballY - ballRadius <= position.y + height;
    }
}