package Entities.Bricks;

import Utils.Config;

/**
 * Gạch thường - vỡ sau 1 lần va chạm
 */
public class NormalBrick extends Brick {

    public NormalBrick(double x, double y) {
        super("NormalBrick", x, y,
                Config.BLOCK_WIDTH, Config.BLOCK_HEIGHT,
                1,  // 1 hit point
                100 // 100 điểm
        );
    }

    @Override
    public void onHit() {
        super.onHit();
        // Có thể thêm hiệu ứng đặc biệt ở đây
    }
}