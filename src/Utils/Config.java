package Utils;

public class Config {
    public static final int TOTAL_LEVELS = 5;
    public static final String SAVE_FILE_PATH = "arkanoid_save.properties";
    public static final String HIGHSCORE_FILE = "highscores.dat";
    public static final String PROGRESS_FILE = "game_progress.dat";

    // Kích thước màn hình
    public static final double SCREEN_WIDTH = 700;
    public static final double SCREEN_HEIGHT = 750;
    public static final double INSET = 3;
    public static final double UPPER_INSET = 80;

    // Vật lý game
    public static final double BALL_SPEED = 450.0;
    public static final double PADDLE_SPEED = 500.0;
    public static final double BONUS_BLOCK_SPEED = 70.0;

    // Kích thước đối tượng
    public static final double BLOCK_WIDTH = 55;
    public static final double BLOCK_HEIGHT = 22;
    public static final double BALL_SIZE = 12;
    public static final double PADDLE_STD_WIDTH = 80;
    public static final double PADDLE_STD_HEIGHT = 22;
    public static final double PADDLE_OFFSET_Y = 68;

    public static final int BONUS_BLOCK_INTERVAL = 20;
}