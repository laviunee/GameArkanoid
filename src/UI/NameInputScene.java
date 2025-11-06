package UI;

import Engine.SceneManager;
import Utils.Config;
import Utils.SaveManager;
import Utils.SoundManager;
import Utils.SpriteLoader;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class NameInputScene extends SceneManager {
    private GraphicsContext ctx;
    private SoundManager soundManager;
    private SaveManager saveManager;
    private SpriteLoader spriteLoader;
    private Image backgroundImage;

    private int score;
    private int level;
    private StringBuilder playerName;
    private Runnable onNameEntered;

    public NameInputScene(GraphicsContext ctx, int score, int level, Runnable onNameEntered) {
        this.ctx = ctx;
        this.soundManager = SoundManager.getInstance();
        this.saveManager = SaveManager.getInstance();
        this.spriteLoader = SpriteLoader.getInstance();
        this.score = score;
        this.level = level;
        this.onNameEntered = onNameEntered;
        this.playerName = new StringBuilder();
        loadBackground();
    }

    private void loadBackground() {
        String[] paths = {
                "/images/backgrounds/name_input_bg.png",
                "images/backgrounds/name_input_bg.png"
        };
        for (String path : paths) {
            backgroundImage = spriteLoader.loadSprite(path, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT, true, false);
            if (backgroundImage != null && !backgroundImage.isError()) {
                System.out.println("✅ NameInputScene background loaded: " + path);
                return;
            }
        }
        System.err.println("⚠️ NameInputScene background not found. Using fallback color.");
        backgroundImage = null;
    }

    @Override
    public void start() {
        System.out.println("📝 Name Input Scene started");
        playerName.setLength(0);

        // Dừng nhạc đang phát
        soundManager.stopAllSounds();

        // Phát nhạc win
        soundManager.playSound("win");
    }

    @Override
    public void update(double deltaTime) {}

    @Override
    public void render() {
        drawBackground();

        ctx.setFill(Color.GOLD);
        ctx.setFont(new Font("Arial", 28));
        ctx.fillText("ENTER YOUR NAME", Config.SCREEN_WIDTH / 2 - 120, 80);

        ctx.setFill(Color.WHITE);
        ctx.setFont(new Font("Arial", 20));
        ctx.fillText("Score: " + score + "  Level: " + level, Config.SCREEN_WIDTH / 2 - 80, 120);

        ctx.setFill(Color.LIGHTGREEN);
        ctx.setFont(new Font("Arial", 18));
        ctx.fillText("ENTER YOUR NAME:", Config.SCREEN_WIDTH / 2 - 100, 180);

        ctx.setFill(Color.YELLOW);
        ctx.setFont(new Font("Arial", 24));
        String displayName = playerName.length() > 0 ? playerName.toString() : "_";
        ctx.fillText(displayName, Config.SCREEN_WIDTH / 2 - 50, 220);

        ctx.setFill(Color.LIGHTGRAY);
        ctx.setFont(new Font("Arial", 14));
        ctx.fillText("Press ENTER to confirm", Config.SCREEN_WIDTH / 2 - 80, 260);
        ctx.fillText("Press BACKSPACE to delete", Config.SCREEN_WIDTH / 2 - 85, 280);
        ctx.fillText("Max 10 characters", Config.SCREEN_WIDTH / 2 - 60, 300);
    }

    private void drawBackground() {
        if (backgroundImage != null) {
            ctx.drawImage(backgroundImage, 0, 0, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT);
        } else {
            ctx.setFill(Color.DARKSLATEBLUE);
            ctx.fillRect(0, 0, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT);
        }
    }

    @Override
    public void handleInput(KeyEvent event) {
        if (event.getEventType() == KeyEvent.KEY_PRESSED) {
            if (event.getCode() == KeyCode.ENTER) {
                soundManager.playSound("powerup");
                saveManager.saveHighscore(
                        playerName.length() > 0 ? playerName.toString() : "Anonymous",
                        score, level
                );
                onNameEntered.run();
            } else if (event.getCode() == KeyCode.BACK_SPACE) {
                if (playerName.length() > 0) {
                    playerName.deleteCharAt(playerName.length() - 1);
                    soundManager.playSound("hit");
                }
            } else if (event.getCode().isLetterKey() || event.getCode().isDigitKey()) {
                if (playerName.length() < 10) {
                    playerName.append(event.getText().toUpperCase());
                    soundManager.playSound("hit");
                }
            }
        }
    }

    @Override
    public void cleanup() {
        System.out.println("📝 Name Input Scene cleaned up");
        soundManager.stopAllSounds();
    }
}
