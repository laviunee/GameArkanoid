package Engine;

/**
 * Đại diện cho trạng thái hiện tại của game
 */
public class GameState {
    public enum State {
        MENU, PLAYING, PAUSED, GAME_OVER, LEVEL_COMPLETE
    }

    private State currentState = State.MENU;
    private int currentLevel = 1;
    private int score = 0;
    private int lives = 3;
    private boolean soundEnabled = true;

    public State getCurrentState() { return currentState; }
    public void setCurrentState(State state) { this.currentState = state; }

    public int getCurrentLevel() { return currentLevel; }
    public void setCurrentLevel(int level) { this.currentLevel = level; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public void addScore(int points) { this.score += points; }

    public int getLives() { return lives; }
    public void setLives(int lives) { this.lives = lives; }
    public void loseLife() { this.lives--; }
    public void addLife() { this.lives++; }

    public boolean isSoundEnabled() { return soundEnabled; }
    public void setSoundEnabled(boolean enabled) { this.soundEnabled = enabled; }

    public boolean isGameOver() { return lives <= 0; }

    public void reset() {
        currentState = State.MENU;
        currentLevel = 1;
        score = 0;
        lives = 3;
    }
}