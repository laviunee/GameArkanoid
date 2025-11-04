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
    private List<Brick> bricks;
    private List<PowerUp> powerUps;
    private int score;
    private int lives;
    private boolean isRunning;

    // Pause scene và trạng thái pause
    private GameEngine gameEngine;
    private Runnable onGameOver;
    private PauseScene pauseScene;
    private boolean isPaused = false;

    // Mouse control
    private boolean mouseControlEnabled = false;
    private double mouseX = 0;

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
    private javafx.scene.image.Image powerupExtraliveImage;
    private javafx.scene.image.Image powerupPierceballImage;

    public GameScene(GraphicsContext ctx, GameEngine gameEngine, Runnable onGameOver) {
        this.ctx = ctx;
        this.canvas = ctx.getCanvas();
        this.gameEngine = gameEngine;
        this.onGameOver = onGameOver;
        this.soundManager = SoundManager.getInstance();
        this.spriteLoader = SpriteLoader.getInstance();
        this.isRunning = true;
        this.pauseScene = null;
        loadGameSprites();
        setupMouseControls();
    }

    // THÊM: Thiết lập điều khiển chuột
    private void setupMouseControls() {
        // Di chuyển paddle khi di chuột
        canvas.setOnMouseMoved(this::handleMouseMoved);

        // Di chuyển paddle khi kéo chuột
        canvas.setOnMouseDragged(this::handleMouseDragged);

        // Click để bật/tắt điều khiển chuột hoặc launch ball
        canvas.setOnMouseClicked(this::handleMouseClicked);

        // Theo dõi khi chuột rời khỏi canvas
        canvas.setOnMouseExited(this::handleMouseExited);
    }

    // THÊM: Xử lý sự kiện chuột
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

        // Click trái để launch ball
        if (event.isPrimaryButtonDown()) {
            launchBall();
        }

        // Click phải để bật/tắt điều khiển chuột
        if (event.isSecondaryButtonDown()) {
            toggleMouseControl();
        }
    }

    private void handleMouseExited(MouseEvent event) {
        // Dừng paddle khi chuột rời khỏi canvas
        if (!isRunning || isPaused) return;
        paddle.stop();
    }

    private void handleGameOver() {
        System.out.println("🎮 GameScene: Game Over detected");
        soundManager.onGameOver(); // THÊM: Thông báo game over cho sound manager

        if (onGameOver != null) {
            onGameOver.run(); // Gọi callback khi game over
        } else {
            System.err.println("❌ GameScene: onGameOver callback is null!");
            // Fallback: chuyển thẳng đến menu
            gameEngine.switchToMenuScene();
        }
    }

    // THÊM: Bật/tắt điều khiển chuột
    private void toggleMouseControl() {
        mouseControlEnabled = !mouseControlEnabled;
        System.out.println("Mouse control: " + (mouseControlEnabled ? "ENABLED" : "DISABLED"));

        if (!mouseControlEnabled) {
            paddle.stop();
        }
    }

    // THÊM: Method để resume game từ pause
    private void resumeGame() {
        isPaused = false;
        pauseScene.cleanup();
        System.out.println("Game resumed");
    }

    public int getCurrentLevel() {
        return 1; // Hiện tại mặc định level 1, có thể thay đổi sau
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
            powerupExtraliveImage = spriteLoader.loadSprite("/images/powerup/extralive.png");
            powerupPierceballImage = spriteLoader.loadSprite("/images/powerup/pierceball.png");
            System.out.println("Game sprites loaded successfully");

        } catch (Exception e) {
            System.err.println("Error loading game sprites: " + e.getMessage());
        }
    }

    @Override
    public void start() {
        System.out.println("🎮 GameScene: Starting game...");

        soundManager.stopAllSounds();

        double paddleX = Config.SCREEN_WIDTH / 2;
        double paddleY = Config.SCREEN_HEIGHT - Config.PADDLE_OFFSET_Y;
        paddle = new Paddle(paddleX, paddleY);

        balls = new ArrayList<>();
        spawnBall();
        Entities.Power.PowerFactory.setBalls(balls);
        Entities.Power.PowerFactory.setGameScene(this);

        bricks = new ArrayList<>();
        setupBricks();

        powerUps = new ArrayList<>();
        score = 0;
        lives = 3;

        isRunning = true;
        isPaused = false;
        mouseControlEnabled = false;

        // QUAN TRỌNG: Báo cho SoundManager biết game đã bắt đầu
        soundManager.onGameStart();
        System.out.println("🎵 GameScene: Game start signal sent to SoundManager");
    }

    @Override
    public void update(double deltaTime) {
        // Không update nếu game đang pause
        if (!isRunning || isPaused) return;

        // Nếu điều khiển chuột được bật, cập nhật vị trí paddle
        if (mouseControlEnabled) {
            paddle.moveToMouse(mouseX);
        } else {
            paddle.update(deltaTime);
        }

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
            soundManager.onGameWin(); // THÊM: Thông báo win cho sound manager
            checkHighscore();
        }

        if (lives <= 0) {
            System.out.println("GAME OVER!");
            isRunning = false;
            handleGameOver();
        }
    }

    // Thêm method mới
    private void checkHighscore() {
        SaveManager saveManager = SaveManager.getInstance();
        if (saveManager.isHighscore(score)) {
            // Chuyển đến scene nhập tên
            gameEngine.switchToNameInputScene(score, 1); // Giả sử level 1
        } else {
            // Hoặc chuyển thẳng đến highscore
            handleGameOver();
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
                case LEFT, A -> {
                    if (!mouseControlEnabled) movePaddleLeft();
                }
                case RIGHT, D -> {
                    if (!mouseControlEnabled) movePaddleRight();
                }
                case SPACE -> launchBall();
                case M -> {
                    boolean currentState = soundManager.isSoundEnabled();
                    soundManager.setSoundEnabled(!currentState);
                    System.out.println("Sound toggled from " + currentState + " to " + soundManager.isSoundEnabled());
                }
                case C -> toggleMouseControl(); // THÊM: Phím C để bật/tắt điều khiển chuột
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
                case LEFT, RIGHT, A, D -> {
                    if (!mouseControlEnabled) stopPaddle();
                }
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

        // THÊM: Remove mouse event handlers để tránh memory leak
        canvas.setOnMouseMoved(null);
        canvas.setOnMouseDragged(null);
        canvas.setOnMouseClicked(null);
        canvas.setOnMouseExited(null);
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
        double startY = Config.UPPER_INSET + 60;

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

        for (Ball ball : balls) {
            if (!ball.isActive()) continue;

            if (CollisionManager.checkBallPaddleCollision(ball, paddle)) {
                soundManager.playSound("paddle_hit");
            }

            for (Brick brick : bricks) {
                if (CollisionManager.checkBallBrickCollision(ball, brick)) {

                    brick.onHit();
                    score += brick.getScoreValue();

                    // Nếu bóng xuyên → không đổi hướng
                    if (!ball.isPierce()) {
                        soundManager.playSound("hit");
                    } else {
                        soundManager.playSound("powerup"); // âm khác cho ngầu
                    }

                    // Nếu gạch vỡ → spawn power-up
                    if (brick.isToBeRemoved()) {
                        spawnPowerUp(brick.getPosition().x, brick.getPosition().y);
                    }

                    // Chỉ bật lại hướng nếu KHÔNG xuyên
                    if (!ball.isPierce()) {
                        // giữ nguyên code đảo vận tốc cũ của bạn
                    }

                    break;
                }
            }

            CollisionManager.checkWallCollisions(ball);

            // Nếu bóng rơi khỏi màn hình
            if (ball.getPosition().y + ball.getRadius() >= Config.SCREEN_HEIGHT) {
                System.out.println("Ball lost at bottom!");
                ballsToRemove.add(ball);
            }

// Xóa các bóng đã rơi
            balls.removeAll(ballsToRemove);

// Chỉ trừ mạng khi KHÔNG còn bóng nào
            if (balls.isEmpty()) {
                lives--;
                System.out.println("All balls lost! Lives remaining: " + lives);

                if (lives > 0) {
                    // 🔹 Mất 1 mạng nhưng chưa thua -> nhạc lose1
                    soundManager.onLoseLife();
                    System.out.println("Respawning NEW ball on paddle");
                    spawnBall();
                } else {
                    // 🔹 Hết mạng thật -> nhạc lose (drama)
                    soundManager.onGameOver();
                    System.out.println("GAME OVER!");
                    isRunning = false;
                    handleGameOver();
                }
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
        if (Math.random() < 0.5) {
            PowerUp powerUp = PowerFactory.createRandomPowerUp(x, y);
            powerUp.start();
            powerUps.add(powerUp);
            System.out.println("Power-up spawned: " + powerUp.getName());
        }
    }

    // === INPUT METHODS ===
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
        double size = 25; // Kích thước thống nhất cho mọi power-up

        for (PowerUp powerUp : powerUps) {
            if (powerUp.isCollected()) continue;

            double x = powerUp.getPosition().x - size / 2;
            double y = powerUp.getPosition().y - size / 2;

            Image powerupImage = null;

            // XÁC ĐỊNH ẢNH CHO TỪNG LOẠI POWER-UP
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

            // VẼ ẢNH (đã chuẩn hóa)
            if (powerupImage != null) {
                ctx.drawImage(powerupImage, x, y, size, size);
            } else {
                // Fallback nếu thiếu ảnh
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
        ctx.setFill(Color.BLACK);
        ctx.fillText("SCORE: " + score, 20, 30);
        ctx.fillText("LIVES: " + lives, Config.SCREEN_WIDTH - 80, 30);
        ctx.fillText("LEVEL 1", Config.SCREEN_WIDTH/2 - 40, 40);

        String soundStatus = soundManager.isSoundEnabled() ? "ON" : "OFF";
        Color soundColor = soundManager.isSoundEnabled() ? Color.GREEN : Color.RED;
        ctx.setFill(soundColor);
        ctx.fillText("SOUND: " + soundStatus, Config.SCREEN_WIDTH - 120, 50);
        ctx.setFill(Color.BLACK);
        ctx.fillText("Press M to toggle", Config.SCREEN_WIDTH - 150, 65);

        // THÊM: Hiển thị trạng thái điều khiển chuột
        String mouseControlStatus = mouseControlEnabled ? "MOUSE CONTROL: ON" : "MOUSE CONTROL: OFF";
        Color mouseColor = mouseControlEnabled ? Color.GREEN : Color.YELLOW;
        ctx.setFill(mouseColor);
        ctx.fillText(mouseControlStatus, Config.SCREEN_WIDTH/2 - 70, 60);

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
    public void addLive() {
        lives++;
        System.out.println("Live added! Lives = " + lives);
    }

    public boolean isRunning() { return isRunning; }
    public int getScore() { return score; }
    public int getLives() { return lives; }
    public boolean isPaused() { return isPaused; }
    public boolean isMouseControlEnabled() { return mouseControlEnabled; }
}