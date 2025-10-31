package Engine;

import javafx.scene.canvas.GraphicsContext;
import java.util.HashMap;
import java.util.Map;

/**
 * Quản lý hiệu ứng animation trong game (phá gạch, power-up, explosion...)
 */
public class AnimationManager {
    private static AnimationManager instance;
    private Map<String, Animation> animations;
    private boolean enabled = true;

    // Singleton pattern
    public static AnimationManager getInstance() {
        if (instance == null) {
            instance = new AnimationManager();
        }
        return instance;
    }

    private AnimationManager() {
        this.animations = new HashMap<>();
    }

    /**
     * Thêm animation mới vào manager
     */
    public void addAnimation(String id, Animation animation) {
        animations.put(id, animation);
    }

    /**
     * Bắt đầu chạy animation
     */
    public void playAnimation(String id, double x, double y) {
        if (!enabled) return;

        Animation animation = animations.get(id);
        if (animation != null) {
            animation.start(x, y);
        }
    }

    /**
     * Cập nhật tất cả animations
     */
    public void update(double deltaTime) {
        if (!enabled) return;

        for (Animation animation : animations.values()) {
            if (animation.isPlaying()) {
                animation.update(deltaTime);
            }
        }
    }

    /**
     * Vẽ tất cả animations đang chạy
     */
    public void render(GraphicsContext gc) {
        if (!enabled) return;

        for (Animation animation : animations.values()) {
            if (animation.isPlaying()) {
                animation.render(gc);
            }
        }
    }

    /**
     * Dừng tất cả animations
     */
    public void stopAll() {
        for (Animation animation : animations.values()) {
            animation.stop();
        }
    }

    /**
     * Xóa animation
     */
    public void removeAnimation(String id) {
        animations.remove(id);
    }

    /**
     * Kiểm tra animation có đang chạy không
     */
    public boolean isAnimationPlaying(String id) {
        Animation animation = animations.get(id);
        return animation != null && animation.isPlaying();
    }

    // Getter/Setter
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void clear() { animations.clear(); }
}