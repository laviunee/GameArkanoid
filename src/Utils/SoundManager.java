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
    private MediaPlayer menuMusic;      // ← NHẠC MENU
    private MediaPlayer backgroundMusic; // ← NHẠC GAME
    private boolean soundEnabled = true;
    private boolean initialized = false;
    private boolean inGame = false;
    private double soundVolume = 0.7;
    private double musicVolume = 0.5;

    private SoundManager() {
        soundEffects = new HashMap<>();
    }


    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    public void initialize() {
        if (initialized) return;

        try {
            System.out.println("Bắt đầu khởi tạo SoundManager...");
            loadSounds();
            initialized = true;
            System.out.println("SoundManager initialized successfully - " +
                    soundEffects.size() + " sounds loaded");

        } catch (Exception e) {
            System.err.println("Sound initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadSounds() {
        try {
            System.out.println("Loading sounds from /sounds/ directory...");

            // Load hiệu ứng âm thanh
            loadSoundEffect("hit", "/sounds/hit.wav");
            loadSoundEffect("break", "/sounds/break.wav");
            loadSoundEffect("powerup", "/sounds/powerup.wav");
            loadSoundEffect("lose", "/sounds/lose.wav");
            loadSoundEffect("win", "/sounds/win.wav");
            loadSoundEffect("paddle_hit", "/sounds/paddle_hit.wav");
            loadSoundEffect("game_start", "/sounds/game_start.wav");

            // Load nhạc menu và background
            loadMenuMusic("/sounds/menu_music.wav");
            loadBackgroundMusic("/sounds/game_music.wav");

            System.out.println("Total sounds loaded: " + soundEffects.size());

        } catch (Exception e) {
            System.err.println("Lỗi load sounds: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadSoundEffect(String soundName, String filePath) {
        try {
            System.out.println("Loading sound effect: " + soundName);

            URL soundUrl = getClass().getResource(filePath);
            if (soundUrl != null) {
                AudioClip audioClip = new AudioClip(soundUrl.toString());
                audioClip.setVolume(soundVolume);
                soundEffects.put(soundName, audioClip);
                System.out.println("SUCCESS: Loaded " + soundName);
            } else {
                System.err.println("FAILED: File not found - " + filePath);
            }
        } catch (Exception e) {
            System.err.println("ERROR loading " + soundName + ": " + e.getMessage());
        }
    }

    private void loadMenuMusic(String filePath) {
        try {
            System.out.println("Loading menu music...");

            URL musicUrl = getClass().getResource(filePath);
            if (musicUrl != null) {
                Media media = new Media(musicUrl.toString());
                menuMusic = new MediaPlayer(media);
                menuMusic.setCycleCount(MediaPlayer.INDEFINITE); // Lặp vô hạn
                menuMusic.setVolume(musicVolume);
                System.out.println("Menu music loaded successfully");
            } else {
                System.err.println("Menu music file not found: " + filePath);
            }
        } catch (Exception e) {
            System.err.println("Error loading menu music: " + e.getMessage());
        }
    }

    private void loadBackgroundMusic(String filePath) {
        try {
            System.out.println("Loading background music...");

            URL musicUrl = getClass().getResource(filePath);
            if (musicUrl != null) {
                Media media = new Media(musicUrl.toString());
                backgroundMusic = new MediaPlayer(media);
                backgroundMusic.setCycleCount(MediaPlayer.INDEFINITE); // Lặp vô hạn
                backgroundMusic.setVolume(musicVolume);
                System.out.println("Background music loaded successfully");
            } else {
                System.err.println("Background music file not found: " + filePath);
            }
        } catch (Exception e) {
            System.err.println("Error loading background music: " + e.getMessage());
        }
    }

    // ==================== PUBLIC METHODS ====================

    public void playSound(String soundName) {
        if (!soundEnabled || !initialized) return;

        AudioClip audioClip = soundEffects.get(soundName);
        if (audioClip != null) {
            try {
                audioClip.play();
                System.out.println("Playing sound: " + soundName);
            } catch (Exception e) {
                System.err.println("Error playing " + soundName + ": " + e.getMessage());
            }
        }
    }

    // ← MENU MUSIC METHODS
    public void playMenuMusic() {
        if (!soundEnabled || !initialized || menuMusic == null) return;

        try {
            // Dừng background music nếu đang phát
            stopBackgroundMusic();
            menuMusic.play();
            System.out.println("Menu music started");
        } catch (Exception e) {
            System.err.println("Lỗi starting menu music: " + e.getMessage());
        }
    }

    public void stopMenuMusic() {
        if (menuMusic != null) {
            menuMusic.stop();
            System.out.println("Menu music stopped");
        }
    }

    // ← BACKGROUND MUSIC METHODS
    public void playBackgroundMusic() {
        if (!soundEnabled || !initialized || backgroundMusic == null) return;

        try {
            // Dừng menu music nếu đang phát
            stopMenuMusic();
            backgroundMusic.play();
            System.out.println("Background music started");
        } catch (Exception e) {
            System.err.println("Lỗi starting background music: " + e.getMessage());
        }
    }

    public void stopBackgroundMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
            System.out.println("Background music stopped");
        }
    }

    // ← GAME EVENT METHODS
    public void onGameStart() {
        System.out.println("Game starting...");
        inGame = true;
        stopAllSounds();           // Dừng toàn bộ âm thanh trước đó
        if (soundEnabled && initialized) {
            playSound("game_start");   // Phát hiệu ứng bắt đầu
            playBackgroundMusic();     // Phát lại nhạc nền game
        }
    }


    public void onGameOver() {
        System.out.println("Game over...");
        inGame = false;
        stopBackgroundMusic();
        stopMenuMusic();
        if (soundEnabled && initialized) {
            AudioClip clip = soundEffects.get("lose");
            if (clip != null) {
                clip.stop(); // đảm bảo không chồng
                clip.play();
                System.out.println("Playing lose sound...");
            }
        }
    }

    public void onGameWin() {
        System.out.println("Game win!");
        inGame = false;
        stopBackgroundMusic();
        stopMenuMusic();
        if (soundEnabled && initialized) {
            AudioClip clip = soundEffects.get("win");
            if (clip != null) {
                clip.stop();
                clip.play();
                System.out.println("Playing win sound...");
            }
        }
    }

    public void onReturnToMenu() {
        System.out.println("Returning to menu...");
        inGame = false;
        stopBackgroundMusic();     // Dừng nhạc game
        playMenuMusic();           // Phát nhạc menu
        if (soundEnabled && initialized) {
            playMenuMusic();
        }
    }

    // ==================== GETTERS/SETTERS ====================

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;

        if (!enabled) {
            stopAllSounds();
            System.out.println("Sound disabled");
        } else {
            System.out.println("Sound enabled");
            // Phát lại loại nhạc phù hợp với trạng thái hiện tại
            if (inGame) {
                if (backgroundMusic != null) {
                    playBackgroundMusic();
                } else {
                    // fallback: nếu background không có, play menu
                    playMenuMusic();
                }
            } else {
                playMenuMusic();
            }
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
        System.out.println("Sound volume set to: " + (int)(soundVolume * 100) + "%");
    }

    public double getMusicVolume() {
        return musicVolume;
    }

    public void setMusicVolume(double volume) {
        this.musicVolume = Math.max(0.0, Math.min(1.0, volume));
        if (menuMusic != null) menuMusic.setVolume(musicVolume);
        if (backgroundMusic != null) backgroundMusic.setVolume(musicVolume);
        System.out.println("Music volume set to: " + (int)(musicVolume * 100) + "%");
    }

    private void stopAllSounds() {
        for (AudioClip clip : soundEffects.values()) {
            clip.stop();
        }
        stopMenuMusic();
        stopBackgroundMusic();
    }

    public void cleanup() {
        stopAllSounds();
        if (menuMusic != null) menuMusic.dispose();
        if (backgroundMusic != null) backgroundMusic.dispose();
        System.out.println("SoundManager cleaned up");
    }

    public void printStatus() {
        System.out.println("=== SOUND MANAGER STATUS ===");
        System.out.println("Initialized: " + initialized);
        System.out.println("Sound Enabled: " + soundEnabled);
        System.out.println("Sounds Loaded: " + soundEffects.size());
        System.out.println("Menu Music: " + (menuMusic != null ? "LOADED" : "NULL"));
        System.out.println("Background Music: " + (backgroundMusic != null ? "LOADED" : "NULL"));
        System.out.println("Available Sounds: " + soundEffects.keySet());
        System.out.println("============================");
    }
}