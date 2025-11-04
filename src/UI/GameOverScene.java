package UI;

import Engine.SceneManager;
import Utils.Config;
import Utils.SoundManager;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

/**
 * GameOverScene - Cảnh kết thúc game
 */
public class GameOverScene extends SceneManager {
    private GraphicsContext ctx;
    private Runnable onRestartGame;
    private Runnable onReturnToMenu;
    private SoundManager soundManager;

    private boolean isActive;
    private int selectedOption;
    private final String[] gameOverOptions = {"PLAY AGAIN", "MAIN MENU"};

    private int finalScore;
    private int finalLevel;

    private Font titleFont;
    private Font scoreFont;
    private Font optionFont;
    private Font infoFont;

    // Animation
    private double pulseValue;
    private long lastUpdateTime;

    public GameOverScene(GraphicsContext ctx, int score, int level, Runnable onRestartGame, Runnable onReturnToMenu) {
        this.ctx = ctx;
        this.finalScore = score;
        this.finalLevel = level;
        this.onRestartGame = onRestartGame;
        this.onReturnToMenu = onReturnToMenu;
        this.soundManager = SoundManager.getInstance();

        initializeFonts();
    }

    private void initializeFonts() {
        this.titleFont = Font.font("Arial", 48);
        this.scoreFont = Font.font("Arial", 28);
        this.optionFont = Font.font("Arial", 24);
        this.infoFont = Font.font("Arial", 16);
    }

    @Override
    public void start() {
        isActive = true;
        selectedOption = 0;
        pulseValue = 0;
        lastUpdateTime = System.currentTimeMillis();

        // Play game over sound
        soundManager.playSound("gameover");
    }

    @Override
    public void update(double deltaTime) {
        // Update animation
        long currentTime = System.currentTimeMillis();
        pulseValue = Math.sin(currentTime * 0.005) * 0.3 + 0.7;
        lastUpdateTime = currentTime;
    }

    @Override
    public void render() {
        drawOverlay();
        drawGameOverMenu();
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
            case LEFT, A, UP, W -> {
                moveSelectionUp();
                soundManager.playSound("hit");
            }
            case RIGHT, D, DOWN, S -> {
                moveSelectionDown();
                soundManager.playSound("hit");
            }
            case ENTER, SPACE -> {
                selectOption();
                soundManager.playSound("powerup");
            }
            case M -> toggleSound();
        }
    }

    private void moveSelectionUp() {
        selectedOption = (selectedOption - 1 + gameOverOptions.length) % gameOverOptions.length;
    }

    private void moveSelectionDown() {
        selectedOption = (selectedOption + 1) % gameOverOptions.length;
    }

    private void selectOption() {
        switch (selectedOption) {
            case 0: // PLAY AGAIN
                restartGame();
                break;
            case 1: // MAIN MENU
                returnToMenu();
                break;
        }
    }

    private void restartGame() {
        if (onRestartGame != null) {
            soundManager.stopAllSounds();
            onRestartGame.run();
        }
    }

    private void returnToMenu() {
        if (onReturnToMenu != null) {
            soundManager.stopAllSounds();
            onReturnToMenu.run();
        }
    }

    private void toggleSound() {
        boolean newState = !soundManager.isSoundEnabled();
        soundManager.setSoundEnabled(newState);
    }

    private void drawOverlay() {
        // Gradient background
        for (int i = 0; i < Config.SCREEN_HEIGHT; i += 2) {
            double progress = (double) i / Config.SCREEN_HEIGHT;
            Color color = Color.hsb(0, 0.8, 0.1 + progress * 0.2);
            ctx.setFill(color);
            ctx.fillRect(0, i, Config.SCREEN_WIDTH, 2);
        }

        // Dark overlay
        ctx.setFill(Color.rgb(0, 0, 0, 0.7));
        ctx.fillRect(0, 0, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT);
    }

    private void drawGameOverMenu() {
        // Lưu alignment hiện tại
        TextAlignment originalAlignment = ctx.getTextAlign();

        // Căn giữa tất cả
        ctx.setTextAlign(TextAlignment.CENTER);
        double centerX = Config.SCREEN_WIDTH / 2;

        // Tiêu đề với hiệu ứng pulse
        ctx.setFont(titleFont);
        ctx.setFill(Color.rgb(255, 50, 50, pulseValue));
        ctx.fillText("GAME OVER", centerX, 150);

        // Thông tin điểm số
        ctx.setFont(scoreFont);
        ctx.setFill(Color.YELLOW);
        ctx.fillText("FINAL SCORE: " + finalScore, centerX, 220);

        ctx.setFill(Color.CYAN);
        ctx.fillText("LEVEL REACHED: " + finalLevel, centerX, 260);

        // Vẽ các tùy chọn
        ctx.setFont(optionFont);
        for (int i = 0; i < gameOverOptions.length; i++) {
            if (i == selectedOption) {
                ctx.setFill(Color.CYAN);
                ctx.fillText("> " + gameOverOptions[i] + " <",
                        centerX, 320 + i * 50);
            } else {
                ctx.setFill(Color.WHITE);
                ctx.fillText(gameOverOptions[i],
                        centerX, 320 + i * 50);
            }
        }

        // Hướng dẫn
        ctx.setFont(infoFont);
        ctx.setFill(Color.LIGHTGRAY);
        ctx.fillText("USE ↑↓ OR ←→ TO NAVIGATE", centerX, 450);
        ctx.fillText("PRESS ENTER TO SELECT", centerX, 475);
        ctx.fillText("PRESS M TO TOGGLE SOUND", centerX, 500);

        // Reset về alignment ban đầu
        ctx.setTextAlign(originalAlignment);
    }

    public boolean isActive() { return isActive; }
}