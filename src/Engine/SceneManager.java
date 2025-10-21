package Engine;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;

/**
 * Lớp trừu tượng cho tất cả các Scene trong game
 */
public abstract class SceneManager {
    public abstract void start();
    public abstract void update(double deltaTime);
    public abstract void render();
    public abstract void handleInput(KeyEvent event);
    public abstract void cleanup();
}