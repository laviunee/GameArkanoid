package Core;

import Utils.Vector2D;

/**
 * GameObject có khả năng di chuyển
 */
public abstract class MovableObject extends GameObject {
    protected Vector2D velocity; // v
    protected Vector2D acceleration; // a

    public MovableObject(String name) {
        super(name);
        this.velocity = new Vector2D(0, 0);
        this.acceleration = new Vector2D(0, 0);
    }

    public MovableObject(String name, double x, double y) {
        super(name, x, y);
        this.velocity = new Vector2D(0, 0);
        this.acceleration = new Vector2D(0, 0);
    }

    @Override
    public void update(double deltaTime) {
        // Cập nhật vận tốc theo gia tốc: v = vo + at
        velocity.x += acceleration.x * deltaTime;
        velocity.y += acceleration.y * deltaTime;

        // Cập nhật vị trí theo vận tốc: x = xo + vt
        position.x += velocity.x * deltaTime;
        position.y += velocity.y * deltaTime;
    }

    public Vector2D getVelocity() {
        return velocity;
    }

    public void setVelocity(double vx, double vy) {
        velocity.set(vx, vy);
    }

    // lực tác động vào obj -> thay đổi a
    public void applyForce(double fx, double fy) {
        acceleration.x += fx;
        acceleration.y += fy;
    }
}