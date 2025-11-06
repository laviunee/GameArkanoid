package UI;

import Engine.SceneManager;
import Utils.Config;
import Utils.SoundManager;
import Utils.SpriteLoader;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.InputStream;

public class MenuScene extends SceneManager {
    private GraphicsContext ctx;
    private SoundManager soundManager;
    private SpriteLoader spriteLoader;
    private Runnable onStartGame;
    private Runnable onHighscoreSelected;
    private Runnable onLevelSelectSelected; // THÊM: Callback cho level select

    // Menu state
    private boolean isActive;
    private int selectedOption;
    private final String[] mainOptions = {"START GAME", "LEVEL SELECT", "INSTRUCTION", "CREDITS", "HIGHSCORE" , "EXIT"}; // THÊM LEVEL SELECT
    private MenuState currentState;
    private long lastInputTime;

    // UI elements
    private Font titleFont;
    private Font optionFont;
    private Font infoFont;
    private Font creditsFont;

    // Animation
    private double titleYOffset;
    private double titlePulse;
    private long lastUpdateTime;

    // Sprites
    private javafx.scene.image.Image menuBackground;
    private javafx.scene.image.Image titleImage;

    // Menu states
    private enum MenuState {
        MAIN_MENU,
        INSTRUCTION,
        CREDITS
    }

    // THÊM: Constructor với level select callback
    public MenuScene(GraphicsContext ctx, Runnable onStartGame, Runnable onHighscoreSelected, Runnable onLevelSelectSelected) {
        this.ctx = ctx;
        this.soundManager = SoundManager.getInstance();
        this.spriteLoader = SpriteLoader.getInstance();
        this.onStartGame = onStartGame;
        this.onHighscoreSelected = onHighscoreSelected;
        this.onLevelSelectSelected = onLevelSelectSelected; // THÊM

        loadFontsFromResources();

        // Initialize state
        this.currentState = MenuState.MAIN_MENU;
        this.selectedOption = 0;
        this.lastInputTime = System.currentTimeMillis();
        this.lastUpdateTime = System.currentTimeMillis();

        loadMenuSprites();
    }

    // GIỮ NGUYÊN constructor cũ cho tương thích
    public MenuScene(GraphicsContext ctx, Runnable onStartGame, Runnable onHighscoreSelected) {
        this(ctx, onStartGame, onHighscoreSelected, null);
    }

    private void loadFontsFromResources() {
        try {
            // 🎯 CHỈ LOAD TITLE FONT TỪ FILE
            InputStream titleFontStream = getClass().getResourceAsStream("/fonts/MomoTrustDisplay-Regular.ttf");
            if (titleFontStream != null) {
                titleFont = Font.loadFont(titleFontStream, 80);
                titleFontStream.close();
                System.out.println("✅ Menu title font loaded successfully");
            } else {
                System.err.println("❌ Title font not found, using fallback");
                titleFont = Font.font("Impact", FontWeight.BOLD, 80);
            }

        } catch (Exception e) {
            System.err.println("❌ Error loading menu fonts: " + e.getMessage());
            setFallbackFonts();
        }

        // 🎯 CÁC FONT KHÁC DÙNG HỆ THỐNG
        setSystemFonts();
    }

    private void setSystemFonts() {
        // 🎯 OPTION FONT - DÙNG FONT HỆ THỐNG
        optionFont = Font.font("Arial", FontWeight.BOLD, 32); // Giảm size để fit thêm option

        // 🎯 INFO FONT - DÙNG FONT HỆ THỐNG
        infoFont = Font.font("Courier New", FontWeight.BOLD, 15);

        // 🎯 CREDITS FONT - DÙNG FONT HỆ THỐNG
        creditsFont = Font.font("Courier New", FontWeight.BOLD, 20);
    }

    private void setFallbackFonts() {
        titleFont = Font.font("Impact", FontWeight.BOLD, 80);
        optionFont = Font.font("Arial", FontWeight.BOLD, 28);
        infoFont = Font.font("Courier New", FontWeight.NORMAL, 18);
        creditsFont = Font.font("Verdana", FontWeight.NORMAL, 20);
    }

    private void loadMenuSprites() {
        try {
            String[] bgPaths = {
                    "/images/backgrounds/menu.png",
                    "/images/backgrounds/menu.png",
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
        drawBackground();

        switch (currentState) {
            case MAIN_MENU -> drawMainMenu();
            case INSTRUCTION -> drawInstructionScreen();
            case CREDITS -> drawCreditsScreen();
        }
    }

    private void clearScreen() {
        ctx.clearRect(0, 0, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT);
    }

    private void drawBackground() {
        if (menuBackground != null && !menuBackground.isError()) {
            ctx.drawImage(menuBackground, 0, 0, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT);
        } else {
            for (int i = 0; i < Config.SCREEN_HEIGHT; i += 2) {
                double progress = (double) i / Config.SCREEN_HEIGHT;
                Color color = Color.hsb(240, 0.8, 0.2 + progress * 0.3);
                ctx.setFill(color);
                ctx.fillRect(0, i, Config.SCREEN_WIDTH, 2);
            }

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
        drawTitle();
        drawMainMenuOptions();
        drawInstructions();
    }

    private void drawTitle() {
        double pulse = Math.sin(titlePulse) * 5;
        ctx.setFont(titleFont);

        // Gradient & stroke
        LinearGradient gradient = new LinearGradient(
                0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#FF00FF")), // Hồng neon
                new Stop(1, Color.web("#00FFFF"))  // Xanh neon
        );
        ctx.setFill(gradient);
        ctx.setStroke(Color.web("#FFFFFF"));
        ctx.setLineWidth(2.5);

        // Tạo Text object để đo width
        javafx.scene.text.Text text = new javafx.scene.text.Text("ARKANOID");
        text.setFont(titleFont);
        double textWidth = text.getLayoutBounds().getWidth();

        double x = (Config.SCREEN_WIDTH - textWidth) / 2;
        double y = 180 + titleYOffset + pulse;

        ctx.strokeText("ARKANOID", x, y);
        ctx.fillText("ARKANOID", x, y);
    }


    private void drawMainMenuOptions() {
        // 🎯 OPTIONS DÙNG FONT HỆ THỐNG
        ctx.setFont(optionFont);
        String[] options = getCurrentOptions();

        // Điều chỉnh vị trí để fit thêm option
        int startY = 335;
        int optionSpacing = 50;

        for (int i = 0; i < options.length; i++) {
            if (i == selectedOption) {
                ctx.setFill(Color.YELLOW);
                ctx.setStroke(Color.ORANGE);
                ctx.setLineWidth(2);
                ctx.strokeText("➤ " + options[i], Config.SCREEN_WIDTH / 2 - 150, startY + i * optionSpacing);
                ctx.fillText("➤ " + options[i], Config.SCREEN_WIDTH / 2 - 150, startY + i * optionSpacing);
            } else {
                ctx.setFill(Color.WHITE);
                ctx.fillText(options[i], Config.SCREEN_WIDTH / 2 - 120, startY + i * optionSpacing);
            }
        }
    }

    private void drawInstructionScreen() {
        drawTitle();
        drawInstructionContent();
        drawHowToPlayInstructions();
    }

    private void drawInstructionContent() {
        ctx.setFont(infoFont);
        ctx.setFill(Color.WHITE);

        String[] instructions = {
                "CONTROLS:",
                "← → OR A/D OR MOUSE ⮕ Move Paddle",
                "SPACE ⮕ Launch Ball",
                "P OR ESC ⮕ Pause Game",
                "M ⮕ Toggle Sound",
                "C ⮕ Toggle Mouse",
                "",
                "GAME OBJECTIVE:",
                "BREAK ALL BRICKS WITH BALL",
                "DON'T LET BALL FALL DOWN",
                "COLLECT POWER-UPS FOR BONUSES"
        };

        for (int i = 0; i < instructions.length; i++) {
            ctx.fillText(instructions[i], Config.SCREEN_WIDTH / 2 - 200, 320 + i * 25);
        }
    }

    private void drawHowToPlayInstructions() {
        ctx.setFont(infoFont);
        ctx.setFill(Color.LIGHTGRAY);
        ctx.fillText("PRESS ENTER TO RETURN", Config.SCREEN_WIDTH / 2 - 120, 700);
    }

    private void drawCreditsScreen() {
        drawTitle();
        drawCredits();
        drawCreditsInstructions();
    }

    private void drawCredits() {
        ctx.setFont(creditsFont);
        ctx.setFill(Color.WHITE);
        String[] credits = {
                "GAME DEVELOPED BY:",
                "GROUP 08",
                "",
                "TEAM MEMBERS:",
                "Vũ Thị Lâm Anh",
                "Nguyễn Minh Thư",
                "Nguyễn Thị Chung"
        };

        for (int i = 0; i < credits.length; i++) {
            ctx.fillText(credits[i], Config.SCREEN_WIDTH / 2 - 100, 350 + i * 30);
        }
    }

    private void drawInstructions() {
        // 🎯 MAIN MENU INSTRUCTIONS DÙNG FONT HỆ THỐNG
        ctx.setFont(infoFont);
        ctx.setFill(Color.LIGHTGRAY);
        ctx.fillText("USE ↑↓ OR W/S TO NAVIGATE", Config.SCREEN_WIDTH / 2 - 120, 660);
        ctx.fillText("PRESS ENTER TO SELECT", Config.SCREEN_WIDTH / 2 - 120, 690);
        ctx.fillText("PRESS M TO TOGGLE SOUND", Config.SCREEN_WIDTH / 2 - 120, 720);
    }

    private void drawCreditsInstructions() {
        // 🎯 CREDITS INSTRUCTIONS DÙNG FONT HỆ THỐNG
        ctx.setFont(infoFont);
        ctx.setFill(Color.LIGHTGRAY);
        ctx.fillText("PRESS ENTER TO RETURN", Config.SCREEN_WIDTH / 2 - 120, 700);
    }

    private void updateAnimations(double deltaTime) {
        long currentTime = System.currentTimeMillis();
        titleYOffset = Math.sin(currentTime * 0.001) * 10;
        titlePulse += deltaTime * 2;
        lastUpdateTime = currentTime;
    }

    @Override
    public void handleInput(KeyEvent event) {
        if (!isActive) return;
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastInputTime < 150) return;
        lastInputTime = currentTime;

        if (event.getEventType() == KeyEvent.KEY_PRESSED) {
            handleKeyPress(event);
        }

        if (event.getCode() == KeyCode.H) {
            soundManager.playSound("hit");
            onHighscoreSelected.run();
        }
    }

    private void handleKeyPress(KeyEvent event) {
        switch (currentState) {
            case MAIN_MENU -> handleMainMenuInput(event);
            case INSTRUCTION -> handleInstructionInput(event);
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

    private void handleInstructionInput(KeyEvent event) {
        switch (event.getCode()) {
            case ENTER, SPACE, ESCAPE -> returnToMainMenu();
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
            case 0 -> startGame();           // START GAME
            case 1 -> showLevelSelect();     // LEVEL SELECT ← THÊM
            case 2 -> showInstruction();     // INSTRUCTION
            case 3 -> showCredits();         // CREDITS
            case 4 -> showHighscore();       // HIGHSCORE
            case 5 -> exitGame();            // EXIT
        }
    }

    private void startGame() {
        if (onStartGame != null) {
            onStartGame.run();
        }
    }

    // THÊM: Method cho Level Select
    private void showLevelSelect() {
        if (onLevelSelectSelected != null) {
            onLevelSelectSelected.run();
        } else {
            System.err.println("❌ Level select callback is null!");
            soundManager.playSound("menu_select");
            startGame();
        }
    }

    private void showInstruction() {
        currentState = MenuState.INSTRUCTION;
    }

    private void showCredits() {
        currentState = MenuState.CREDITS;
    }

    private void showHighscore() {
        if (onHighscoreSelected != null) {
            onHighscoreSelected.run();
        }
    }

    private void returnToMainMenu() {
        currentState = MenuState.MAIN_MENU;
        selectedOption = 0;
    }

    private void toggleSound() {
        boolean newState = !soundManager.isSoundEnabled();
        soundManager.setSoundEnabled(newState);
        render();
    }

    private void exitGame() {
        System.exit(0);
    }

    private String[] getCurrentOptions() {
        return mainOptions;
    }

    @Override
    public void cleanup() {
        isActive = false;
    }

    public boolean isActive() { return isActive; }
    public MenuState getCurrentState() { return currentState; }
    public int getSelectedOption() { return selectedOption; }
}