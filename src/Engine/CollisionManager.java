package Engine;

import Entities.Ball;
import Entities.Paddle;
import Utils.Config;

import java.util.List;

public class CollisionManager {

    public static boolean checkBallPaddleCollision(Ball ball, Paddle paddle) {
        if (!ball.isActive()) return false;

        double ballX = ball.getPosition().x;
        double ballY = ball.getPosition().y;
        double ballRadius = ball.getRadius();

        double paddleX = paddle.getPosition().x;
        double paddleY = paddle.getPosition().y;
        double paddleWidth = paddle.getWidth();
        double paddleHeight = paddle.getHeight();

        // Kiểm tra va chạm
        boolean isColliding = ballY + ballRadius >= paddleY - paddleHeight/2 &&
                ballY - ballRadius <= paddleY + paddleHeight/2 &&
                ballX + ballRadius >= paddleX - paddleWidth/2 &&
                ballX - ballRadius <= paddleX + paddleWidth/2;

        if (isColliding && ball.getVelocity().y > 0) {
            handlePaddleBounce(ball, paddle, ballX, ballY, paddleX, paddleY, paddleWidth, paddleHeight);
            return true;
        }

        return false;
    }

    private static void handlePaddleBounce(Ball ball, Paddle paddle,
                                           double ballX, double ballY,
                                           double paddleX, double paddleY,
                                           double paddleWidth, double paddleHeight) {
        double ballRadius = ball.getRadius();

        // Tính vị trí va chạm trên paddle (-1 đến 1)
        double hitPosition = (ballX - paddleX) / (paddleWidth / 2);
        hitPosition = Math.max(-0.9, Math.min(0.9, hitPosition));

        // Góc nảy từ -60 đến 60 độ
        double bounceAngle = hitPosition * 60.0;
        double angleRad = Math.toRadians(bounceAngle);

        // Giữ nguyên tốc độ hiện tại hoặc dùng tốc độ mặc định
        double currentSpeed = Math.sqrt(ball.getVelocity().x * ball.getVelocity().x +
                ball.getVelocity().y * ball.getVelocity().y);
        double speed = Math.max(currentSpeed, Config.BALL_SPEED * 0.8);

        // Tính velocity mới
        double newVX = speed * Math.sin(angleRad);
        double newVY = -Math.abs(speed * Math.cos(angleRad));

        // Đặt velocity và vị trí mới
        ball.setVelocity(newVX, newVY);
        ball.getPosition().y = paddleY - paddleHeight/2 - ballRadius - 1;

        System.out.println("Paddle collision - HitPos: " + hitPosition +
                ", Angle: " + bounceAngle + "°, Speed: " + speed);
    }


    public static void checkWallCollisions(Ball ball) {
        if (!ball.isActive()) return;

        double ballX = ball.getPosition().x;
        double ballY = ball.getPosition().y;
        double ballRadius = ball.getRadius();

        // Tường trái
        if (ballX - ballRadius <= Config.INSET) {
            ball.getVelocity().x = Math.abs(ball.getVelocity().x);
            ball.getPosition().x = Config.INSET + ballRadius + 1;
            System.out.println("Left wall collision");
        }

        // Tường phải
        if (ballX + ballRadius >= Config.SCREEN_WIDTH - Config.INSET) {
            ball.getVelocity().x = -Math.abs(ball.getVelocity().x);
            ball.getPosition().x = Config.SCREEN_WIDTH - Config.INSET - ballRadius - 1;
            System.out.println("Right wall collision");
        }

        // Trần
        if (ballY - ballRadius <= Config.UPPER_INSET) {
            ball.getVelocity().y = Math.abs(ball.getVelocity().y);
            ball.getPosition().y = Config.UPPER_INSET + ballRadius + 1;
            System.out.println("Top wall collision");
        }
    }
}