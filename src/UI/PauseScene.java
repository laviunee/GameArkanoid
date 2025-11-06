package UI;

import Engine.SceneManager;
import Utils.Config;
import Utils.SpriteLoader;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

/**
 * PauseScene - Cảnh tạm dừng game
 */
public class PauseScene extends SceneManager {
    private GraphicsContext ctx;
    private Runnable onResumeGame;
    private Runnable onRestartGame;
    private Runnable onReturnToMenu;

    private boolean isActive;
    private int selectedOption;
    private final String[] pauseOptions = {"RESUME GAME", "RESTART LEVEL", "MAIN MENU"};

    private Font titleFont;
    private Font optionFont;
    private Font infoFont;

    private Image pauseBackground;
    private SpriteLoader spriteLoader;

    public PauseScene(GraphicsContext ctx, Runnable onResumeGame, Runnable onRestartGame, Runnable onReturnToMenu) {
        this.ctx = ctx;
        this.onResumeGame = onResumeGame;
        this.onRestartGame = onRestartGame;
        this.onReturnToMenu = onReturnToMenu;

        this.titleFont = Font.font("Arial", 36);
        this.optionFont = Font.font("Arial", 24);
        this.infoFont = Font.font("Arial", 16);

        this.spriteLoader = SpriteLoader.getInstance();
        loadBackground();
    }

    private void loadBackground() {
        String[] paths = {
                "/images/backgrounds/pause_bg.png",
                "images/backgrounds/pause_bg.png"
        };

        for (String path : paths) {
            pauseBackground = spriteLoader.loadSprite(path, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT, true, false);
            if (pauseBackground != null && !pauseBackground.isError()) {
                System.out.println("✅ Pause background loaded: " + path);
                return;
            }
        }

        System.err.println("⚠️ Pause background not found. Using fallback color.");
        pauseBackground = null;
    }

    @Override
    public void start() {
        isActive = true;
        selectedOption = 0;
    }

    @Override
    public void update(double deltaTime) {
        // Pause scene k cần update logic
    }

    @Override
    public void render() {
        drawBackground();
        drawPauseMenu();
    }

    private void drawBackground() {
        if (pauseBackground != null) {
            ctx.drawImage(pauseBackground, 0, 0, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT);
        } else {
            ctx.setFill(Color.rgb(0, 0, 50, 0.8));
            ctx.fillRect(0, 0, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT);
        }
    }

    @Override
    public void handleInput(KeyEvent event) {
        if (!isActive) return;

        if (event.getEventType() == KeyEvent.KEY_PRESSED) {
            handleKeyPress(event);
        }
    }

    @Override
    public void cleanup() {
        isActive = false;
    }

    private void handleKeyPress(KeyEvent event) {
        switch (event.getCode()) {
            case UP, W -> moveSelectionUp();
            case DOWN, S -> moveSelectionDown();
            case ENTER -> selectOption();
            case P, ESCAPE -> resumeGame();
        }
    }

    private void moveSelectionUp() {
        selectedOption = (selectedOption - 1 + pauseOptions.length) % pauseOptions.length;
    }

    private void moveSelectionDown() {
        selectedOption = (selectedOption + 1) % pauseOptions.length;
    }

    private void selectOption() {
        switch (selectedOption) {
            case 0 -> resumeGame();
            case 1 -> restartLevel();
            case 2 -> returnToMenu();
        }
    }

    private void resumeGame() {
        if (onResumeGame != null) onResumeGame.run();
    }

    private void restartLevel() {
        if (onRestartGame != null) onRestartGame.run();
    }

    private void returnToMenu() {
        if (onReturnToMenu != null) onReturnToMenu.run();
    }

    private void drawPauseMenu() {
        TextAlignment originalAlignment = ctx.getTextAlign();
        ctx.setTextAlign(TextAlignment.CENTER);
        double centerX = Config.SCREEN_WIDTH / 2;

        ctx.setFont(titleFont);
        ctx.setFill(Color.YELLOW);
        ctx.fillText("GAME PAUSED", centerX, 200);

        ctx.setFont(optionFont);
        for (int i = 0; i < pauseOptions.length; i++) {
            if (i == selectedOption) {
                ctx.setFill(Color.CYAN);
                ctx.fillText("➤ " + pauseOptions[i], centerX, 280 + i * 50);
            } else {
                ctx.setFill(Color.WHITE);
                ctx.fillText(pauseOptions[i], centerX, 280 + i * 50);
            }
        }

        ctx.setFont(infoFont);
        ctx.setFill(Color.LIGHTGRAY);
        ctx.fillText("USE ↑↓ TO NAVIGATE", centerX, 425);
        ctx.fillText("PRESS ENTER TO SELECT", centerX, 450);
        ctx.fillText("PRESS P OR ESC TO RESUME", centerX, 475);

        ctx.setTextAlign(originalAlignment);
    }

    public boolean isActive() { return isActive; }
}
