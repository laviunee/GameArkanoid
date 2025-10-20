package Utils;

public class Vector2D {
    public double x, y;

    public Vector2D() { this(0, 0); }
    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void set(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void add(double dx, double dy) {
        this.x += dx;
        this.y += dy;
    }

    public void multiply(double scalar) {
        this.x *= scalar;
        this.y *= scalar;
    }

    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }

    public void normalize() {
        double mag = magnitude();
        if (mag > 0) {
            x /= mag;
            y /= mag;
        }
    }

    @Override
    public String toString() {
        return String.format("(%.2f, %.2f)", x, y);
    }
}