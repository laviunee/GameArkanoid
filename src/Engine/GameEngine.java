package Engine;

import UI.*;
import Utils.Config;
import Utils.SaveManager;
import Utils.SoundManager;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
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
    private GameOverScene gameOverScene;
    private SoundManager soundManager;
    private HighscoreScene highscoreScene;
    private NameInputScene nameInputScene;
    private SaveManager saveManager;
    private LevelSelectScene levelSelectScene;

    //================= START ====================
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.isRunning = true;

        System.out.println("GameEngine Starting");

        setupWindow(); // tạo canvas, các scene, set stage
        initializeManagers(); // tạo soundmanager, savemanager
        initializeScenes(); // các scene
        setupGameLoop(); // AnimationTimer chạy update + render liên tục

        System.out.println("GameEngine Started Successfully!");
    }

    private void setupWindow() {
        canvas = new Canvas(Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT); // bảng vẽ
        ctx = canvas.getGraphicsContext2D(); // bút vẽ

        StackPane root = new StackPane(canvas); // để sau này xếp chồng hud,menu,... lên canvas
        Scene scene = new Scene(root, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT); // tạo scene chứa root
        // Stage (cửa sổ) → chứa Scene → chứa StackPane → chứa Canvas.

        setupInputHandling(scene); // method riêng để xử lý điều khiển của người chơi
        // JavaFx bắt events ở cấp scene nên cần truyền nó vào

        primaryStage.setTitle("ARKANOID");
        primaryStage.setScene(scene); // gắn scene vừa tạo vào stage
        // Stage = cửa sổ, Scene = nội dung bên trong
        // k gắn nội dung thì k có gì hiển thị

        primaryStage.setResizable(false);
        primaryStage.show(); // JavaFX runtime bắt đầu loop UI chính
        // bỏ -> k show -> k thấy (ctrinh vẫn chạy)
    }

    private void setupInputHandling(Scene scene) {
        scene.setOnKeyPressed(e -> { // Gắn sự kiện khi người chơi nhấn phím
            if (currentScene != null) {
                currentScene.handleInput(e); // để scene hiện tại xử lý
            }
        });

        scene.setOnKeyReleased(e -> { // sự kiện nhả phím
            if (currentScene != null) {
                currentScene.handleInput(e);
            }
        });
    }

    private void initializeManagers() {
        System.out.println("Initializing SoundManager");
        soundManager = SoundManager.getInstance();
        soundManager.initialize(); // initialize() phải được gọi trước khi phát sound (khởi tạo), k thì null

        System.out.println("Initializing SaveManager");
        saveManager = SaveManager.getInstance();
    }

    private void initializeScenes() { // khởi tạo all scenes
        System.out.println("Initializing Scenes");

        // MenuScene chỉ cần gọi callback, GameEngine sẽ chuyển sang GameScene
        menuScene = new MenuScene(ctx,
                this::switchToGameScene,
                this::switchToHighscoreScene,
                this::switchToLevelSelectScene
        );

        gameScene = new GameScene(ctx, this, () -> {
            // Callback khi game over
            gameOver(gameScene.getScore(), gameScene.getCurrentLevel());
        });

        // Khởi tạo PauseScene với các callback
        pauseScene = new PauseScene(ctx,
                this::resumeGame,
                this::restartGame,
                this::switchToMenuScene);

        // Khởi tạo HighscoreScene
        highscoreScene = new HighscoreScene(ctx, this::switchToMenuScene);

        // level select scene
        levelSelectScene = new LevelSelectScene(ctx, this);

        // NameInputScene, gameOverScene sẽ được tạo khi cần
        nameInputScene = null;
        gameOverScene = null;

        // Gán menu là scene đầu tiên
        currentScene = menuScene; // scene hiện tại đang hiển thị và nhận input
        currentScene.start();
        soundManager.playMenuMusic();
    }

    private void setupGameLoop() { // game bắt đầu chạy liên tục, sau khi window, scene, managers đã setup xong
        lastUpdateTime = System.nanoTime(); // lưu thời điểm hiện tại để làm mốc tính

        AnimationTimer gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) { // now: thời điểm hiện tại
                double deltaTime = (now - lastUpdateTime) / 1_000_000_000.0;
                lastUpdateTime = now;

                update(deltaTime); // cập nhật logic game
                render(); // vẽ lại
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

    //============SCENE SWITCHING ===================
    public void switchToMenuScene() {
        if (currentScene != null) currentScene.cleanup();
        currentScene = menuScene;
        currentScene.start();
        soundManager.onReturnToMenu();
        System.out.println("Switched to Menu Scene");
    }

    public void switchToGameScene() {
        if (currentScene != null) currentScene.cleanup(); // dọn dẹp
        currentScene = gameScene;
        currentScene.start();
        System.out.println("Switched to Game Scene");
    }

    public void switchToHighscoreScene() {
        if (currentScene != null) currentScene.cleanup();
        currentScene = highscoreScene;
        currentScene.start();
        System.out.println("Switched to Highscore Scene");
    }

    public void switchToLevelSelectScene() {
        if (currentScene != null) currentScene.cleanup();
        currentScene = levelSelectScene;
        currentScene.start();
        System.out.println("Switched to Level Select Scene");
    }

    public void switchToNameInputScene(int score, int level) {
        if (currentScene != null) currentScene.cleanup();
        // giờ mới tạo nameInputScene
        nameInputScene = new NameInputScene(ctx, score, level, this::switchToHighscoreScene);
        currentScene = nameInputScene;
        currentScene.start();
        System.out.println("Switched to Name Input Scene");
    }

    public void switchToGameOverScene(int score, int level) {
        if (currentScene != null) currentScene.cleanup();
        // giờ mới tạo GameOverScene
        gameOverScene = new GameOverScene(ctx, score, level,
                this::restartGame,
                this::switchToMenuScene
        );
        currentScene = gameOverScene;
        currentScene.start();
        System.out.println("Switched to Game Over Scene - Score: " + score + ", Level: " + level);
    }

    //================ GAME STATE METHODS ====================
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
            ((GameScene) gameScene).resumeFromPause();
            System.out.println("Game resumed");
        }
    }

    public void restartGame() {
        System.out.println("Restarting game");
        if (currentScene != null) currentScene.cleanup();

        // Tạo game scene mới
        gameScene = new GameScene(ctx, this, () -> {
            gameOver(gameScene.getScore(), gameScene.getCurrentLevel());
        });

        currentScene = gameScene;
        currentScene.start();

        System.out.println("Game restarted");
    }

    public void gameOver(int score, int level) {
        System.out.println("Game Over - Score: " + score + ", Level: " + level);

        // có phải highscore mới không
        if (saveManager.isHighscore(score)) {
            System.out.println("New Highscore Achieved!");
            switchToNameInputScene(score, level);
        } else {
            switchToGameOverScene(score, level);
        }
    }

    //=================================
    public void startGameAtLevel(int levelIndex) {
        switchToGameScene();
        if (gameScene != null) {
            gameScene.startGameAtLevel(levelIndex);
        }
    }

    // để game scene có thể gọi game over
    // Scene chỉ báo “game over” → engine quyết định scene tiếp theo
    public void notifyGameOver(int score, int level) {
        gameOver(score, level);
    }

    //================ STOP ========================
    @Override
    public void stop() {
        isRunning = false;
        if (currentScene != null) currentScene.cleanup();
        if (soundManager != null) soundManager.cleanup();
        if (saveManager != null) saveManager.cleanup();
        System.out.println("GameEngine Stopped");
    }
}

// GameEngine được khởi tạo (start) → window, managers, scenes.
//Game loop chạy → update + render.
//Người chơi điều khiển → scene chuyển → game state thay đổi (pause/restart/gameOver).
//Khi cần API từ bên ngoài (chọn level, kết thúc game) → gọi startGameAtLevel, notifyGameOver.
//Cuối cùng dừng game (stop).