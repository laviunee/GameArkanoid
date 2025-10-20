package Core;

import Utils.Vector2D;

/**
 * Lớp cha cho mọi đối tượng trong game
 */
public abstract class GameObject {
    protected String name;
    protected boolean active = true;
    protected Vector2D position;

    public GameObject(String name) {
        this.name = name;
        this.position = new Vector2D(0, 0);
    }

    public GameObject(String name, double x, double y) {
        this(name);
        this.position.set(x, y);
    }

    // Phương thức trừu tượng
    public abstract void start();
    public abstract void update(double deltaTime);

    // Getter/Setter
    public String getName() { return name; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Vector2D getPosition() { return position; }

    public void setPosition(double x, double y) {
        position.set(x, y);
    }
}