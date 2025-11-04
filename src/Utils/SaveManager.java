package Utils;

import java.io.*;
import java.util.*;

public class SaveManager {
    private static final String HIGHSCORE_FILE = "highscores.dat";
    private static final String PROGRESS_FILE = "game_progress.dat";
    private static final int MAX_HIGHSCORES = 10;
    private static SaveManager instance;
    private List<HighscoreEntry> highscores;
    private GameProgress gameProgress;

    private SaveManager() {
        this.highscores = new ArrayList<>();
        this.gameProgress = new GameProgress();
        loadHighscores();
        loadGameProgress();
    }

    public static SaveManager getInstance() {
        if (instance == null) {
            instance = new SaveManager();
        }
        return instance;
    }

    // Inner class for highscore entry
    public static class HighscoreEntry implements Serializable {
        private static final long serialVersionUID = 1L;

        private String playerName;
        private int score;
        private int level;
        private long timestamp;

        public HighscoreEntry(String playerName, int score, int level) {
            this.playerName = playerName;
            this.score = score;
            this.level = level;
            this.timestamp = System.currentTimeMillis();
        }

        // Getters
        public String getPlayerName() { return playerName; }
        public int getScore() { return score; }
        public int getLevel() { return level; }
        public long getTimestamp() { return timestamp; }
        public String getFormattedDate() {
            return new Date(timestamp).toString();
        }
    }

    // Inner class for game progress
    private static class GameProgress implements Serializable {
        private static final long serialVersionUID = 2L;

        private int unlockedLevels = 1;
        private Map<Integer, Boolean> levelCompleted = new HashMap<>();
        private Map<Integer, Integer> levelScores = new HashMap<>();
        private int totalScore = 0;

        public GameProgress() {
            // Khởi tạo level 1 luôn mở khóa
            unlockedLevels = 1;
            levelCompleted.put(1, false);
        }

        // Getters and setters
        public int getUnlockedLevels() { return unlockedLevels; }
        public void setUnlockedLevels(int unlockedLevels) { this.unlockedLevels = unlockedLevels; }

        public boolean isLevelCompleted(int level) {
            return levelCompleted.getOrDefault(level, false);
        }

        public void setLevelCompleted(int level, boolean completed) {
            levelCompleted.put(level, completed);
        }

        public int getLevelScore(int level) {
            return levelScores.getOrDefault(level, 0);
        }

        public void setLevelScore(int level, int score) {
            levelScores.put(level, score);
        }

        public int getTotalScore() { return totalScore; }
        public void setTotalScore(int totalScore) { this.totalScore = totalScore; }
    }

    // === HIGHSCORE METHODS ===
    public void saveHighscore(String playerName, int score, int level) {
        if (playerName == null || playerName.trim().isEmpty()) {
            playerName = "Anonymous";
        }

        HighscoreEntry newEntry = new HighscoreEntry(playerName, score, level);
        highscores.add(newEntry);

        // Sắp xếp theo điểm giảm dần
        highscores.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));

        // Giữ chỉ MAX_HIGHSCORES entries
        if (highscores.size() > MAX_HIGHSCORES) {
            highscores = new ArrayList<>(highscores.subList(0, MAX_HIGHSCORES));
        }

        saveHighscores();
    }

    public List<HighscoreEntry> getHighscores() {
        // Trả về bản copy đã sắp xếp
        List<HighscoreEntry> sorted = new ArrayList<>(highscores);
        sorted.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));
        return sorted;
    }

    public boolean isHighscore(int score) {
        // Nếu danh sách chưa đủ MAX_HIGHSCORES, luôn là highscore
        if (highscores.size() < MAX_HIGHSCORES) {
            return true;
        }

        // Kiểm tra xem score có cao hơn điểm thấp nhất không
        int lowestScore = highscores.get(highscores.size() - 1).getScore();
        return score > lowestScore;
    }

    @SuppressWarnings("unchecked")
    private void loadHighscores() {
        File file = new File(HIGHSCORE_FILE);
        if (!file.exists()) {
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object loaded = ois.readObject();
            if (loaded instanceof List) {
                highscores = (List<HighscoreEntry>) loaded;
                // Đảm bảo danh sách được sắp xếp
                highscores.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading highscores: " + e.getMessage());
        }
    }

    private void saveHighscores() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(HIGHSCORE_FILE))) {
            oos.writeObject(highscores);
        } catch (IOException e) {
            System.err.println("Error saving highscores: " + e.getMessage());
        }
    }

    // === GAME PROGRESS METHODS ===
    public int getUnlockedLevels() {
        return gameProgress.getUnlockedLevels();
    }

    public void unlockLevel(int level) {
        int currentUnlocked = gameProgress.getUnlockedLevels();
        if (level > currentUnlocked) {
            gameProgress.setUnlockedLevels(level);
            saveGameProgress();
            System.out.println("🎉 Level " + level + " unlocked!");
        }
    }

    public void completeLevel(int level, int score) {
        // Đánh dấu level đã hoàn thành
        gameProgress.setLevelCompleted(level, true);

        // Lưu điểm cao nhất cho level
        int currentBest = gameProgress.getLevelScore(level);
        if (score > currentBest) {
            gameProgress.setLevelScore(level, score);
        }

        // Cập nhật tổng điểm
        gameProgress.setTotalScore(gameProgress.getTotalScore() + score);

        // Mở khóa level tiếp theo
        unlockLevel(level + 1);

        saveGameProgress();
        System.out.println("✅ Level " + level + " completed! Score: " + score);
    }

    public boolean isLevelCompleted(int level) {
        return gameProgress.isLevelCompleted(level);
    }

    public int getLevelScore(int level) {
        return gameProgress.getLevelScore(level);
    }

    public int getTotalScore() {
        return gameProgress.getTotalScore();
    }

    public void resetProgress() {
        gameProgress = new GameProgress();
        saveGameProgress();
        System.out.println("🔄 Game progress reset");
    }

    @SuppressWarnings("unchecked")
    private void loadGameProgress() {
        File file = new File(PROGRESS_FILE);
        if (!file.exists()) {
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object loaded = ois.readObject();
            if (loaded instanceof GameProgress) {
                gameProgress = (GameProgress) loaded;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading game progress: " + e.getMessage());
            gameProgress = new GameProgress(); // Fallback to default
        }
    }

    private void saveGameProgress() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(PROGRESS_FILE))) {
            oos.writeObject(gameProgress);
        } catch (IOException e) {
            System.err.println("Error saving game progress: " + e.getMessage());
        }
    }

    // === UTILITY METHODS ===
    public void resetHighscores() {
        highscores.clear();
        saveHighscores();
    }

    public void cleanup() {
        saveHighscores();
        saveGameProgress();
    }

    // === DEBUG METHODS ===
    public void printProgress() {
        System.out.println("=== GAME PROGRESS ===");
        System.out.println("Unlocked Levels: " + gameProgress.getUnlockedLevels());
        System.out.println("Total Score: " + gameProgress.getTotalScore());

        for (int i = 1; i <= Config.TOTAL_LEVELS; i++) {
            System.out.println("Level " + i + ": " +
                    (gameProgress.isLevelCompleted(i) ? "COMPLETED" : "NOT COMPLETED") +
                    " (Best: " + gameProgress.getLevelScore(i) + ")");
        }
    }
}