package Engine;

import Entities.Bricks.Brick;
import Entities.Bricks.NormalBrick;
import Entities.Bricks.StrongBrick;
import Utils.Config;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class LevelManager {
    public enum BrickType {
        NONE, NORMAL, STRONG
    }

    // === LEVEL DESIGNS ===
    private static final BrickType[][] LEVEL_1 = {
            {BrickType.NORMAL, BrickType.NORMAL, BrickType.NORMAL, BrickType.NORMAL, BrickType.NORMAL, BrickType.NORMAL, BrickType.NORMAL, BrickType.NORMAL},
            {BrickType.NORMAL, BrickType.NONE,    BrickType.NONE,    BrickType.NONE,    BrickType.NONE,    BrickType.NONE,    BrickType.NONE,    BrickType.NORMAL},
            {BrickType.NORMAL, BrickType.NONE,    BrickType.STRONG, BrickType.STRONG, BrickType.STRONG, BrickType.STRONG, BrickType.NONE,    BrickType.NORMAL},
            {BrickType.NORMAL, BrickType.NONE,    BrickType.NONE,    BrickType.NONE,    BrickType.NONE,    BrickType.NONE,    BrickType.NONE,    BrickType.NORMAL},
            {BrickType.NORMAL, BrickType.NORMAL, BrickType.NORMAL, BrickType.NORMAL, BrickType.NORMAL, BrickType.NORMAL, BrickType.NORMAL, BrickType.NORMAL}
    };

    private static final BrickType[][] LEVEL_2 = {
            {BrickType.STRONG, BrickType.NONE,    BrickType.NORMAL, BrickType.NORMAL, BrickType.NORMAL, BrickType.NORMAL, BrickType.NONE,    BrickType.STRONG},
            {BrickType.NONE,    BrickType.STRONG, BrickType.NONE,    BrickType.NORMAL, BrickType.NORMAL, BrickType.NONE,    BrickType.STRONG, BrickType.NONE},
            {BrickType.NORMAL, BrickType.NONE,    BrickType.STRONG, BrickType.NONE,    BrickType.NONE,    BrickType.STRONG, BrickType.NONE,    BrickType.NORMAL},
            {BrickType.NONE,    BrickType.STRONG, BrickType.NONE,    BrickType.NORMAL, BrickType.NORMAL, BrickType.NONE,    BrickType.STRONG, BrickType.NONE},
            {BrickType.STRONG, BrickType.NONE,    BrickType.NORMAL, BrickType.NORMAL, BrickType.NORMAL, BrickType.NORMAL, BrickType.NONE,    BrickType.STRONG}
    };

    private static final BrickType[][] LEVEL_3 = {
            {BrickType.NORMAL, BrickType.STRONG, BrickType.NORMAL, BrickType.STRONG, BrickType.NORMAL, BrickType.STRONG, BrickType.NORMAL, BrickType.STRONG},
            {BrickType.STRONG, BrickType.NORMAL, BrickType.STRONG, BrickType.NORMAL, BrickType.STRONG, BrickType.NORMAL, BrickType.STRONG, BrickType.NORMAL},
            {BrickType.NORMAL, BrickType.STRONG, BrickType.NORMAL, BrickType.STRONG, BrickType.NORMAL, BrickType.STRONG, BrickType.NORMAL, BrickType.STRONG},
            {BrickType.STRONG, BrickType.NORMAL, BrickType.STRONG, BrickType.NORMAL, BrickType.STRONG, BrickType.NORMAL, BrickType.STRONG, BrickType.NORMAL},
            {BrickType.NORMAL, BrickType.STRONG, BrickType.NORMAL, BrickType.STRONG, BrickType.NORMAL, BrickType.STRONG, BrickType.NORMAL, BrickType.STRONG}
    };

    private static final BrickType[][] LEVEL_4 = {
            {BrickType.STRONG, BrickType.STRONG, BrickType.STRONG, BrickType.STRONG, BrickType.STRONG, BrickType.STRONG, BrickType.STRONG, BrickType.STRONG},
            {BrickType.STRONG, BrickType.NORMAL, BrickType.NORMAL, BrickType.NORMAL, BrickType.NORMAL, BrickType.NORMAL, BrickType.NORMAL, BrickType.STRONG},
            {BrickType.STRONG, BrickType.NORMAL, BrickType.STRONG, BrickType.STRONG, BrickType.STRONG, BrickType.STRONG, BrickType.NORMAL, BrickType.STRONG},
            {BrickType.STRONG, BrickType.NORMAL, BrickType.NORMAL, BrickType.NORMAL, BrickType.NORMAL, BrickType.NORMAL, BrickType.NORMAL, BrickType.STRONG},
            {BrickType.STRONG, BrickType.STRONG, BrickType.STRONG, BrickType.STRONG, BrickType.STRONG, BrickType.STRONG, BrickType.STRONG, BrickType.STRONG}
    };

    private static final BrickType[][] LEVEL_5 = {
            {BrickType.NONE,    BrickType.NONE,    BrickType.STRONG, BrickType.STRONG, BrickType.STRONG, BrickType.STRONG, BrickType.NONE,    BrickType.NONE},
            {BrickType.NONE,    BrickType.STRONG, BrickType.STRONG, BrickType.STRONG, BrickType.STRONG, BrickType.STRONG, BrickType.STRONG, BrickType.NONE},
            {BrickType.STRONG, BrickType.STRONG, BrickType.STRONG, BrickType.STRONG, BrickType.STRONG, BrickType.STRONG, BrickType.STRONG, BrickType.STRONG},
            {BrickType.NONE,    BrickType.STRONG, BrickType.STRONG, BrickType.STRONG, BrickType.STRONG, BrickType.STRONG, BrickType.STRONG, BrickType.NONE},
            {BrickType.NONE,    BrickType.NONE,    BrickType.STRONG, BrickType.STRONG, BrickType.STRONG, BrickType.STRONG, BrickType.NONE,    BrickType.NONE}
    };

    private static final List<BrickType[][]> LEVELS = List.of(LEVEL_1, LEVEL_2, LEVEL_3, LEVEL_4, LEVEL_5);

    private int currentLevel = 0;
    private List<Brick> bricks;
    private int totalBricks;

    // Khoảng cách giữa các brick
    private static final double HORIZONTAL_GAP = 12;
    private static final double VERTICAL_GAP = 12;

    public LevelManager() {
        this.bricks = new ArrayList<>();
    }

    public void loadLevel(int levelNumber) {
        if (levelNumber < 0 || levelNumber >= LEVELS.size()) {
            throw new IllegalArgumentException("Invalid level number: " + levelNumber);
        }

        this.currentLevel = levelNumber;
        this.bricks.clear();
        createBricks(LEVELS.get(levelNumber));
    }

    private void createBricks(BrickType[][] layout) {
        int rows = layout.length;
        int cols = layout[0].length;

        double brickWidth = Config.BLOCK_WIDTH;
        double brickHeight = Config.BLOCK_HEIGHT;

        // Tính toán vị trí bắt đầu để căn giữa
        double totalWidth = cols * brickWidth + (cols - 1) * HORIZONTAL_GAP;
        double startX = (Config.SCREEN_WIDTH - totalWidth) / 2;
        double startY = Config.UPPER_INSET + 50; // Tăng khoảng cách từ trên xuống

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                BrickType type = layout[row][col];
                if (type == BrickType.NONE) continue;

                double x = startX + col * (brickWidth + HORIZONTAL_GAP);
                double y = startY + row * (brickHeight + VERTICAL_GAP);

                Brick brick = createBrick(type, x, y);
                if (brick != null) {
                    bricks.add(brick);
                }
            }
        }

        this.totalBricks = bricks.size();
        System.out.println("Level " + (currentLevel + 1) + " loaded with " + totalBricks + " bricks");
        System.out.println("Brick spacing - Horizontal: " + HORIZONTAL_GAP + ", Vertical: " + VERTICAL_GAP);
    }

    private Brick createBrick(BrickType type, double x, double y) {
        return switch (type) {
            case NORMAL -> new NormalBrick(x, y);
            case STRONG -> {
                // Tăng độ khó theo level: level 1-2: 2 HP, level 3-4: 3 HP, level 5: 4 HP
                int hitPoints = 2 + (currentLevel / 2);
                yield new StrongBrick(x, y, hitPoints);
            }
            default -> null;
        };
    }

    public List<Brick> getBricks() {
        return bricks;
    }

    public void removeBrick(Brick brick) {
        bricks.remove(brick);
    }

    public boolean isLevelCompleted() {
        return bricks.isEmpty();
    }

    public boolean hasNextLevel() {
        return currentLevel < LEVELS.size() - 1;
    }

    public void nextLevel() {
        if (hasNextLevel()) {
            loadLevel(currentLevel + 1);
        }
    }

    public int getCurrentLevel() {
        return currentLevel + 1;
    }

    public int getTotalLevels() {
        return LEVELS.size();
    }

    public int getRemainingBricks() {
        return bricks.size();
    }

    public int getTotalBricks() {
        return totalBricks;
    }

    public void reset() {
        loadLevel(currentLevel);
    }

    // Method để điều chỉnh khoảng cách nếu cần
    public static void setBrickSpacing(double horizontalGap, double verticalGap) {
        // Có thể thêm logic để thay đổi khoảng cách động
        System.out.println("Brick spacing set to: H=" + horizontalGap + ", V=" + verticalGap);
    }
}