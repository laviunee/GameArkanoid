import Utils.Vector2D;

/**
 * Component Collider - dựa trên Bounds từ code cũ
 */

// cân nhắc có thể xóa - sau khi hiểu code nếu thấy xóa được thì xóa
public class Collider {
    private Vector2D position;
    private double width, height;
    private boolean enabled = true;

    public Collider() {
        this.position = new Vector2D();
        this.width = 0;
        this.height = 0;
    }

    public Collider(double width, double height) {
        this();
        this.width = width;
        this.height = height;
    }

    public void setSize(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public void setPosition(double x, double y) {
        position.set(x, y);
    }

    public void setPosition(Vector2D position) {
        this.position.set(position.x, position.y);
    }

    // Collision detection methods từ code cũ
    public boolean contains(double px, double py) {
        if (!enabled) return false;
        return px >= position.x && px <= position.x + width &&
                py >= position.y && py <= position.y + height;
    }

    public boolean intersects(Collider other) {
        if (!enabled || !other.enabled) return false;
        return position.x < other.position.x + other.width &&
                position.x + width > other.position.x &&
                position.y < other.position.y + other.height &&
                position.y + height > other.position.y;
    }

    public boolean intersects(double ox, double oy, double oWidth, double oHeight) {
        if (!enabled) return false;
        return position.x < ox + oWidth &&
                position.x + width > ox &&
                position.y < oy + oHeight &&
                position.y + height > oy;
    }

    // Bounds properties từ code cũ
    public double getMinX() { return position.x; }
    public double getMinY() { return position.y; }
    public double getMaxX() { return position.x + width; }
    public double getMaxY() { return position.y + height; }
    public double getCenterX() { return position.x + width * 0.5; }
    public double getCenterY() { return position.y + height * 0.5; }

    // Getters
    public Vector2D getPosition() { return position; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    @Override
    public String toString() {
        return String.format("Collider(pos: %s, size: %.1fx%.1f)",
                position, width, height);
    }
}