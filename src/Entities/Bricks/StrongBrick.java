package Entities.Bricks;

import Utils.Config;

/**
 * Gạch cứng - cần 2 lần va chạm để phá
 */
public class StrongBrick extends Brick {

    public StrongBrick(double x, double y) {
        super("StrongBrick", x, y,
                Config.BLOCK_WIDTH, Config.BLOCK_HEIGHT,
                2,  // 2 hit points
                200 // 200 điểm
        );
    }

    @Override
    public void onHit() {
        super.onHit();
        System.out.println("Strong brick hit! Remaining: " + hitPoints);
    }
}