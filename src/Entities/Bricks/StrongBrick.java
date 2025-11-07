package Entities.Bricks;

import Utils.Config;

/**
 * Gạch cứng - cần nhiều lần va chạm để phá hủy
 */
public class StrongBrick extends Brick {

    public StrongBrick(double x, double y, int hitPoints) {
        super("StrongBrick", x, y,
                Config.BLOCK_WIDTH, Config.BLOCK_HEIGHT,
                hitPoints,  // custom hit points
                200         // 200 điểm
        );
    }

    @Override
    public void onHit() {
        super.onHit();
        System.out.println("StrongBrick hit! Remaining HP: " + hitPoints);
    }
}