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
    private boolean gameCompleted = false;

    // === STATE METHODS ===
    public State getCurrentState() {
        return currentState;
    }

    public void setCurrentState(State state) {
        this.currentState = state;
    }

    // === LEVEL METHODS ===
    public int getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(int level) {
        this.currentLevel = level;
    }

    public void nextLevel() {
        this.currentLevel++;
    }

    // === SCORE METHODS ===
    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void addScore(int points) {
        this.score += points;
    }

    // === LIVES METHODS ===
    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public void loseLife() {
        this.lives--;
    }

    public void addLife() {
        this.lives++;
    }

    // === GAME COMPLETION ===
    public boolean isGameCompleted() {
        return gameCompleted;
    }

    public void setGameCompleted(boolean completed) {
        this.gameCompleted = completed;
    }

    // === SOUND SETTINGS ===
    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }

    // === GAME STATUS CHECKS ===
    public boolean isGameOver() {
        return lives <= 0;
    }

    public boolean isPlaying() {
        return currentState == State.PLAYING;
    }

    public boolean isPaused() {
        return currentState == State.PAUSED;
    }

    public boolean isMenu() {
        return currentState == State.MENU;
    }

    // === RESET METHOD ===
    public void reset() {
        currentState = State.MENU;
        currentLevel = 1;
        score = 0;
        lives = 3;
        gameCompleted = false;
    }

    // === UTILITY METHODS ===
    public void startGame() {
        currentState = State.PLAYING;
        score = 0;
        lives = 3;
        gameCompleted = false;
    }

    public void pauseGame() {
        if (currentState == State.PLAYING) {
            currentState = State.PAUSED;
        }
    }

    public void resumeGame() {
        if (currentState == State.PAUSED) {
            currentState = State.PLAYING;
        }
    }

    public void completeLevel() {
        currentState = State.LEVEL_COMPLETE;
    }

    public void gameOver() {
        currentState = State.GAME_OVER;
    }

    public void returnToMenu() {
        currentState = State.MENU;
    }
}