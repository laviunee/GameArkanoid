package Engine;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp đại diện cho một animation sequence
 */
public class Animation {
    private List<Image> frames;
    private double frameDuration;
    private double currentTime;
    private int currentFrame;
    private boolean playing;
    private boolean loop;
    private double x, y;
    private double width, height;

    public Animation(List<Image> frames, double frameDuration, boolean loop) {
        this.frames = new ArrayList<>(frames);
        this.frameDuration = frameDuration;
        this.loop = loop;
        this.playing = false;
        this.currentFrame = 0;
        this.currentTime = 0;
    }

    public Animation(Image[] frames, double frameDuration, boolean loop) {
        this.frames = new ArrayList<>();
        for (Image frame : frames) {
            this.frames.add(frame);
        }
        this.frameDuration = frameDuration;
        this.loop = loop;
        this.playing = false;
        this.currentFrame = 0;
        this.currentTime = 0;
    }

    public void start(double x, double y) {
        this.x = x;
        this.y = y;
        this.playing = true;
        this.currentFrame = 0;
        this.currentTime = 0;
    }

    public void update(double deltaTime) {
        if (!playing) return;

        currentTime += deltaTime;
        if (currentTime >= frameDuration) {
            currentTime = 0;
            currentFrame++;

            if (currentFrame >= frames.size()) {
                if (loop) {
                    currentFrame = 0;
                } else {
                    playing = false;
                }
            }
        }
    }

    public void render(GraphicsContext gc) {
        if (!playing || frames.isEmpty()) return;

        Image currentImage = frames.get(currentFrame);
        if (currentImage != null) {
            if (width > 0 && height > 0) {
                gc.drawImage(currentImage, x, y, width, height);
            } else {
                gc.drawImage(currentImage, x, y);
            }
        }
    }

    public void stop() {
        playing = false;
    }

    // Getters & Setters
    public boolean isPlaying() { return playing; }
    public void setSize(double width, double height) {
        this.width = width;
        this.height = height;
    }
    public double getX() { return x; }
    public double getY() { return y; }
    public int getCurrentFrame() { return currentFrame; }
    public int getTotalFrames() { return frames.size(); }
}