package UI;

import Engine.GameEngine;
import Engine.SceneManager;
import Utils.Config;
import Utils.SaveManager;
import Utils.SoundManager;
import Utils.SpriteLoader;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class LevelSelectScene extends SceneManager {
    private GraphicsContext ctx;
    private GameEngine gameEngine;
    private SoundManager soundManager;
    private SpriteLoader spriteLoader;

    private Image backgroundImage;

    private int unlockedLevels;
    private int selectedLevel = 1;
    private int totalLevels = 5;

    private LevelButton[] levelButtons;

    public LevelSelectScene(GraphicsContext ctx, GameEngine gameEngine) {
        this.ctx = ctx;
        this.gameEngine = gameEngine;
        this.soundManager = SoundManager.getInstance();
        this.spriteLoader = SpriteLoader.getInstance();

        loadBackground();
        loadUnlockedLevels();
        setupLevelButtons();
    }

    private void loadBackground() {
        String path = "/images/backgrounds/levelselect_bg.png";
        backgroundImage = spriteLoader.loadSprite(path, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT, true, false);

        if (backgroundImage == null || backgroundImage.isError()) {
            System.err.println("⚠️ LevelSelectScene background not found: " + path + ". Using fallback.");
            backgroundImage = null;
        } else {
            System.out.println("✅ LevelSelectScene background loaded: " + path);
        }
    }

    @Override
    public void start() {
        System.out.println("🎮 LevelSelectScene: Starting...");
        loadUnlockedLevels();
        selectedLevel = Math.min(unlockedLevels, 1);
    }

    private void loadUnlockedLevels() {
        SaveManager saveManager = SaveManager.getInstance();
        unlockedLevels = saveManager.getUnlockedLevels();
        if (unlockedLevels > totalLevels) unlockedLevels = totalLevels;
        System.out.println("Unlocked levels: " + unlockedLevels);
    }

    private void setupLevelButtons() {
        levelButtons = new LevelButton[totalLevels];
        int cols = 3;
        int buttonSize = 80;
        int buttonSpacing = 30;
        int startX = (int) (Config.SCREEN_WIDTH - (cols * buttonSize + (cols - 1) * buttonSpacing)) / 2;
        int startY = 150;

        for (int i = 0; i < totalLevels; i++) {
            int row = i / cols;
            int col = i % cols;
            int x = startX + col * (buttonSize + buttonSpacing);
            int y = startY + row * (buttonSize + buttonSpacing);
            levelButtons[i] = new LevelButton(i + 1, x, y, buttonSize, buttonSize);
        }
    }

    @Override
    public void update(double deltaTime) {
    }

    @Override
    public void render() {
        drawBackground();
        drawTitle();
        drawLevelButtons();
        drawInstructions();
    }

    private void drawBackground() {
        if (backgroundImage != null) {
            ctx.drawImage(backgroundImage, 0, 0, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT);
        } else {
            ctx.setFill(Color.DARKBLUE);
            ctx.fillRect(0, 0, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT);
            ctx.setFill(Color.rgb(30, 30, 60));
            for (int i = 0; i < Config.SCREEN_WIDTH; i += 40) {
                for (int j = 0; j < Config.SCREEN_HEIGHT; j += 40) {
                    ctx.fillRect(i, j, 2, 2);
                }
            }
        }
    }

    private void drawTitle() {
        ctx.setFill(Color.GOLD);
        ctx.setFont(Font.font("Arial", 36));
        ctx.fillText("SELECT LEVEL", Config.SCREEN_WIDTH / 2 - 100, 80);

        ctx.setFill(Color.WHITE);
        ctx.setFont(Font.font("Arial", 16));
        ctx.fillText("Unlocked: " + unlockedLevels + "/" + totalLevels, Config.SCREEN_WIDTH / 2 - 50, 110);
    }

    private void drawLevelButtons() {
        for (LevelButton button : levelButtons) drawLevelButton(button);
    }

    private void drawLevelButton(LevelButton button) {
        boolean isUnlocked = button.level <= unlockedLevels;
        boolean isSelected = button.level == selectedLevel;

        if (isSelected) ctx.setFill(Color.GOLD);
        else if (isUnlocked) ctx.setFill(Color.ROYALBLUE);
        else ctx.setFill(Color.DARKGRAY);

        ctx.fillRoundRect(button.x, button.y, button.width, button.height, 15, 15);

        ctx.setStroke(isSelected ? Color.WHITE : Color.LIGHTGRAY);
        ctx.setLineWidth(isSelected ? 3 : 2);
        ctx.strokeRoundRect(button.x, button.y, button.width, button.height, 15, 15);

        ctx.setFill(isUnlocked ? Color.WHITE : Color.GRAY);
        ctx.setFont(Font.font("Arial", 24));
        ctx.fillText(String.valueOf(button.level), button.x + button.width / 2 - 8, button.y + button.height / 2 + 8);

        if (!isUnlocked) {
            ctx.setFill(Color.GOLD);
            ctx.setFont(Font.font("Arial", 20));
            ctx.fillText("🔒", button.x + button.width / 2 - 10, button.y + button.height - 10);
        }

        if (SaveManager.getInstance().isLevelCompleted(button.level)) {
            ctx.setFill(Color.LIME);
            ctx.setFont(Font.font("Arial", 16));
            ctx.fillText("✓", button.x + button.width - 20, button.y + 20);
        }
    }

    private void drawInstructions() {
        ctx.setFill(Color.WHITE);
        ctx.setFont(Font.font("Arial", 14));
        ctx.fillText("← → : Select Level", Config.SCREEN_WIDTH / 2 - 60, Config.SCREEN_HEIGHT - 60);
        ctx.fillText("ENTER : Play Level", Config.SCREEN_WIDTH / 2 - 60, Config.SCREEN_HEIGHT - 40);
        ctx.fillText("ESC : Back to Menu", Config.SCREEN_WIDTH / 2 - 60, Config.SCREEN_HEIGHT - 20);
    }

    @Override
    public void handleInput(KeyEvent event) {
        if (event.getEventType() == KeyEvent.KEY_PRESSED) {
            switch (event.getCode()) {
                case LEFT, A -> selectPreviousLevel();
                case RIGHT, D -> selectNextLevel();
                case ENTER, SPACE -> startSelectedLevel();
                case ESCAPE -> gameEngine.switchToMenuScene();
                case DIGIT1 -> selectLevel(1);
                case DIGIT2 -> selectLevel(2);
                case DIGIT3 -> selectLevel(3);
                case DIGIT4 -> selectLevel(4);
                case DIGIT5 -> selectLevel(5);
            }
        }
    }

    private void selectPreviousLevel() {
        if (selectedLevel > 1) {
            selectedLevel--;
            soundManager.playSound("menu_select");
        }
    }

    private void selectNextLevel() {
        if (selectedLevel < Math.min(unlockedLevels, totalLevels)) {
            selectedLevel++;
            soundManager.playSound("menu_select");
        }
    }

    private void selectLevel(int level) {
        if (level <= unlockedLevels && level <= totalLevels) {
            selectedLevel = level;
            soundManager.playSound("menu_select");
        }
    }

    private void startSelectedLevel() {
        if (selectedLevel <= unlockedLevels) {
            soundManager.playSound("menu_confirm");
            gameEngine.startGameAtLevel(selectedLevel - 1);
        } else soundManager.playSound("menu_error");
    }

    public void handleMouseInput(MouseEvent event) {
        if (event.getEventType() == MouseEvent.MOUSE_CLICKED) {
            double mouseX = event.getX();
            double mouseY = event.getY();
            for (LevelButton button : levelButtons) {
                if (button.contains(mouseX, mouseY)) {
                    selectLevel(button.level);
                    startSelectedLevel();
                    break;
                }
            }
        }
    }

    @Override
    public void cleanup() {
        System.out.println("Cleaning up LevelSelectScene...");
    }

    private static class LevelButton {
        int level;
        double x, y, width, height;

        LevelButton(int level, double x, double y, double width, double height) {
            this.level = level;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        boolean contains(double px, double py) {
            return px >= x && px <= x + width && py >= y && py <= y + height;
        }
    }
}
