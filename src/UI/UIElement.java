package UI;

import javafx.scene.canvas.GraphicsContext;

/**
 * Lớp cơ sở cho các phần tử UI (button, text, panel...)
 */
public abstract class UIElement {
    protected double x, y;
    protected double width, height;
    protected boolean visible = true;
    protected boolean interactive = false;

    public UIElement(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public abstract void render(GraphicsContext gc);
    public abstract void update(double deltaTime);

    public boolean contains(double px, double py) {
        return px >= x && px <= x + width && py >= y && py <= y + height;
    }

    public void onClick() {
        // Override trong subclass
    }

    public void onHover() {
        // Override trong subclass
    }

    // Getters & Setters
    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
    public boolean isInteractive() { return interactive; }
    public void setInteractive(boolean interactive) { this.interactive = interactive; }
}