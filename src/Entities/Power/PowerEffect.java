package Entities.Power;

import Entities.Paddle;
import Entities.Ball;

public interface PowerEffect {
    void activate(Paddle paddle, Ball ball);
    void deactivate(Paddle paddle, Ball ball);
    String getEffectName();
}