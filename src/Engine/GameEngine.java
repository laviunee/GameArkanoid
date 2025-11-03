package Engine;

import UI.GameScene;
import UI.MenuScene;
import UI.PauseScene;
import UI.HighscoreScene;
import UI.NameInputScene;
import Utils.Config;
import Utils.SoundManager;
import Utils.SaveManager;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class GameEngine extends Application {
    private Stage primaryStage;
    private Canvas canvas;
    private GraphicsContext ctx;
    private boolean isRunning = false;
    private long lastUpdateTime;

    private PauseScene pauseScene;
    private SceneManager currentScene;
    private GameScene gameScene;
    private MenuScene menuScene;
    private SoundManager soundManager;
    private HighscoreScene highscoreScene;
    private NameInputScene nameInputScene;
    private SaveManager saveManager;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.isRunning = true;

        System.out.println("GameEngine Starting...");

        setupWindow();
        initializeManagers();
        initializeScenes();
        setupGameLoop();

        System.out.println("GameEngine Started Successfully!");
    }

    private void initializeManagers() {
        System.out.println("🎵 Initializing SoundManager...");
        soundManager = SoundManager.getInstance();
        soundManager.initialize();

        System.out.println("💾 Initializing SaveManager...");
        saveManager = SaveManager.getInstance();
        // Test sound
        soundManager.playSound("hit");
    }

    private void initializeScenes() {
        System.out.println("🎮 Initializing Scenes...");

        // Truyền callback lambda
//        menuScene = new MenuScene(ctx, () -> {
//            soundManager.onGameStart();
//            switchToGameScene();
//        }, this::switchToHighscoreScene
//        );

        menuScene = new MenuScene(ctx, this::switchToGameScene, this::switchToHighscoreScene);

        gameScene = new GameScene(ctx, this);

        // Khởi tạo PauseScene với các callback
        pauseScene = new PauseScene(ctx,
                this::resumeGame,
                this::restartGame,
                this::switchToMenuScene);

        // Khởi tạo HighscoreScene
        highscoreScene = new HighscoreScene(ctx, this::switchToMenuScene);

        // NameInputScene sẽ được tạo khi cần, khởi tạo là null
        nameInputScene = null;

        // Bắt đầu với Menu
        currentScene = menuScene;
        currentScene.start();
        soundManager.playMenuMusic();
    }

    private void setupWindow() {
        canvas = new Canvas(Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT);
        ctx = canvas.getGraphicsContext2D();

        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT);

        setupInputHandling(scene);

        primaryStage.setTitle("GAMEGr08 - Arkanoid Remake");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void setupInputHandling(Scene scene) {
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.S) {
                soundManager.playSound("hit"); // Test sound
            }
            if (e.getCode() == KeyCode.B) {
                soundManager.playBackgroundMusic(); // Test music
            }

            if (currentScene != null) {
                currentScene.handleInput(e);
            }
        });

        scene.setOnKeyReleased(e -> {
            if (currentScene != null) {
                currentScene.handleInput(e);
            }
        });
    }

    private void setupGameLoop() {
        lastUpdateTime = System.nanoTime();

        AnimationTimer gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double deltaTime = (now - lastUpdateTime) / 1_000_000_000.0;
                lastUpdateTime = now;

                update(deltaTime);
                render();
            }
        };
        gameLoop.start();
    }

    private void update(double deltaTime) {
        if (!isRunning || currentScene == null) return;
        currentScene.update(deltaTime);
    }

    private void render() {
        ctx.clearRect(0, 0, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT);
        if (currentScene != null) {
            currentScene.render();
        }
    }

    public void switchToGameScene() {
        if (currentScene != null) currentScene.cleanup();
        currentScene = gameScene;
        currentScene.start();
        System.out.println("🎮 Switched to Game Scene");
    }

    public void switchToMenuScene() {
        if (currentScene != null) currentScene.cleanup();
        currentScene = menuScene;
        currentScene.start();
        soundManager.onReturnToMenu();
        System.out.println("Switched to Menu Scene");
    }

    public void switchToHighscoreScene() {
        if (currentScene != null) currentScene.cleanup();
        currentScene = highscoreScene;
        currentScene.start();
        System.out.println("🏆 Switched to Highscore Scene");
    }

    public void switchToNameInputScene(int score, int level) {
        if (currentScene != null) currentScene.cleanup();
        nameInputScene = new NameInputScene(ctx, score, level, this::switchToHighscoreScene);
        currentScene = nameInputScene;
        currentScene.start();
        System.out.println("📝 Switched to Name Input Scene");
    }

    public void pauseGame() {
        if (currentScene == gameScene) {
            currentScene = pauseScene;
            currentScene.start();
            System.out.println("Game paused");
        }
    }

    public void resumeGame() {
        if (currentScene == pauseScene) {
            currentScene = gameScene;
            ((GameScene) gameScene).resumeFromPause(); // Thông báo resume
            System.out.println("Game resumed");
        }
    }

    public void restartGame() {
        System.out.println("Restarting game...");
        if (currentScene != null) currentScene.cleanup();

        // Tạo game scene mới
        gameScene = new GameScene(ctx, this);
        currentScene = gameScene;
        currentScene.start();

        System.out.println("Game restarted");
    }

    public void gameOver(int score, int level) {
        System.out.println("💀 Game Over - Score: " + score + ", Level: " + level);

        // Kiểm tra xem có phải highscore mới không
        if (saveManager.isHighscore(score)) {
            System.out.println("🎉 New Highscore Achieved!");
            switchToNameInputScene(score, level);
        } else {
            System.out.println("😊 No new highscore, returning to menu");
            switchToMenuScene();
        }
    }

    @Override
    public void stop() {
        isRunning = false;
        if (currentScene != null) currentScene.cleanup();
        if (soundManager != null) soundManager.cleanup();
        if (saveManager != null) saveManager.cleanup();
        System.out.println("GameEngine Stopped");
    }

    // Utility method để game scene có thể gọi game over
    public void notifyGameOver(int score, int level) {
        gameOver(score, level);
    }
}