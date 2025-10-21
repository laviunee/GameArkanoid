package Engine;

import Utils.Config;
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

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.isRunning = true;

        System.out.println("GameEngine Starting...");

        setupWindow();
        setupGameLoop();

        System.out.println("GameEngine Started Successfully!");
    }

    private void setupWindow() {
        canvas = new Canvas(Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT);
        ctx = canvas.getGraphicsContext2D();

        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT);

        primaryStage.setTitle("Arkanoid Game");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void setupGameLoop() {
        lastUpdateTime = System.nanoTime();

        AnimationTimer gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double deltaTime = (now - lastUpdateTime) / 1_000_000_000.0;
                lastUpdateTime = now;

                render();
            }
        };
        gameLoop.start();
    }

    private void render() {
        ctx.clearRect(0, 0, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT);
    }

    @Override
    public void stop() {}
}