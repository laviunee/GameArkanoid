package Utils;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.util.HashMap;
import java.util.Map;
import java.net.URL;

public class SoundManager {
    private static SoundManager instance;
    private Map<String, AudioClip> soundEffects;
    private MediaPlayer menuMusic;
    private MediaPlayer backgroundMusic;
    private boolean soundEnabled = true;
    private boolean initialized = false;
    private boolean inGame = false;
    private double soundVolume = 0.7;
    private double musicVolume = 0.5;

    private SoundManager() {
        soundEffects = new HashMap<>();
    }

    public static synchronized SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    public void initialize() {
        if (initialized) return;

        try {
            System.out.println("Initializing SoundManager...");
            loadSounds();
            initialized = true;
            System.out.println("SoundManager initialized - " + soundEffects.size() + " sounds loaded");

            // Tự động phát nhạc menu khi khởi tạo
            playMenuMusic();

        } catch (Exception e) {
            System.err.println("Sound initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadSounds() {
        try {
            System.out.println("Loading sounds...");

            // Load sound effects
            loadSoundEffect("hit", "/sounds/hit.wav");
            loadSoundEffect("break", "/sounds/break.wav");
            loadSoundEffect("powerup", "/sounds/powerup.wav");
            loadSoundEffect("lose", "/sounds/lose.wav");
            loadSoundEffect("win", "/sounds/win.wav");
            loadSoundEffect("paddle_hit", "/sounds/paddle_hit.wav");
            loadSoundEffect("game_start", "/sounds/game_start.wav");
            loadSoundEffect("lose_life", "/sounds/lose.wav");


            // Load music
            loadMenuMusic("/sounds/menu_music.wav");
            loadBackgroundMusic("/sounds/game_music.wav");

        } catch (Exception e) {
            System.err.println("Error loading sounds: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadSoundEffect(String soundName, String filePath) {
        try {
            URL soundUrl = getClass().getResource(filePath);
            if (soundUrl != null) {
                AudioClip audioClip = new AudioClip(soundUrl.toString());
                audioClip.setVolume(soundVolume);
                soundEffects.put(soundName, audioClip);
                System.out.println("Loaded sound: " + soundName);
            } else {
                System.err.println("Sound file not found: " + filePath);
            }
        } catch (Exception e) {
            System.err.println("Error loading " + soundName + ": " + e.getMessage());
        }
    }

    private void loadMenuMusic(String filePath) {
        try {
            URL musicUrl = getClass().getResource(filePath);
            if (musicUrl != null) {
                Media media = new Media(musicUrl.toString());
                menuMusic = new MediaPlayer(media);
                menuMusic.setCycleCount(MediaPlayer.INDEFINITE);
                menuMusic.setVolume(musicVolume);

                // Add error listener
                menuMusic.setOnError(() ->
                        System.err.println("Menu music error: " + menuMusic.getError().getMessage()));

                System.out.println("Menu music loaded");
            } else {
                System.err.println("Menu music file not found: " + filePath);
            }
        } catch (Exception e) {
            System.err.println("Error loading menu music: " + e.getMessage());
        }
    }

    private void loadBackgroundMusic(String filePath) {
        try {
            URL musicUrl = getClass().getResource(filePath);
            if (musicUrl != null) {
                Media media = new Media(musicUrl.toString());
                backgroundMusic = new MediaPlayer(media);
                backgroundMusic.setCycleCount(MediaPlayer.INDEFINITE);
                backgroundMusic.setVolume(musicVolume);

                // Add status listeners for debugging
                backgroundMusic.setOnPlaying(() -> System.out.println("Background music: PLAYING"));
                backgroundMusic.setOnStopped(() -> System.out.println("Background music: STOPPED"));
                backgroundMusic.setOnError(() ->
                        System.err.println("Background music error: " + backgroundMusic.getError().getMessage()));

                System.out.println("Background music loaded");
            } else {
                System.err.println("Background music file not found: " + filePath);
            }
        } catch (Exception e) {
            System.err.println("Error loading background music: " + e.getMessage());
        }
    }

    // ==================== SOUND EFFECTS ====================

    public void playSound(String soundName) {
        if (!soundEnabled || !initialized) return;

        AudioClip audioClip = soundEffects.get(soundName);
        if (audioClip != null) {
            try {
                audioClip.play();
            } catch (Exception e) {
                System.err.println("Error playing " + soundName + ": " + e.getMessage());
            }
        } else {
            System.err.println("Sound not found: " + soundName);
        }
    }

    // ==================== MUSIC CONTROL ====================

    public void playMenuMusic() {
        if (!soundEnabled || !initialized || menuMusic == null) return;

        try {
            // Nếu đang phát thì khỏi làm gì
            if (menuMusic.getStatus() == MediaPlayer.Status.PLAYING) {
                return;
            }

            // Nếu đang pause → resume luôn (không seek lại)
            if (menuMusic.getStatus() == MediaPlayer.Status.PAUSED) {
                menuMusic.play();
                System.out.println("Menu music resumed");
                return;
            }

            // Dừng nhạc nền của game nếu đang phát
            if (backgroundMusic != null &&
                    backgroundMusic.getStatus() == MediaPlayer.Status.PLAYING) {
                backgroundMusic.stop();
            }

            // Nếu chưa phát bao giờ thì mới seek về đầu
            menuMusic.play();
            System.out.println("Menu music started (continued)");

        } catch (Exception e) {
            System.err.println("Error starting menu music: " + e.getMessage());
        }
    }


    public void playBackgroundMusic() {
        if (!soundEnabled || !initialized || backgroundMusic == null) return;

        try {
            // Stop menu music first
            if (menuMusic != null) {
                menuMusic.pause();
            }

            // Play background music
            backgroundMusic.seek(javafx.util.Duration.ZERO);
            backgroundMusic.play();
            System.out.println("Background music started");

        } catch (Exception e) {
            System.err.println("Error starting background music: " + e.getMessage());
        }
    }

    public void stopMenuMusic() {
        if (menuMusic != null && menuMusic.getStatus() == MediaPlayer.Status.PLAYING) {
            menuMusic.stop();
            System.out.println("Menu music stopped");
        }
    }

    public void stopBackgroundMusic() {
        if (backgroundMusic != null && backgroundMusic.getStatus() == MediaPlayer.Status.PLAYING) {
            backgroundMusic.stop();
            System.out.println("Background music stopped");
        }
    }

    // ==================== GAME EVENTS ====================

    public void onGameStart() {
        System.out.println("=== GAME START ===");
        inGame = true;

        if (soundEnabled && initialized) {
            // Play start sound and switch to game music
            playSound("game_start");
            playBackgroundMusic(); // QUAN TRỌNG: Chuyển sang nhạc game
        }
    }

    public void onGameOver() {
        System.out.println("=== GAME OVER ===");
        inGame = false;

        if (soundEnabled && initialized) {
            stopBackgroundMusic();
            playSound("lose");
        }
    }

    public void onGameWin() {
        System.out.println("=== GAME WIN ===");
        inGame = false;

        if (soundEnabled && initialized) {
            stopBackgroundMusic();
            playSound("win");
        }
    }

    public void onLoseLife() {
        System.out.println("=== LOSE ONE LIFE ===");

        if (soundEnabled && initialized) {
            playSound("lose_life"); // phát nhạc lose tạm
        }
    }


    public void onReturnToMenu() {
        System.out.println("=== RETURN TO MENU ===");
        inGame = false;

        if (soundEnabled && initialized) {
            stopBackgroundMusic();
            playMenuMusic(); // Chuyển về nhạc menu
        }
    }

    // ==================== SETTINGS ====================

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;

        if (enabled) {
            System.out.println("Sound enabled");
            // Resume appropriate music based on game state
            if (inGame) {
                playBackgroundMusic();
            } else {
                playMenuMusic();
            }
        } else {
            System.out.println("Sound disabled");
            stopAllSounds();
        }
    }


    public void toggleSound() {
        setSoundEnabled(!soundEnabled);
    }

    public double getSoundVolume() {
        return soundVolume;
    }

    public void setSoundVolume(double volume) {
        this.soundVolume = Math.max(0.0, Math.min(1.0, volume));
        for (AudioClip clip : soundEffects.values()) {
            clip.setVolume(soundVolume);
        }
        System.out.println("Sound volume: " + (int) (soundVolume * 100) + "%");
    }

    public double getMusicVolume() {
        return musicVolume;
    }

    public void setMusicVolume(double volume) {
        this.musicVolume = Math.max(0.0, Math.min(1.0, volume));
        if (menuMusic != null) menuMusic.setVolume(musicVolume);
        if (backgroundMusic != null) backgroundMusic.setVolume(musicVolume);
        System.out.println("Music volume: " + (int) (musicVolume * 100) + "%");
    }

    // ==================== UTILITY ====================

    public void stopAllSounds() {
        // Stop all sound effects
        for (AudioClip clip : soundEffects.values()) {
            clip.stop();
        }

        // Stop all music
        stopMenuMusic();
        stopBackgroundMusic();
    }

    public void cleanup() {
        stopAllSounds();
        if (menuMusic != null) menuMusic.dispose();
        if (backgroundMusic != null) backgroundMusic.dispose();
        soundEffects.clear();
        System.out.println("SoundManager cleaned up");
    }

    // ==================== DEBUG ====================

    public void printStatus() {
        System.out.println("=== SOUND MANAGER STATUS ===");
        System.out.println("Initialized: " + initialized);
        System.out.println("Sound Enabled: " + soundEnabled);
        System.out.println("In Game: " + inGame);
        System.out.println("Sounds Loaded: " + soundEffects.size());
        System.out.println("Menu Music: " + (menuMusic != null ?
                menuMusic.getStatus() + " (vol: " + menuMusic.getVolume() + ")" : "NULL"));
        System.out.println("Background Music: " + (backgroundMusic != null ?
                backgroundMusic.getStatus() + " (vol: " + backgroundMusic.getVolume() + ")" : "NULL"));
        System.out.println("Available Sounds: " + soundEffects.keySet());
        System.out.println("============================");

    }

    public void onLevelComplete() {
        if (isSoundEnabled()) {
            // Có thể thêm âm thanh level complete ở đây
            System.out.println("🎵 Level complete sound");
        }
    }
}