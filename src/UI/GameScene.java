package UI;

import Engine.CollisionManager;
import Engine.GameEngine;
import Engine.LevelManager;
import Engine.SceneManager;
import Entities.Ball;
import Entities.Bricks.StrongBrick;
import Entities.Bricks.NormalBrick;
import Entities.Paddle;
import Entities.Bricks.Brick;
import Entities.Power.PowerUp;
import Entities.Power.PowerFactory;
import Utils.Config;
import Utils.SaveManager;
import Utils.SoundManager;
import Utils.SpriteLoader;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;

public class GameScene extends SceneManager {
    private GraphicsContext ctx;
    private Canvas canvas;
    private SoundManager soundManager;
    private SpriteLoader spriteLoader;

    // Game objects
    private Paddle paddle;
    private List<Ball> balls;
    private LevelManager levelManager;
    private List<PowerUp> powerUps;
    private int score;
    private int lives;
    private boolean isRunning;

    // Pause scene và trạng thái pause
    private GameEngine gameEngine;
    private Runnable onGameOver;
    private boolean isPaused = false;

    // Mouse control
    private boolean mouseControlEnabled = false;
    private double mouseX = 0;

    // Sprite references
    private Image gameBackground;
    private Image paddleImage;
    private Image ballImage;
    private Image brickNormalImage;
    private Image brickStrongImage;
    private Image brickStrongCrackedImage;

    // Power-up sprites
    private Image powerupExpandImage;
    private Image powerupFastballImage;
    private Image powerupMultiballImage;
    private Image powerupExtraliveImage;
    private Image powerupPierceballImage;

    public GameScene(GraphicsContext ctx, GameEngine gameEngine, Runnable onGameOver) {
        this.ctx = ctx;
        this.canvas = ctx.getCanvas();
        this.gameEngine = gameEngine;
        this.onGameOver = onGameOver;
        this.soundManager = SoundManager.getInstance();
        this.spriteLoader = SpriteLoader.getInstance();
        this.isRunning = true;
        this.levelManager = new LevelManager();

        loadGameSprites();
        setupMouseControls();
    }

    // === PUBLIC METHODS ===
    public void startGameAtLevel(int levelIndex) {
        System.out.println("🎮 Starting game at level: " + (levelIndex + 1));

        // Reset game state
        score = 0;
        lives = 3;
        isRunning = true;
        isPaused = false;

        // Load level cụ thể
        levelManager.loadLevel(levelIndex);

        // Reset paddle và ball
        double paddleX = Config.SCREEN_WIDTH / 2;
        double paddleY = Config.SCREEN_HEIGHT - Config.PADDLE_OFFSET_Y;
        paddle = new Paddle(paddleX, paddleY); // TẠO PADDLE MỚI

        balls = new ArrayList<>();
        spawnBall();
        PowerFactory.setBalls(balls);
        PowerFactory.setGameScene(this);

        powerUps = new ArrayList<>();

        soundManager.onGameStart();
    }

    public void resumeFromPause() {
        isPaused = false;
        System.out.println("GameScene resumed from pause");
    }

    // === SCENE MANAGER METHODS ===
    @Override
    public void start() {
        System.out.println("🎮 GameScene: Starting game...");

        soundManager.stopAllSounds();

        // Khởi tạo game objects
        double paddleX = Config.SCREEN_WIDTH / 2;
        double paddleY = Config.SCREEN_HEIGHT - Config.PADDLE_OFFSET_Y;
        paddle = new Paddle(paddleX, paddleY);

        balls = new ArrayList<>();
        spawnBall();
        PowerFactory.setBalls(balls);
        PowerFactory.setGameScene(this);

        powerUps = new ArrayList<>();
        score = 0;
        lives = 3;

        // Load level đầu tiên
        levelManager.loadLevel(0);

        isRunning = true;
        isPaused = false;
        mouseControlEnabled = false;

        soundManager.onGameStart();
        System.out.println("🎵 GameScene: Game start signal sent to SoundManager");
    }

    @Override
    public void update(double deltaTime) {
        if (!isRunning || isPaused) return;

        // Update paddle
        if (mouseControlEnabled) {
            paddle.moveToMouse(mouseX);
        } else {
            paddle.update(deltaTime);
        }

        // Update balls
        for (Ball ball : balls) {
            if (ball.isOnPaddle()) {
                ball.followPaddle(paddle.getPosition().x, paddle.getPosition().y, paddle.getHeight());
            }
            if (ball.isActive()) {
                ball.update(deltaTime);
            }
        }

        // Update power-ups
        updatePowerUps(deltaTime);

        // Check collisions
        checkCollisions();
        checkPowerUpCollisions();

        // Clean up collected power-ups
        powerUps.removeIf(PowerUp::isCollected);

        // Kiểm tra hoàn thành level
        if (levelManager.isLevelCompleted()) {
            handleLevelComplete();
        }

        // Kiểm tra game over
        if (lives <= 0) {
            System.out.println("GAME OVER!");
            isRunning = false;
            handleGameOver();
        }
    }


    @Override
    public void render() {
        drawBackground();
        drawHUDBackground();
        drawBricks();
        drawPowerUps();
        drawPaddle();
        drawBalls();
        drawUI();
    }

    @Override
    public void handleInput(KeyEvent event) {
        if (event.getEventType() == KeyEvent.KEY_PRESSED) {
            switch (event.getCode()) {
                case LEFT, A -> {
                    if (!mouseControlEnabled) movePaddleLeft();
                }
                case RIGHT, D -> {
                    if (!mouseControlEnabled) movePaddleRight();
                }
                case SPACE -> launchBall();
                case M -> toggleSound();
                case C -> toggleMouseControl();
                case P, ESCAPE -> {
                    if (!isPaused) {
                        gameEngine.pauseGame();
                        isPaused = true;
                    }
                }
            }
        }

        if (event.getEventType() == KeyEvent.KEY_RELEASED) {
            switch (event.getCode()) {
                case LEFT, RIGHT, A, D -> {
                    if (!mouseControlEnabled) stopPaddle();
                }
            }
        }
    }

    @Override
    public void cleanup() {
        System.out.println("Cleaning up Game Scene...");
        if (balls != null) balls.clear();
        if (powerUps != null) powerUps.clear();
        isPaused = false;

        // Remove mouse event handlers
        canvas.setOnMouseMoved(null);
        canvas.setOnMouseDragged(null);
        canvas.setOnMouseClicked(null);
        canvas.setOnMouseExited(null);
    }

    // === GAME LOGIC METHODS ===
    private void spawnBall() {
        balls.clear();

        double ballX = paddle.getPosition().x;
        double ballY = paddle.getPosition().y - paddle.getHeight() - Config.BALL_SIZE;

        Ball newBall = new Ball(ballX, ballY);
        newBall.setActive(false);
        balls.add(newBall);

        System.out.println("NEW ball on paddle at (" + ballX + ", " + ballY + ")");
    }

    private void updatePowerUps(double deltaTime) {
        for (PowerUp powerUp : powerUps) {
            powerUp.update(deltaTime);
        }
    }

    private void checkCollisions() {
        List<Ball> ballsToRemove = new ArrayList<>();

        for (Ball ball : balls) {
            if (!ball.isActive()) continue;

            // Check paddle collision
            if (CollisionManager.checkBallPaddleCollision(ball, paddle)) {
                soundManager.playSound("paddle_hit");
            }

            // Check brick collisions
            List<Brick> bricksToRemove = new ArrayList<>();
            for (Brick brick : levelManager.getBricks()) {
                if (CollisionManager.checkBallBrickCollision(ball, brick)) {
                    brick.onHit();
                    score += brick.getScoreValue();

                    // Play sound
                    if (!ball.isPierce()) {
                        soundManager.playSound("hit");
                    } else {
                        soundManager.playSound("powerup");
                    }

                    // Spawn power-up if brick is destroyed
                    if (brick.isToBeRemoved()) {
                        bricksToRemove.add(brick);
                        spawnPowerUp(brick.getPosition().x, brick.getPosition().y);
                    }

                    // Handle ball bounce (if not piercing)
                    if (!ball.isPierce()) {
                        // Xử lý đổi hướng bóng - giữ nguyên logic cũ
                    }
                    break; // Chỉ xử lý 1 brick mỗi frame
                }
            }

            // Remove destroyed bricks
            for (Brick brick : bricksToRemove) {
                levelManager.removeBrick(brick);
            }

            // Check wall collisions
            CollisionManager.checkWallCollisions(ball);

            // Check ball lost
            if (ball.getPosition().y + ball.getRadius() >= Config.SCREEN_HEIGHT) {
                System.out.println("Ball lost at bottom!");
                ballsToRemove.add(ball);
            }
        }

        // Remove lost balls
        balls.removeAll(ballsToRemove);

        // Handle ball loss
        if (balls.isEmpty()) {
            lives--;
            System.out.println("All balls lost! Lives remaining: " + lives);

            if (lives > 0) {
                soundManager.onLoseLife();
                System.out.println("Respawning NEW ball on paddle");
                spawnBall();
            } else {
                soundManager.onGameOver();
                System.out.println("GAME OVER!");
                isRunning = false;
                handleGameOver();
            }
        }
    }

    private void checkPowerUpCollisions() {
        List<PowerUp> collectedPowerUps = new ArrayList<>();

        for (PowerUp powerUp : powerUps) {
            if (powerUp.isCollected()) continue;

            double powerUpX = powerUp.getPosition().x;
            double powerUpY = powerUp.getPosition().y;
            double powerUpSize = 40;

            double paddleX = paddle.getPosition().x;
            double paddleY = paddle.getPosition().y;
            double paddleWidth = paddle.getWidth();
            double paddleHeight = paddle.getHeight();

            // Simple rectangle collision check
            if (powerUpX >= paddleX - paddleWidth/2 &&
                    powerUpX <= paddleX + paddleWidth/2 &&
                    powerUpY >= paddleY - paddleHeight/2 &&
                    powerUpY <= paddleY + paddleHeight/2) {

                powerUp.collect();
                collectedPowerUps.add(powerUp);

                // Apply power-up effect
                if (!balls.isEmpty()) {
                    powerUp.applyEffect(paddle, balls.get(0));
                }

                soundManager.playSound("powerup");
                System.out.println("Collected: " + powerUp.getName());
            }
        }

        powerUps.removeAll(collectedPowerUps);
    }

    private void spawnPowerUp(double x, double y) {
        if (Math.random() < 0.3) { // 30% chance to spawn power-up
            PowerUp powerUp = PowerFactory.createRandomPowerUp(x, y);
            powerUp.start();
            powerUps.add(powerUp);
            System.out.println("Power-up spawned: " + powerUp.getName());
        }
    }

    // === LEVEL MANAGEMENT ===
    private void handleLevelComplete() {
        System.out.println("Level " + levelManager.getCurrentLevel() + " completed!");

        // Lưu tiến trình
        SaveManager saveManager = SaveManager.getInstance();
        saveManager.completeLevel(levelManager.getCurrentLevel(), score);

        if (levelManager.hasNextLevel()) {
            // Chuyển level tiếp theo
            levelManager.nextLevel();
            resetBallAndPaddle();
            showLevelCompleteMessage();
            soundManager.onLevelComplete();
        } else {
            // Hoàn thành tất cả level
            System.out.println("🎉 ALL LEVELS COMPLETED! YOU WIN!");
            soundManager.onGameWin();
            checkHighscore();
        }
    }

    private void resetBallAndPaddle() {
        // Reset paddle về vị trí giữa
        paddle.getPosition().x = Config.SCREEN_WIDTH / 2;

        // Reset balls
        balls.clear();
        spawnBall();

        // Clear power-ups
        powerUps.clear();

        System.out.println("Ball and paddle reset for new level");
    }

    private void showLevelCompleteMessage() {
        System.out.println("LEVEL " + levelManager.getCurrentLevel() + " START!");
        // Có thể thêm hiệu ứng visual ở đây
    }

    private void checkHighscore() {
        SaveManager saveManager = SaveManager.getInstance();
        if (saveManager.isHighscore(score)) {
            gameEngine.switchToNameInputScene(score, levelManager.getCurrentLevel());
        } else {
            handleGameOver();
        }
    }

    // === INPUT HANDLERS ===
    private void setupMouseControls() {
        canvas.setOnMouseMoved(this::handleMouseMoved);
        canvas.setOnMouseDragged(this::handleMouseDragged);
        canvas.setOnMouseClicked(this::handleMouseClicked);
        canvas.setOnMouseExited(this::handleMouseExited);
    }

    private void handleMouseMoved(MouseEvent event) {
        if (!isRunning || isPaused) return;
        mouseX = event.getX();
        if (mouseControlEnabled) {
            paddle.moveToMouse(mouseX);
        }
    }

    private void handleMouseDragged(MouseEvent event) {
        if (!isRunning || isPaused) return;
        mouseX = event.getX();
        paddle.moveToMouse(mouseX);
    }

    private void handleMouseClicked(MouseEvent event) {
        if (!isRunning || isPaused) return;

        if (event.isPrimaryButtonDown()) {
            launchBall();
        }

        if (event.isSecondaryButtonDown()) {
            toggleMouseControl();
        }
    }

    private void handleMouseExited(MouseEvent event) {
        if (!isRunning || isPaused) return;
        paddle.stop();
    }

    public void movePaddleLeft() {
        if (!mouseControlEnabled) paddle.moveLeft();
    }

    public void movePaddleRight() {
        if (!mouseControlEnabled) paddle.moveRight();
    }

    public void stopPaddle() {
        if (!mouseControlEnabled) paddle.stop();
    }

    public void launchBall() {
        if (!balls.isEmpty() && balls.get(0).isOnPaddle()) {
            Ball ball = balls.get(0);
            ball.setActive(true);
            ball.setVelocity(0, -Config.BALL_SPEED);
            System.out.println("Ball launched from paddle!");
        }
    }

    private void toggleMouseControl() {
        mouseControlEnabled = !mouseControlEnabled;
        System.out.println("Mouse control: " + (mouseControlEnabled ? "ENABLED" : "DISABLED"));

        if (!mouseControlEnabled) {
            paddle.stop();
        }
    }

    private void toggleSound() {
        boolean currentState = soundManager.isSoundEnabled();
        soundManager.setSoundEnabled(!currentState);
        System.out.println("Sound toggled from " + currentState + " to " + soundManager.isSoundEnabled());
    }

    // === RENDER METHODS ===
    private void drawBackground() {
        // Sử dụng background từ LevelManager
        if (levelManager.hasCustomBackground()) {
            javafx.scene.image.Image levelBackground = levelManager.getCurrentBackground();
            if (levelBackground != null && !levelBackground.isError()) {
                ctx.drawImage(levelBackground, 0, 0, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT);
                return;
            }
        }

        // Fallback: sử dụng màu nền từ LevelManager
        Color backgroundColor = levelManager.getCurrentBackgroundColor();
        if (backgroundColor != null) {
            ctx.setFill(backgroundColor);
            ctx.fillRect(0, 0, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT);
        } else {
            // Fallback cũ
            ctx.setFill(Color.BLACK);
            ctx.fillRect(0, 0, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT);
        }

        // Vẽ border và các phần khác
        ctx.setFill(Color.DARKBLUE);
        ctx.fillRect(Config.INSET, Config.UPPER_INSET,
                Config.SCREEN_WIDTH - 2 * Config.INSET,
                Config.SCREEN_HEIGHT - Config.UPPER_INSET - Config.INSET);

        ctx.setFill(Color.GRAY);
        ctx.fillRect(0, Config.UPPER_INSET, Config.INSET, Config.SCREEN_HEIGHT - Config.UPPER_INSET);
        ctx.fillRect(Config.SCREEN_WIDTH - Config.INSET, Config.UPPER_INSET, Config.INSET, Config.SCREEN_HEIGHT - Config.UPPER_INSET);
    }

    private void drawHUDBackground() {
        double hudHeight = 80;
        int level = levelManager.getCurrentLevel(); // 1-based
        Color startColor, midColor, endColor, borderColor, shadowColor;

        // Chọn màu theo level
        switch (level) {
            case 1, 3, 4 -> {
                startColor = Color.rgb(0, 20, 50, 0.7);
                midColor = Color.rgb(0, 80, 150, 0.85);
                endColor = Color.rgb(0, 20, 50, 0.7);
                borderColor = Color.rgb(200, 220, 255, 0.15);
                shadowColor = Color.rgb(0, 0, 0, 0.2);
            }
            case 2 -> {
                startColor = Color.rgb(10, 10, 30, 0.7);
                midColor = Color.rgb(50, 0, 80, 0.85);
                endColor = Color.rgb(10, 10, 30, 0.7);
                borderColor = Color.rgb(255, 255, 255, 0.15);
                shadowColor = Color.rgb(0, 0, 0, 0.2);
            }
            case 5 -> { // Final / Magenta
                startColor = Color.rgb(50, 0, 50, 0.7);
                midColor = Color.rgb(200, 0, 150, 0.85);
                endColor = Color.rgb(50, 0, 50, 0.7);
                borderColor = Color.rgb(255, 150, 255, 0.15);
                shadowColor = Color.rgb(0, 0, 0, 0.2);
            }
            default -> { // fallback
                startColor = Color.rgb(0, 0, 0, 0.7);
                midColor = Color.rgb(30, 30, 50, 0.85);
                endColor = Color.rgb(0, 0, 0, 0.7);
                borderColor = Color.rgb(255, 255, 255, 0.15);
                shadowColor = Color.rgb(0, 0, 0, 0.2);
            }
        }

        // Gradient thanh HUD
        LinearGradient gradient = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, startColor),
                new Stop(0.5, midColor),
                new Stop(1, endColor)
        );

        ctx.setFill(gradient);
        ctx.fillRoundRect(0, 0, Config.SCREEN_WIDTH, hudHeight, 15, 15);

        // Viền thanh HUD
        ctx.setStroke(borderColor);
        ctx.setLineWidth(2);
        ctx.strokeRoundRect(1, 1, Config.SCREEN_WIDTH - 2, hudHeight - 2, 15, 15);

        // Shadow nhẹ ở đáy
        ctx.setStroke(shadowColor);
        ctx.setLineWidth(3);
        ctx.strokeLine(0, hudHeight - 1, Config.SCREEN_WIDTH, hudHeight - 1);
    }




    private void drawBricks() {
        for (Brick brick : levelManager.getBricks()) {
            double x = brick.getPosition().x;
            double y = brick.getPosition().y;
            double width = brick.getWidth();
            double height = brick.getHeight();

            Image brickImage = getImage(brick);

            if (brickImage != null) {
                ctx.drawImage(brickImage, x, y, width, height);
            } else {
                // Fallback: draw with colors
                if (brick instanceof StrongBrick) {
                    StrongBrick strongBrick = (StrongBrick) brick;
                    ctx.setFill(strongBrick.getHitPoints() > 1 ? Color.ORANGE : Color.RED); // SỬA ĐIỀU KIỆN
                } else {
                    ctx.setFill(Color.LIME);
                }
                ctx.fillRect(x, y, width, height);
                ctx.setStroke(Color.WHITE);
                ctx.strokeRect(x, y, width, height);
            }
        }
    }

    private Image getImage(Brick brick) {
        Image brickImage = null;

        if (brick instanceof StrongBrick) {
            StrongBrick strongBrick = (StrongBrick) brick;
            if (strongBrick.getHitPoints() > 1 && brickStrongImage != null) { // SỬA: > 1 thay vì == 2
                brickImage = brickStrongImage;
            } else if (brickStrongCrackedImage != null) {
                brickImage = brickStrongCrackedImage;
            }
        } else if (brick instanceof NormalBrick && brickNormalImage != null) { // SỬA: instanceof NormalBrick
            brickImage = brickNormalImage;
        }
        return brickImage;
    }

    private void drawPowerUps() {
        double size = 25;

        for (PowerUp powerUp : powerUps) {
            if (powerUp.isCollected()) continue;

            double x = powerUp.getPosition().x - size / 2;
            double y = powerUp.getPosition().y - size / 2;

            Image powerupImage = null;

            // Determine power-up image based on type
            if (powerUp instanceof Entities.Power.ExpandPaddle) {
                powerupImage = powerupExpandImage;
            } else if (powerUp instanceof Entities.Power.FastBall) {
                powerupImage = powerupFastballImage;
            } else if (powerUp instanceof Entities.Power.PowerUpMultiBall) {
                powerupImage = powerupMultiballImage;
            } else if (powerUp instanceof Entities.Power.PowerUpExtraLive) {
                powerupImage = powerupExtraliveImage;
            } else if (powerUp instanceof Entities.Power.PowerUpPierceBall) {
                powerupImage = powerupPierceballImage;
            }

            if (powerupImage != null) {
                ctx.drawImage(powerupImage, x, y, size, size);
            } else {
                // Fallback: draw colored square
                ctx.setFill(Color.MAGENTA);
                ctx.fillRect(x, y, size, size);
                ctx.setStroke(Color.BLACK);
                ctx.strokeRect(x, y, size, size);
            }
        }
    }

    private void drawPaddle() {
        double x = paddle.getPosition().x - paddle.getWidth()/2;
        double y = paddle.getPosition().y - paddle.getHeight()/2;

        if (paddleImage != null) {
            ctx.drawImage(paddleImage, x, y, paddle.getWidth(), paddle.getHeight());
        } else {
            // Fallback: draw colored rectangle
            ctx.setFill(Color.LIGHTGRAY);
            ctx.fillRect(x, y, paddle.getWidth(), paddle.getHeight());
            ctx.setStroke(Color.WHITE);
            ctx.strokeRect(x, y, paddle.getWidth(), paddle.getHeight());
        }
    }

    private void drawBalls() {
        for (Ball ball : balls) {
            double x = ball.getPosition().x - ball.getRadius();
            double y = ball.getPosition().y - ball.getRadius();
            double diameter = ball.getRadius() * 2;

            if (ballImage != null) {
                ctx.drawImage(ballImage, x, y, diameter, diameter);
            } else {
                // Fallback: draw colored circle
                ctx.setFill(ball.getColor());
                ctx.fillOval(x, y, diameter, diameter);
                ctx.setStroke(Color.WHITE);
                ctx.strokeOval(x, y, diameter, diameter);
            }
        }
    }

    private void drawUI() {
        ctx.setFill(Color.WHITE);
        ctx.setFont(Font.font(16));

        // Score và Lives
        ctx.fillText("SCORE: " + score, 20, 30);
        ctx.fillText("LIVES: " + lives, Config.SCREEN_WIDTH - 80, 30);

        // Level info
        String levelText = "LEVEL: " + levelManager.getCurrentLevel() + "/" + levelManager.getTotalLevels();
        ctx.fillText(levelText, Config.SCREEN_WIDTH/2 - 40, 30);

        // Bricks remaining
        String bricksText = "BRICKS: " + levelManager.getRemainingBricks() + "/" + levelManager.getTotalBricks();
        ctx.fillText(bricksText, Config.SCREEN_WIDTH/2 - 40, 50);

        // Sound status
        String soundStatus = soundManager.isSoundEnabled() ? "ON" : "OFF";
        Color soundColor = soundManager.isSoundEnabled() ? Color.GREEN : Color.RED;
        ctx.setFill(soundColor);
        ctx.fillText("SOUND: " + soundStatus, Config.SCREEN_WIDTH - 120, 50);
        ctx.setFill(Color.WHITE);
        ctx.fillText("Press M to toggle", Config.SCREEN_WIDTH - 150, 65);

        // Mouse control status
        String mouseControlStatus = mouseControlEnabled ? "MOUSE CONTROL: ON" : "MOUSE CONTROL: OFF";
        Color mouseColor = mouseControlEnabled ? Color.GREEN : Color.YELLOW;
        ctx.setFill(mouseColor);
        ctx.fillText(mouseControlStatus, Config.SCREEN_WIDTH/2 - 70, 70);

        // Game over message
        if (lives <= 0) {
            ctx.setFill(Color.RED);
            ctx.setFont(Font.font(32));
            ctx.fillText("GAME OVER", Config.SCREEN_WIDTH/2 - 80, Config.SCREEN_HEIGHT/2);
        }

        // Level complete message
        if (levelManager.isLevelCompleted() && levelManager.hasNextLevel()) {
            ctx.setFill(Color.GREEN);
            ctx.setFont(Font.font(24));
            ctx.fillText("LEVEL COMPLETE!", Config.SCREEN_WIDTH/2 - 80, Config.SCREEN_HEIGHT/2 - 30);
            ctx.fillText("GET READY FOR NEXT LEVEL", Config.SCREEN_WIDTH/2 - 120, Config.SCREEN_HEIGHT/2 + 10);
        }
    }

    // === EVENT HANDLERS ===
    private void handleGameOver() {
        System.out.println("🎮 GameScene: Game Over detected");
        soundManager.onGameOver();

        if (onGameOver != null) {
            onGameOver.run();
        } else {
            System.err.println("❌ GameScene: onGameOver callback is null!");
            gameEngine.switchToMenuScene();
        }
    }

    private void loadGameSprites() {
        try {
            gameBackground = spriteLoader.loadSprite("/images/backgrounds/game_bg.png",
                    Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT, true, false);
            paddleImage = spriteLoader.loadSprite("/images/paddle.png");
            ballImage = spriteLoader.loadSprite("/images/ball.png");
            brickNormalImage = spriteLoader.loadSprite("/images/bricks/normal.png");
            brickStrongImage = spriteLoader.loadSprite("/images/bricks/strong.png");
            brickStrongCrackedImage = spriteLoader.loadSprite("/images/bricks/strong_cracked.png");

            powerupExpandImage = spriteLoader.loadSprite("/images/powerup/expand.png");
            powerupFastballImage = spriteLoader.loadSprite("/images/powerup/fastball.png");
            powerupMultiballImage = spriteLoader.loadSprite("/images/powerup/multiball.png");
            powerupExtraliveImage = spriteLoader.loadSprite("/images/powerup/extralive.png");
            powerupPierceballImage = spriteLoader.loadSprite("/images/powerup/pierceball.png");

            System.out.println("Game sprites loaded successfully");

        } catch (Exception e) {
            System.err.println("Error loading game sprites: " + e.getMessage());
        }
    }

    // === GETTERS ===
    public void addLive() {
        lives++;
        System.out.println("Live added! Lives = " + lives);
    }

    public boolean isRunning() { return isRunning; }
    public int getScore() { return score; }
    public int getLives() { return lives; }
    public boolean isPaused() { return isPaused; }
    public boolean isMouseControlEnabled() { return mouseControlEnabled; }
    public int getCurrentLevel() { return levelManager.getCurrentLevel(); }
}