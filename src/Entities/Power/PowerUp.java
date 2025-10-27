package Entities.Power;

import Core.GameObject;
import Entities.Paddle;
import Entities.Ball;
import Utils.Config;

public abstract class PowerUp extends GameObject {
    protected boolean collected = false;
    protected double velocityY;

    public PowerUp(String name, double x, double y) {
        super(name, x, y);
        this.velocityY = Config.BONUS_BLOCK_SPEED;
    }

    @Override
    public void start() {
        System.out.println("Up:" + getName() + " spawned at " + getPosition());
    }

    @Override
    public void update(double deltaTime) {
        if (collected) return;

        position.y += velocityY * deltaTime;

        if (position.y > Config.SCREEN_HEIGHT) {
            collected = true;
            System.out.println("Up:" + getName() + " disappeared off screen");
        }
    }

    public boolean isCollected() { return collected; }
    public void collect() { collected = true; }

    public abstract void applyEffect(Paddle paddle, Ball ball);
}