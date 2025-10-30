package UI;

import Engine.SceneManager;
import Utils.Config;
import Utils.SoundManager;
import Utils.SpriteLoader;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class MenuScene extends SceneManager {
    private GraphicsContext ctx;
    private SoundManager soundManager;
    private SpriteLoader spriteLoader;
    private Runnable onStartGame;

    // Menu state
    private boolean isActive;
    private int selectedOption;
    private final String[] mainOptions = {"START GAME", "OPTIONS", "CREDITS", "EXIT"};
    private MenuState currentState;
    private long lastInputTime;

    // UI elements
    private Font titleFont;
    private Font optionFont;
    private Font infoFont;

    // Animation
    private double titleYOffset;
    private double titlePulse;
    private long lastUpdateTime;

    // CHỈ 2 sprite này trước
    private javafx.scene.image.Image menuBackground;
    private javafx.scene.image.Image titleImage;

    // Menu states
    private enum MenuState {
        MAIN_MENU,
        OPTIONS,
        CREDITS
    }

    public MenuScene(GraphicsContext ctx, Runnable onStartGame) {
        this.ctx = ctx;
        this.soundManager = SoundManager.getInstance();
        this.spriteLoader = SpriteLoader.getInstance();
        this.onStartGame = onStartGame;

        // Initialize fonts
        this.titleFont = Font.font("Arial", 48);
        this.optionFont = Font.font("Arial", 32);
        this.infoFont = Font.font("Arial", 16);

        // Initialize state
        this.currentState = MenuState.MAIN_MENU;
        this.selectedOption = 0;
        this.lastInputTime = System.currentTimeMillis();
        this.lastUpdateTime = System.currentTimeMillis();

        // CHỈ load 2 ảnh này trước
        loadMenuSprites();
    }

    private void loadMenuSprites() {
        try {
            // THỬ các đường dẫn khác nhau
            String[] bgPaths = {
                    "/images/backgrounds/menu.png",
                    "/assets/images/backgrounds/menu.png",
                    "images/backgrounds/menu.png"
            };

            for (String path : bgPaths) {
                menuBackground = spriteLoader.loadSprite(path, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT, true, false);
                if (menuBackground != null && !menuBackground.isError()) {
                    System.out.println("✅ Menu background loaded: " + path);
                    break;
                }
            }


        } catch (Exception e) {
            System.err.println("Error loading menu sprites: " + e.getMessage());
        }
    }

    @Override
    public void start() {
        isActive = true;
        selectedOption = 0;
        currentState = MenuState.MAIN_MENU;
        titleYOffset = 0;
        titlePulse = 0;
    }

    @Override
    public void update(double deltaTime) {
        if (!isActive) return;
        updateAnimations(deltaTime);
    }

    @Override
    public void render() {
        clearScreen();
        drawBackground(); // QUAN TRỌNG: vẽ background có image

        switch (currentState) {
            case MAIN_MENU -> drawMainMenu();
            case OPTIONS -> drawOptionsMenu();
            case CREDITS -> drawCreditsScreen();
        }

    }

    private void clearScreen() {
        ctx.clearRect(0, 0, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT);
    }

    private void drawBackground() {
        if (menuBackground != null && !menuBackground.isError()) {
            // Vẽ background image nếu load được
            ctx.drawImage(menuBackground, 0, 0, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT);
            System.out.println("🎨 Drawing menu background image");
        } else {
            // Fallback: vẽ background
            System.out.println("🎨 Using fallback background");
            for (int i = 0; i < Config.SCREEN_HEIGHT; i += 2) {
                double progress = (double) i / Config.SCREEN_HEIGHT;
                Color color = Color.hsb(240, 0.8, 0.2 + progress * 0.3);
                ctx.setFill(color);
                ctx.fillRect(0, i, Config.SCREEN_WIDTH, 2);
            }

            // Stars
            ctx.setFill(Color.WHITE);
            for (int i = 0; i < 50; i++) {
                double x = (i * 73) % Config.SCREEN_WIDTH;
                double y = (i * 47) % Config.SCREEN_HEIGHT;
                double size = (i % 3) + 1;
                ctx.fillOval(x, y, size, size);
            }
        }
    }

    private void drawMainMenu() {
        drawTitle(); // Vẽ title có image
        drawMainMenuOptions();
        drawInstructions();
    }

    private void drawTitle() {
        if (titleImage != null && !titleImage.isError()) {
            // Vẽ title image
            double pulse = Math.sin(titlePulse) * 5;
            double x = Config.SCREEN_WIDTH / 2 - titleImage.getWidth() / 2;
            double y = 100 + titleYOffset + pulse;
            ctx.drawImage(titleImage, x, y);
        } /*else {
            // Fallback: vẽ title bằng text
            double pulse = Math.sin(titlePulse) * 5;
            ctx.setFont(titleFont);
            ctx.setFill(Color.CYAN);
            ctx.fillText("By Group 08",
                    Config.SCREEN_WIDTH / 2 - 140,
                    150 + titleYOffset + pulse);

            ctx.setFont(Font.font("Arial", 24));
            ctx.setFill(Color.YELLOW);
            ctx.fillText("ARKANOID",
                    Config.SCREEN_WIDTH / 2 - 130,
                    200 + titleYOffset + pulse);
        }*/
    }

    private void drawMainMenuOptions() {
        ctx.setFont(optionFont);
        String[] options = getCurrentOptions();

        for (int i = 0; i < options.length; i++) {
            if (i == selectedOption) {
                ctx.setFill(Color.YELLOW);
                ctx.fillText("> " + options[i] + " <",
                        Config.SCREEN_WIDTH / 2 - 100,
                        450 + i * 60);
            } else {
                ctx.setFill(Color.WHITE);
                ctx.fillText(options[i],
                        Config.SCREEN_WIDTH / 2 - 80,
                        450 + i * 60);
            }
        }
    }

    private void drawOptionsMenu() {
        drawTitle();
        drawOptions();
        drawOptionsInstructions();
    }

    private void drawCreditsScreen() {
        drawTitle();
        drawCredits();
        drawCreditsInstructions();
    }

    private void drawOptions() {
        ctx.setFont(optionFont);
        ctx.setFill(Color.YELLOW);
        ctx.fillText("OPTIONS", Config.SCREEN_WIDTH / 2 - 70, 420);

        ctx.setFont(infoFont);
        if (selectedOption == 0) {
            ctx.setFill(Color.YELLOW);
            ctx.fillText("> SOUND: " + (soundManager.isSoundEnabled() ? "ON" : "OFF") + " <",
                    Config.SCREEN_WIDTH / 2 - 65, 480);
        } else {
            ctx.setFill(Color.WHITE);
            ctx.fillText("SOUND: " + (soundManager.isSoundEnabled() ? "ON" : "OFF"),
                    Config.SCREEN_WIDTH / 2 - 45, 480);
        }
    }

    private void drawCredits() {
        ctx.setFont(optionFont);
        ctx.setFill(Color.YELLOW);
        ctx.fillText("CREDITS", Config.SCREEN_WIDTH / 2 - 80, 450);

        ctx.setFont(infoFont);
        ctx.setFill(Color.WHITE);
        String[] credits = {"GAME DEVELOPED BY: Group 8"};
        for (int i = 0; i < credits.length; i++) {
            ctx.fillText(credits[i], Config.SCREEN_WIDTH / 2 - 130, 500 + i * 25);
        }
    }

    private void drawInstructions() {
        ctx.setFont(infoFont);
        ctx.setFill(Color.LIGHTGRAY);
        ctx.fillText("USE ↑↓ OR W/S TO NAVIGATE", Config.SCREEN_WIDTH / 2 - 120, 665);
        ctx.fillText("PRESS ENTER TO SELECT", Config.SCREEN_WIDTH / 2 - 120, 695);
        ctx.fillText("PRESS M TO TOGGLE SOUND", Config.SCREEN_WIDTH / 2 - 120, 725);
    }

    private void drawOptionsInstructions() {
        ctx.setFont(infoFont);
        ctx.setFill(Color.WHITE);
        ctx.fillText("USE ↑↓ TO SELECT OPTION", Config.SCREEN_WIDTH / 2 - 100, 550);
        ctx.fillText("USE ←→ TO ADJUST VALUE", Config.SCREEN_WIDTH / 2 - 100, 580);
        ctx.fillText("PRESS ENTER TO APPLY", Config.SCREEN_WIDTH / 2 - 80, 610);
        ctx.fillText("PRESS ESC TO CANCEL", Config.SCREEN_WIDTH / 2 - 80, 640);
    }

    private void drawCreditsInstructions() {
        ctx.setFont(infoFont);
        ctx.setFill(Color.WHITE);
        ctx.fillText("PRESS ANY KEY TO RETURN", Config.SCREEN_WIDTH / 2 - 115, 600);
    }


    private void updateAnimations(double deltaTime) {
        long currentTime = System.currentTimeMillis();
        titleYOffset = Math.sin(currentTime * 0.001) * 10;
        titlePulse += deltaTime * 2;
        lastUpdateTime = currentTime;
    }

    // Input handling giữ nguyên
    @Override
    public void handleInput(KeyEvent event) {
        if (!isActive) return;
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastInputTime < 150) return;
        lastInputTime = currentTime;

        if (event.getEventType() == KeyEvent.KEY_PRESSED) {
            handleKeyPress(event);
        }
    }

    private void handleKeyPress(KeyEvent event) {
        switch (currentState) {
            case MAIN_MENU -> handleMainMenuInput(event);
            case OPTIONS -> handleOptionsInput(event);
            case CREDITS -> handleCreditsInput(event);
        }
    }

    private void handleMainMenuInput(KeyEvent event) {
        switch (event.getCode()) {
            case UP, W -> { moveSelectionUp(); soundManager.playSound("hit"); }
            case DOWN, S -> { moveSelectionDown(); soundManager.playSound("hit"); }
            case ENTER -> { selectMainMenuOption(); soundManager.playSound("powerup"); }
            case M -> toggleSound();
            case ESCAPE -> exitGame();
        }
    }

    private void handleOptionsInput(KeyEvent event) {
        switch (event.getCode()) {
            case UP, W -> moveSelectionUp();
            case DOWN, S -> moveSelectionDown();
            case LEFT, A -> adjustOption(-1);
            case RIGHT, D -> adjustOption(1);
            case ENTER, SPACE -> applyOptions();
            case ESCAPE -> returnToMainMenu();
        }
    }

    private void handleCreditsInput(KeyEvent event) {
        switch (event.getCode()) {
            case ENTER, SPACE, ESCAPE -> returnToMainMenu();
        }
    }

    private void moveSelectionUp() {
        selectedOption = (selectedOption - 1 + getCurrentOptions().length) % getCurrentOptions().length;
    }

    private void moveSelectionDown() {
        selectedOption = (selectedOption + 1) % getCurrentOptions().length;
    }

    private void selectMainMenuOption() {
        switch (selectedOption) {
            case 0 -> startGame();
            case 1 -> showOptions();
            case 2 -> showCredits();
            case 3 -> exitGame();
        }
    }

    private void startGame() {
        if (onStartGame != null) {
            onStartGame.run();
        }
    }

    private void showOptions() {
        currentState = MenuState.OPTIONS;
        selectedOption = 0;
    }

    private void showCredits() {
        currentState = MenuState.CREDITS;
    }

    private void returnToMainMenu() {
        currentState = MenuState.MAIN_MENU;
        selectedOption = 0;
    }

    private void applyOptions() {
        returnToMainMenu();
    }

    private void adjustOption(int direction) {
        // Placeholder
    }

    private void toggleSound() {
        boolean newState = !soundManager.isSoundEnabled();
        soundManager.setSoundEnabled(newState);
    }

    private void exitGame() {
        System.exit(0);
    }

    private String[] getCurrentOptions() {
        switch (currentState) {
            case MAIN_MENU: return mainOptions;
            case OPTIONS: return new String[]{"SOUND", "MUSIC", "CONTROLS", "BACK"};
            default: return mainOptions;
        }
    }

    @Override
    public void cleanup() {
        isActive = false;
    }

    public boolean isActive() { return isActive; }
    public MenuState getCurrentState() { return currentState; }
    public int getSelectedOption() { return selectedOption; }
}