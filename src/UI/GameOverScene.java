package UI;

import Engine.SceneManager;
import Utils.Config;
import Utils.SoundManager;
import Utils.SpriteLoader;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

public class GameOverScene extends SceneManager {
    private GraphicsContext ctx;
    private Runnable onRestartGame;
    private Runnable onReturnToMenu;
    private SoundManager soundManager;
    private SpriteLoader spriteLoader;
    private Image backgroundImage;

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
        this.spriteLoader = SpriteLoader.getInstance();

        initializeFonts();
        loadBackground();
    }

    private void initializeFonts() {
        this.titleFont = Font.font("Arial", 48);
        this.scoreFont = Font.font("Arial", 28);
        this.optionFont = Font.font("Arial", 24);
        this.infoFont = Font.font("Arial", 16);
    }

    private void loadBackground() {
        String[] paths = {
                "/images/backgrounds/gameover_bg.png",
                "images/backgrounds/gameover_bg.png"
        };

        for (String path : paths) {
            backgroundImage = spriteLoader.loadSprite(path, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT, true, false);
            if (backgroundImage != null && !backgroundImage.isError()) {
                System.out.println("✅ GameOverScene background loaded: " + path);
                return;
            }
        }

        System.err.println("⚠️ GameOverScene background not found. Using gradient overlay.");
        backgroundImage = null;
    }

    @Override
    public void start() {
        isActive = true;
        selectedOption = 0;
        pulseValue = 0;
        lastUpdateTime = System.currentTimeMillis();
        soundManager.playSound("gameover");
    }

    @Override
    public void update(double deltaTime) {
        long currentTime = System.currentTimeMillis();
        pulseValue = Math.sin(currentTime * 0.005) * 0.3 + 0.7;
        lastUpdateTime = currentTime;
    }

    @Override
    public void render() {
        drawBackground();
        drawGameOverMenu();
    }

    private void drawBackground() {
        if (backgroundImage != null) {
            ctx.drawImage(backgroundImage, 0, 0, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT);
        } else {
            // Gradient background fallback
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
    }

    @Override
    public void handleInput(KeyEvent event) {
        if (!isActive) return;

        if (event.getEventType() == KeyEvent.KEY_PRESSED) {
            switch (event.getCode()) {
                case A, UP, W -> { selectedOption = (selectedOption - 1 + gameOverOptions.length) % gameOverOptions.length; soundManager.playSound("hit"); }
                case D, DOWN, S -> { selectedOption = (selectedOption + 1) % gameOverOptions.length; soundManager.playSound("hit"); }
                case ENTER, SPACE -> {
                    if (selectedOption == 0) restartGame();
                    else returnToMenu();
                    soundManager.playSound("powerup");
                }
                case M -> toggleSound();
            }
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
        soundManager.setSoundEnabled(!soundManager.isSoundEnabled());
    }

    private void drawGameOverMenu() {
        TextAlignment originalAlignment = ctx.getTextAlign();
        ctx.setTextAlign(TextAlignment.CENTER);
        double centerX = Config.SCREEN_WIDTH / 2;

        // Title
        ctx.setFont(titleFont);
        ctx.setFill(Color.rgb(255, 50, 50, pulseValue));
        ctx.fillText("GAME OVER", centerX, 150);

        // Score & level
        ctx.setFont(scoreFont);
        ctx.setFill(Color.YELLOW);
        ctx.fillText("FINAL SCORE: " + finalScore, centerX, 220);
        ctx.setFill(Color.CYAN);
        ctx.fillText("LEVEL REACHED: " + finalLevel, centerX, 260);

        // Options
        ctx.setFont(optionFont);
        for (int i = 0; i < gameOverOptions.length; i++) {
            if (i == selectedOption) ctx.setFill(Color.CYAN);
            else ctx.setFill(Color.WHITE);
            ctx.fillText(i == selectedOption ? "➤ " + gameOverOptions[i] : gameOverOptions[i], centerX, 320 + i * 50);
        }

        // Instructions
        ctx.setFont(infoFont);
        ctx.setFill(Color.LIGHTGRAY);
        ctx.fillText("USE ↑↓ TO NAVIGATE", centerX, 450);
        ctx.fillText("PRESS ENTER TO SELECT", centerX, 475);
        ctx.fillText("PRESS M TO TOGGLE SOUND", centerX, 500);

        ctx.setTextAlign(originalAlignment);
    }

    @Override
    public void cleanup() {
        isActive = false;
    }

    public boolean isActive() { return isActive; }
}
