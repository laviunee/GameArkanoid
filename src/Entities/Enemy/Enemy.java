package Entities.Enemy;

import Core.GameObject;
import Utils.Vector2D;

/**
 * Base class cho tất cả enemy
 */
public abstract class Enemy extends GameObject {
    protected double width, height;
    protected int health;
    protected int maxHealth;
    protected int scoreValue;
    protected boolean isActive;

    public Enemy(String name, double x, double y, double width, double height) {
        super(name, x, y);
        this.width = width;
        this.height = height;
        this.isActive = true;
    }

    public abstract void update(double deltaTime);
    public abstract void onHit(int damage);
    public abstract void attack();

    // Getters

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public int getScoreValue() { return scoreValue; }
    public boolean isActive() { return isActive; }
    public boolean isDestroyed() { return health <= 0; }
}