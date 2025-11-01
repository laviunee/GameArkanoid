package UI;

import Engine.CollisionManager;
import Engine.GameEngine;
import Engine.SceneManager;
import Entities.Ball;
import Entities.Paddle;
import Entities.Bricks.Brick;
import Entities.Bricks.NormalBrick;
import Entities.Bricks.StrongBrick;
import Entities.Power.PowerUp;
import Entities.Power.PowerFactory;
import Utils.Config;
import Utils.SoundManager;
import Utils.SpriteLoader;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class GameScene extends SceneManager {
    private GraphicsContext ctx;
    private SoundManager soundManager;
    private SpriteLoader spriteLoader;

    // Game objects
    private Paddle paddle;
    private List<Ball> balls;
    private List<Brick> bricks;
    private List<PowerUp> powerUps;
    private int score;
    private int lives;
    private boolean isRunning;

    // Pause scene và trạng thái pause
    private GameEngine gameEngine;
    private PauseScene pauseScene;
    private boolean isPaused = false;

    // Sprite references
    private javafx.scene.image.Image gameBackground;
    private javafx.scene.image.Image paddleImage;
    private javafx.scene.image.Image ballImage;
    private javafx.scene.image.Image brickNormalImage;
    private javafx.scene.image.Image brickStrongImage;
    private javafx.scene.image.Image brickStrongCrackedImage;

    // THÊM SPRITE CHO POWER-UP
    private javafx.scene.image.Image powerupExpandImage;
    private javafx.scene.image.Image powerupFastballImage;
    private javafx.scene.image.Image powerupMultiballImage;

    public GameScene(GraphicsContext ctx, GameEngine gameEngine) {
        this.ctx = ctx;
        this.gameEngine = gameEngine;
        this.soundManager = SoundManager.getInstance();
        this.spriteLoader = SpriteLoader.getInstance();
        this.isRunning = true;
        this.pauseScene = null; //PauseScene sẽ được GameEngine quản lý
        loadGameSprites();
    }

    // THÊM: Method để resume game từ pause
    private void resumeGame() {
        isPaused = false;
        pauseScene.cleanup();
        System.out.println("Game resumed");
    }

    // THÊM: Method để return to main menu (cần implement)
    private void returnToMainMenu() {
        System.out.println("Returning to main menu");
        // TODO: Implement return to main menu logic
        // Ví dụ: sceneManager.switchToScene("MainMenu");
    }

    private void loadGameSprites() {
        try {
            // Load game background
            gameBackground = spriteLoader.loadSprite("/images/backgrounds/game_bg.png",
                    Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT, true, false);

            // Load paddle
            paddleImage = spriteLoader.loadSprite("/images/paddle.png");

            // Load ball
            ballImage = spriteLoader.loadSprite("/images/ball.png");

            // Load bricks
            brickNormalImage = spriteLoader.loadSprite("/images/bricks/normal.png");
            brickStrongImage = spriteLoader.loadSprite("/images/bricks/strong.png");
            brickStrongCrackedImage = spriteLoader.loadSprite("/images/bricks/strong_cracked.png");

            // THÊM LOAD POWER-UP SPRITES
            powerupExpandImage = spriteLoader.loadSprite("/images/powerup/expand.png");
            powerupFastballImage = spriteLoader.loadSprite("/images/powerup/fastball.png");
            powerupMultiballImage = spriteLoader.loadSprite("/images/powerup/multiball.png");

            System.out.println("Game sprites loaded successfully");

        } catch (Exception e) {
            System.err.println("Error loading game sprites: " + e.getMessage());
        }
    }

    @Override
    public void start() {
        double paddleX = Config.SCREEN_WIDTH / 2;
        double paddleY = Config.SCREEN_HEIGHT - Config.PADDLE_OFFSET_Y;
        paddle = new Paddle(paddleX, paddleY);

        balls = new ArrayList<>();
        spawnBall();

        bricks = new ArrayList<>();
        setupBricks();

        powerUps = new ArrayList<>();
        score = 0;
        lives = 3;

        isRunning = true;
        isPaused = false; // Đảm bảo không pause khi bắt đầu
    }

    @Override
    public void update(double deltaTime) {
        // Không update nếu game đang pause
        if (!isRunning || isPaused) return;

        paddle.update(deltaTime);

        for (Ball ball : balls) {
            if (ball.isOnPaddle()) {
                ball.followPaddle(paddle.getPosition().x, paddle.getPosition().y, paddle.getHeight());
            }
            if (ball.isActive()) {
                ball.update(deltaTime);
            }
        }

        updatePowerUps(deltaTime);
        checkCollisions();
        checkPowerUpCollisions();

        bricks.removeIf(brick -> brick.isToBeRemoved());
        powerUps.removeIf(powerUp -> powerUp.isCollected());

        if (bricks.isEmpty()) {
            System.out.println("YOU WIN!");
            isRunning = false;
        }

        if (lives <= 0) {
            System.out.println("GAME OVER!");
            isRunning = false;
        }
    }

    @Override
    public void render() {
        drawBackground();
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
                case LEFT, A -> movePaddleLeft();
                case RIGHT, D -> movePaddleRight();
                case SPACE -> launchBall();
                case M -> {
                    boolean currentState = soundManager.isSoundEnabled();
                    soundManager.setSoundEnabled(!currentState);
                    System.out.println("Sound toggled from " + currentState + " to " + soundManager.isSoundEnabled());
                }
                // Gọi GameEngine để pause
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
                case LEFT, RIGHT, A, D -> stopPaddle();
            }
        }
    }

    // Method để toggle pause
    private void togglePause() {
        if (!isPaused) {
            // Pause game
            isPaused = true;
            pauseScene.start();
            System.out.println("Game paused");
        } else {
            // Resume game
            resumeGame();
        }
    }

    // Method để resume từ GameEngine
    public void resumeFromPause() {
        isPaused = false;
        System.out.println("GameScene resumed from pause");
    }

    @Override
    public void cleanup() {
        System.out.println("Cleaning up Game Scene...");
        balls.clear();
        bricks.clear();
        powerUps.clear();
        isPaused = false; // Đảm bảo reset trạng thái pause
    }

    // === CÁC METHOD GAME LOGIC ===
    private void spawnBall() {
        balls.clear();

        double ballX = paddle.getPosition().x;
        double ballY = paddle.getPosition().y - paddle.getHeight() - Config.BALL_SIZE;

        Ball newBall = new Ball(ballX, ballY);
        newBall.setActive(false);
        balls.add(newBall);

        System.out.println("NEW ball on paddle at (" + ballX + ", " + ballY + ")");
    }

    private void setupBricks() {
        int rows = 4;
        int cols = 12;

        double horizontalGap = 8;
        double verticalGap = 8;

        // TÍNH TOÁN TRONG VÙNG CHƠI GIỮA 2 WALL
        double playAreaWidth = Config.SCREEN_WIDTH - 2 * Config.INSET;
        double totalWidth = cols * Config.BLOCK_WIDTH + (cols - 1) * horizontalGap;

        // CĂN GIỮA TRONG VÙNG CHƠI
        double startX = Config.INSET + (playAreaWidth - totalWidth) / 2;
        double startY = Config.UPPER_INSET + 30;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                double x = startX + col * (Config.BLOCK_WIDTH + horizontalGap);
                double y = startY + row * (Config.BLOCK_HEIGHT + verticalGap);

                Brick brick;
                if (row == 0) {
                    brick = new StrongBrick(x, y);
                } else {
                    brick = new NormalBrick(x, y);
                }

                bricks.add(brick);
            }
        }

        System.out.println("Created " + bricks.size() + " bricks (centered in play area)");
    }

    private void updatePowerUps(double deltaTime) {
        for (PowerUp powerUp : powerUps) {
            powerUp.update(deltaTime);
        }
    }

    private void checkCollisions() {
        List<Ball> ballsToRemove = new ArrayList<>();
        boolean ballLost = false;

        for (Ball ball : balls) {
            if (!ball.isActive()) continue;

            if (CollisionManager.checkBallPaddleCollision(ball, paddle)) {
                soundManager.playSound("hit");
            }

            for (Brick brick : bricks) {
                if (CollisionManager.checkBallBrickCollision(ball, brick)) {
                    brick.onHit();
                    score += brick.getScoreValue();

                    if (brick.isToBeRemoved()) {
                        soundManager.playSound("break");
                        spawnPowerUp(brick.getPosition().x, brick.getPosition().y);
                    } else {
                        soundManager.playSound("hit");
                    }
                    break;
                }
            }

            CollisionManager.checkWallCollisions(ball);

            if (ball.getPosition().y + ball.getRadius() >= Config.SCREEN_HEIGHT) {
                System.out.println("Ball lost at bottom!");
                ballsToRemove.add(ball);
                ballLost = true;
                lives--;
                soundManager.playSound("lose");
                System.out.println("Lives remaining: " + lives);
            }
        }

        balls.removeAll(ballsToRemove);

        if (ballLost && lives > 0 && balls.isEmpty()) {
            System.out.println("Respawning NEW ball on paddle");
            spawnBall();
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

            if (powerUpX >= paddleX - paddleWidth/2 &&
                    powerUpX <= paddleX + paddleWidth/2 &&
                    powerUpY >= paddleY - paddleHeight/2 &&
                    powerUpY <= paddleY + paddleHeight/2) {

                powerUp.collect();
                collectedPowerUps.add(powerUp);

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
        if (Math.random() < 0.3) {
            PowerUp powerUp = PowerFactory.createRandomPowerUp(x, y);
            powerUp.start();
            powerUps.add(powerUp);
            System.out.println("Power-up spawned: " + powerUp.getName());
        }
    }

    // === INPUT METHODS ===
    public void movePaddleLeft() { paddle.moveLeft(); }
    public void movePaddleRight() { paddle.moveRight(); }
    public void stopPaddle() { paddle.stop(); }
    public void launchBall() {
        if (!balls.isEmpty() && balls.get(0).isOnPaddle()) {
            Ball ball = balls.get(0);
            ball.setActive(true);
            ball.setVelocity(0, -Config.BALL_SPEED);
            System.out.println("Ball launched from paddle!");
        }
    }

    // === RENDER METHODS VỚI HÌNH ẢNH ===
    private void drawBackground() {
        if (gameBackground != null && !gameBackground.isError()) {
            // Vẽ background image
            ctx.drawImage(gameBackground, 0, 0, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT);
        } else {
            // Fallback: vẽ background bằng màu
            ctx.setFill(Color.BLACK);
            ctx.fillRect(0, 0, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT);

            ctx.setFill(Color.DARKBLUE);
            ctx.fillRect(Config.INSET, Config.UPPER_INSET,
                    Config.SCREEN_WIDTH - 2 * Config.INSET,
                    Config.SCREEN_HEIGHT - Config.UPPER_INSET - Config.INSET);

            ctx.setFill(Color.GRAY);
            ctx.fillRect(0, Config.UPPER_INSET, Config.INSET, Config.SCREEN_HEIGHT - Config.UPPER_INSET);
            ctx.fillRect(Config.SCREEN_WIDTH - Config.INSET, Config.UPPER_INSET, Config.INSET, Config.SCREEN_HEIGHT - Config.UPPER_INSET);
        }
    }

    private void drawBricks() {
        for (Brick brick : bricks) {
            double x = brick.getPosition().x;
            double y = brick.getPosition().y;
            double width = brick.getWidth();
            double height = brick.getHeight();

            Image brickImage = getImage(brick);

            if (brickImage != null) {
                // Vẽ sprite brick
                ctx.drawImage(brickImage, x, y, width, height);
            } else {
                // Fallback: vẽ bằng màu
                if (brick instanceof StrongBrick) {
                    StrongBrick strongBrick = (StrongBrick) brick;
                    ctx.setFill(strongBrick.getHitPoints() == 2 ? Color.ORANGE : Color.RED);
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
            if (strongBrick.getHitPoints() == 2 && brickStrongImage != null) {
                brickImage = brickStrongImage;
            } else if (brickStrongCrackedImage != null) {
                brickImage = brickStrongCrackedImage;
            }
        } else if (brickNormalImage != null) {
            brickImage = brickNormalImage;
        }
        return brickImage;
    }

    private void drawPowerUps() {
        for (PowerUp powerUp : powerUps) {
            if (powerUp.isCollected()) continue;

            double size = 40;
            double x = powerUp.getPosition().x - size/2;
            double y = powerUp.getPosition().y - size/2;

            javafx.scene.image.Image powerupImage = null;

            // XÁC ĐỊNH ẢNH CHO TỪNG LOẠI POWER-UP
            if (powerUp instanceof Entities.Power.ExpandPaddle) {
                powerupImage = powerupExpandImage;
            } else if (powerUp instanceof Entities.Power.FastBall) {
                powerupImage = powerupFastballImage;
            }

            if (powerupImage != null) {
                // VẼ SPRITE POWER-UP VỚI KÍCH THƯỚC 40x40
                ctx.drawImage(powerupImage, x, y, size, size);
            } else {
                // FALLBACK VỚI KÍCH THƯỚC 40x40
                drawPowerUpFallback(powerUp, x, y, size);
            }
        }
    }

    // METHOD FALLBACK CHO POWER-UP
    private void drawPowerUpFallback(PowerUp powerUp, double x, double y, double size) {
        if (powerUp instanceof Entities.Power.ExpandPaddle) {
            ctx.setFill(Color.BLUE);
        } else if (powerUp instanceof Entities.Power.FastBall) {
            ctx.setFill(Color.RED);
        } else {
            ctx.setFill(Color.GREEN);
        }


        ctx.fillRect(x, y, size, size);
        ctx.setStroke(Color.WHITE);
        ctx.strokeRect(x, y, size, size);

        ctx.setFill(Color.WHITE);
        if (powerUp instanceof Entities.Power.ExpandPaddle) {
            ctx.fillText("E", x + size/2 - 3, y + size/2 + 3);
        } else if (powerUp instanceof Entities.Power.FastBall) {
            ctx.fillText("F", x + size/2 - 3, y + size/2 + 3);
        }
    }

    private void drawPaddle() {
        double x = paddle.getPosition().x - paddle.getWidth()/2;
        double y = paddle.getPosition().y - paddle.getHeight()/2;

        if (paddleImage != null) {
            // Vẽ sprite paddle
            ctx.drawImage(paddleImage, x, y, paddle.getWidth(), paddle.getHeight());
        } else {
            // Fallback: vẽ bằng màu
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
                // Vẽ sprite ball
                ctx.drawImage(ballImage, x, y, diameter, diameter);
            } else {
                // Fallback: vẽ bằng màu
                ctx.setFill(ball.getColor());
                ctx.fillOval(x, y, diameter, diameter);
                ctx.setStroke(Color.WHITE);
                ctx.strokeOval(x, y, diameter, diameter);
            }
        }
    }

    private void drawUI() {
        ctx.setFill(Color.WHITE);
        ctx.fillText("SCORE: " + score, 20, 30);
        ctx.fillText("LIVES: " + lives, Config.SCREEN_WIDTH - 80, 30);
        ctx.fillText("GAMEGr08 - ARKANOID", Config.SCREEN_WIDTH/2 - 60, 30);

        String soundStatus = soundManager.isSoundEnabled() ? "ON" : "OFF";
        Color soundColor = soundManager.isSoundEnabled() ? Color.GREEN : Color.RED;
        ctx.setFill(soundColor);
        ctx.fillText("SOUND: " + soundStatus, Config.SCREEN_WIDTH - 120, 50);
        ctx.setFill(Color.WHITE);
        ctx.fillText("Press M to toggle", Config.SCREEN_WIDTH - 150, 65);

        // THÊM: Hiển thị hướng dẫn pause
        ctx.setFill(Color.YELLOW);
        ctx.fillText("Press P or ESC to pause", Config.SCREEN_WIDTH/2 - 70, Config.SCREEN_HEIGHT - 20);

        long activeBalls = balls.stream().filter(Ball::isActive).count();
        if (activeBalls == 0 && lives > 0) {
            ctx.setFill(Color.YELLOW);
            ctx.fillText("PRESS SPACE TO LAUNCH BALL", Config.SCREEN_WIDTH/2 - 90, Config.SCREEN_HEIGHT - 40);
            ctx.setFill(Color.WHITE);
            ctx.fillText("Lives remaining: " + lives, Config.SCREEN_WIDTH/2 - 50, Config.SCREEN_HEIGHT - 60);
        }

        if (lives <= 0) {
            ctx.setFill(Color.RED);
            ctx.fillText("GAME OVER", Config.SCREEN_WIDTH/2 - 40, Config.SCREEN_HEIGHT/2);
        }

        if (bricks.isEmpty()) {
            ctx.setFill(Color.GREEN);
            ctx.fillText("YOU WIN!", Config.SCREEN_WIDTH/2 - 30, Config.SCREEN_HEIGHT/2);
        }
    }

    // === GETTERS ===
    public boolean isRunning() { return isRunning; }
    public int getScore() { return score; }
    public int getLives() { return lives; }
    public boolean isPaused() { return isPaused; }
}