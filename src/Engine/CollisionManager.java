package Engine;

import Entities.Ball;
import Entities.Bricks.Brick;
import Entities.Paddle;
import Utils.Config;

public class CollisionManager {

    public static boolean checkBallPaddleCollision(Ball ball, Paddle paddle) {
        if (!ball.isActive()) return false;

        // lấy vị trí & kích thước
        double ballX = ball.getPosition().x;
        double ballY = ball.getPosition().y;
        double ballRadius = ball.getRadius();

        double paddleX = paddle.getPosition().x;
        double paddleY = paddle.getPosition().y;
        double paddleWidth = paddle.getWidth();
        double paddleHeight = paddle.getHeight();

        // Kiểm tra va chạm
        boolean isColliding = ballY + ballRadius >= paddleY - paddleHeight / 2 &&
                ballY - ballRadius <= paddleY + paddleHeight / 2 &&
                ballX + ballRadius >= paddleX - paddleWidth / 2 &&
                ballX - ballRadius <= paddleX + paddleWidth / 2;

        if (isColliding && ball.getVelocity().y > 0) { // va chạm + bóng đang đi xuống
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

        // Tính vị trí va chạm trên paddle (-1 đến 1) trái - giữa - phải
        double hitPosition = (ballX - paddleX) / (paddleWidth / 2);
        hitPosition = Math.max(-0.8, Math.min(0.8, hitPosition));

        // Góc nảy từ -60 đến 60 độ
        double bounceAngle = hitPosition * 60.0;
        double angleRad = Math.toRadians(bounceAngle);

        // Giữ nguyên tốc độ hiện tại hoặc dùng tốc độ mặc định
        double currentSpeed = Math.sqrt(ball.getVelocity().x * ball.getVelocity().x +
                ball.getVelocity().y * ball.getVelocity().y);
        double speed = Math.max(currentSpeed, Config.BALL_SPEED * 0.8);

        // velocity mới
        double newVX = speed * Math.sin(angleRad);
        double newVY = - Math.abs(speed * Math.cos(angleRad));

        // Đặt velocity và vị trí mới
        ball.setVelocity(newVX, newVY);
        ball.getPosition().y = paddleY - paddleHeight / 2 - ballRadius - 1;
        // sau khi đổi hướng, đặt tọa độ y của bóng lên trên paddle một chút

        System.out.println("Paddle collision - HitPos: " + hitPosition +
                ", Angle: " + bounceAngle + "°, Speed: " + speed);
    }

    public static boolean checkBallBrickCollision(Ball ball, Brick brick) {
        if (!ball.isActive()) return false;

        double ballX = ball.getPosition().x;
        double ballY = ball.getPosition().y;
        double ballRadius = ball.getRadius();

        if (brick.collidesWith(ballX, ballY, ballRadius)) {

            // Nếu bóng xuyên phá, không đảo hướng
            if (ball.isPierce()) {
                System.out.println("PierceBall xuyên qua gạch!");
                return true; // chỉ báo gamescene có va chạm để phá gạch, k đổi hướng
            }
            // Xác định hướng va chạm để nảy đúng hướng
            double brickLeft = brick.getPosition().x;
            double brickRight = brick.getPosition().x + brick.getWidth();
            double brickTop = brick.getPosition().y;
            double brickBottom = brick.getPosition().y + brick.getHeight();

            // Tính khoảng cách từ tâm ball đến các cạnh brick
            double overlapLeft = Math.abs(ballX - brickLeft);
            double overlapRight = Math.abs(ballX - brickRight);
            double overlapTop = Math.abs(ballY - brickTop);
            double overlapBottom = Math.abs(ballY - brickBottom);

            // Tìm hướng va chạm gần nhất
            double minOverlap = Math.min(Math.min(overlapLeft, overlapRight),
                    Math.min(overlapTop, overlapBottom));

            if (minOverlap == overlapLeft || minOverlap == overlapRight) {
                // Va chạm trái/phải - đảo ngược hướng X
                ball.getVelocity().x = -ball.getVelocity().x;
                System.out.println("Brick side collision - X reversed");
            } else {
                // Va chạm trên/dưới - đảo ngược hướng Y
                ball.getVelocity().y = -ball.getVelocity().y;
                System.out.println("Brick top/bottom collision - Y reversed");
            }
            return true;
        }
        return false;
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