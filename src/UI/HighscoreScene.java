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
import javafx.scene.text.FontWeight;

import java.util.List;

public class HighscoreScene extends SceneManager {
    private GraphicsContext ctx;
    private SoundManager soundManager;
    private SaveManager saveManager;
    private SpriteLoader spriteLoader;
    private Image backgroundImage;

    private List<SaveManager.HighscoreEntry> highscores;
    private Runnable returnCallback;

    public HighscoreScene(GraphicsContext ctx, Runnable returnCallback) {
        this.ctx = ctx;
        this.soundManager = SoundManager.getInstance();
        this.saveManager = SaveManager.getInstance();
        this.spriteLoader = SpriteLoader.getInstance();
        this.returnCallback = returnCallback;

        loadBackground();
    }

    private void loadBackground() {
        String path = "/images/backgrounds/highscore_bg.png";
        backgroundImage = spriteLoader.loadSprite(path, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT, true, false);
        if (backgroundImage == null || backgroundImage.isError()) {
            System.err.println("⚠️ HighscoreScene background not found: " + path + ". Using gradient fallback.");
            backgroundImage = null;
        } else {
            System.out.println("✅ HighscoreScene background loaded: " + path);
        }
    }

    @Override
    public void start() {
        // LUÔN load highscores mới nhất
        this.highscores = saveManager.getHighscores();
        System.out.println("Highscore Scene started - Loaded " + highscores.size() + " highscores");
    }

    @Override
    public void update(double deltaTime) {
    }

    @Override
    public void render() {
        drawBackground();
        drawTitle();
        drawTableHeader();
        drawHighscores();
        drawInstructions();
    }

    private void drawBackground() {
        if (backgroundImage != null) {
            ctx.drawImage(backgroundImage, 0, 0, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT);
        } else {
            for (int i = 0; i < Config.SCREEN_HEIGHT; i++) {
                double progress = (double) i / Config.SCREEN_HEIGHT;
                Color color = Color.hsb(240, 0.8, 0.1 + progress * 0.3);
                ctx.setFill(color);
                ctx.fillRect(0, i, Config.SCREEN_WIDTH, 1);
            }
        }
    }

    private void drawTitle() {
        ctx.setFill(Color.GOLD);
        ctx.setFont(Font.font("Arial", 36));
        ctx.fillText("HIGH SCORES", Config.SCREEN_WIDTH / 2 - 100, 60);
    }

    private void drawTableHeader() {
        ctx.setFill(Color.LIGHTGRAY);
        ctx.setFont(Font.font("Arial", 20));
        ctx.fillText("RANK", 100, 120);
        ctx.fillText("NAME", 200, 120);
        ctx.fillText("SCORE", 350, 120);
        ctx.fillText("LEVEL", 450, 120);

        ctx.setStroke(Color.WHITE);
        ctx.setLineWidth(1);
        ctx.strokeLine(80, 130, 520, 130);
    }

    private void drawHighscores() {
        if (highscores.isEmpty()) {
            ctx.setFill(Color.WHITE);
            ctx.setFont(Font.font("Arial", 20));
            ctx.fillText("NO HIGHSCORES YET!", Config.SCREEN_WIDTH / 2 - 100, 200);
            return;
        }

        ctx.setFont(Font.font("Arial", 18));
        for (int i = 0; i < highscores.size(); i++) {
            SaveManager.HighscoreEntry entry = highscores.get(i);

            if (i == 0) ctx.setFill(Color.GOLD);
            else if (i == 1) ctx.setFill(Color.SILVER);
            else if (i == 2) ctx.setFill(Color.ORANGE);
            else ctx.setFill(Color.WHITE);

            FontWeight weight = (i <= 2) ? FontWeight.BOLD : FontWeight.NORMAL;
            ctx.setFont(Font.font("Arial", weight, (i <= 2) ? 20 : 18));

            int y = 160 + i * 30;
            ctx.fillText((i + 1) + ".", 100, y);
            ctx.fillText(entry.getPlayerName(), 200, y);
            ctx.fillText(String.valueOf(entry.getScore()), 350, y);
            ctx.fillText(String.valueOf(entry.getLevel()), 450, y);
        }
    }

    private void drawInstructions() {
        ctx.setFill(Color.LIGHTGREEN);
        ctx.setFont(Font.font("Arial", 16));
        ctx.fillText("Press ENTER to return to Menu", Config.SCREEN_WIDTH / 2 - 120, Config.SCREEN_HEIGHT - 30);
    }

    @Override
    public void handleInput(KeyEvent event) {
        if (event.getEventType() == KeyEvent.KEY_PRESSED) {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.ESCAPE) {
                soundManager.playSound("hit");
                returnCallback.run();
            }
        }
    }

    @Override
    public void cleanup() {
        System.out.println("Highscore Scene cleaned up");
    }
}
