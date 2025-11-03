package UI;

import Engine.SceneManager;
import Utils.Config;
import Utils.SaveManager;
import Utils.SoundManager;
import javafx.scene.canvas.GraphicsContext;
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
    private List<SaveManager.HighscoreEntry> highscores;
    private Runnable returnCallback;

    public HighscoreScene(GraphicsContext ctx, Runnable returnCallback) {
        this.ctx = ctx;
        this.soundManager = SoundManager.getInstance();
        this.saveManager = SaveManager.getInstance();
        this.returnCallback = returnCallback;
        // KHÔNG load highscores ở constructor, sẽ load mới mỗi lần start()
    }

    @Override
    public void start() {
        // LUÔN load highscores mới nhất mỗi khi scene được bắt đầu
        this.highscores = saveManager.getHighscores();
        System.out.println("Highscore Scene started - Loaded " + highscores.size() + " highscores");

        // Debug: in ra tất cả highscores để kiểm tra
        for (int i = 0; i < highscores.size(); i++) {
            SaveManager.HighscoreEntry entry = highscores.get(i);
            System.out.println("Highscore " + (i+1) + ": " + entry.getPlayerName() + " - " + entry.getScore() + " - Level " + entry.getLevel());
        }
    }

    @Override
    public void update(double deltaTime) {
        // Không cần update thường xuyên
    }

    @Override
    public void render() {
        // Nền gradient đẹp hơn
        drawGradientBackground();

        // Tiêu đề
        drawTitle();

        // Header bảng
        drawTableHeader();

        // Vẽ các highscore
        drawHighscores();

        // Hướng dẫn
        drawInstructions();
    }

    private void drawGradientBackground() {
        for (int i = 0; i < Config.SCREEN_HEIGHT; i++) {
            double progress = (double) i / Config.SCREEN_HEIGHT;
            Color color = Color.hsb(240, 0.8, 0.1 + progress * 0.3);
            ctx.setFill(color);
            ctx.fillRect(0, i, Config.SCREEN_WIDTH, 1);
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

        // Đường kẻ ngang dưới header
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

            // Màu sắc khác nhau cho top 3
            if (i == 0) {
                ctx.setFill(Color.GOLD);
                ctx.setFont(Font.font("Arial", FontWeight.BOLD, 20));
            } else if (i == 1) {
                ctx.setFill(Color.SILVER);
                ctx.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            } else if (i == 2) {
                ctx.setFill(Color.ORANGE);
                ctx.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            } else {
                ctx.setFill(Color.WHITE);
                ctx.setFont(Font.font("Arial", 18));
            }

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