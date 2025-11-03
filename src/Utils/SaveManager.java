package Utils;

import java.io.*;
import java.util.*;

public class SaveManager {
    private static final String HIGHSCORE_FILE = "highscores.dat";
    private static final int MAX_HIGHSCORES = 10; // Sửa tên biến cho nhất quán
    private static SaveManager instance;
    private List<HighscoreEntry> highscores;

    private SaveManager() {
        this.highscores = new ArrayList<>();
        loadHighscores();
    }

    public static SaveManager getInstance() {
        if (instance == null) {
            instance = new SaveManager();
        }
        return instance;
    }

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

    public void resetHighscores() {
        highscores.clear();
    }

    public void cleanup() {
        saveHighscores();
    }
}