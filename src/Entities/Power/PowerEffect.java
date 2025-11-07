package Entities.Power;

import Entities.Ball;
import Entities.Paddle;

public interface PowerEffect {
    void activate(Paddle paddle, Ball ball);

    void deactivate(Paddle paddle, Ball ball);

    String getEffectName();
}