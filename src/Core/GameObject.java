package Core;

import Utils.Vector2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

/**
 * Lớp cha cho mọi đối tượng trong game (tên, ảnh, vị trí, active)
 */
public abstract class GameObject {
    protected String name;
    protected boolean active = true;
    protected Vector2D position;
    protected Image sprite;

    public GameObject(String name) {
        this.name = name;
        this.position = new Vector2D(0, 0);
    }

    public GameObject(String name, double x, double y) {
        this.name = name;
        this.position = new Vector2D(x, y);
    }

    // sprite
    public void setSprite(Image sprite) {
        this.sprite = sprite;
    }
    public Image getSprite() {
        return sprite;
    }

    // render để vẽ hình (nếu có)
    public void render(GraphicsContext gc) {
        if (sprite != null && active) {
            gc.drawImage(sprite, position.x, position.y);
        }
    }

    // Phương thức trừu tượng
    public abstract void start();
    public abstract void update(double deltaTime);

    // Getter/Setter
    public String getName() {
        return name;
    }
    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }
    public Vector2D getPosition() {
        return position;
    }
    public void setPosition(double x, double y) {
        position.set(x, y);
    }
}