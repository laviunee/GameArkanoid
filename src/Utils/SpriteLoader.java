package Utils;

import javafx.scene.image.Image;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

public class SpriteLoader {
    private static SpriteLoader instance;
    private final Map<String, Image> spriteCache;

    private SpriteLoader() {
        spriteCache = new HashMap<>();
    }

    // Singleton
    public static SpriteLoader getInstance() {
        if (instance == null) {
            instance = new SpriteLoader();
        }
        return instance;
    }

    // Load sprite nhanh (không scale, không smooth)
    public Image loadSprite(String path) {
        return loadSprite(path, 0, 0, false, false);
    }

    // Load sprite với tùy chọn width/height và smooth
    public Image loadSprite(String path, double width, double height, boolean preserveRatio, boolean smooth) {
        String cacheKey = path + "_" + width + "x" + height + "_smooth:" + smooth;
        if (spriteCache.containsKey(cacheKey)) {
            return spriteCache.get(cacheKey);
        }

        try {
            java.io.InputStream is = getClass().getResourceAsStream(path);
            if (is == null) {
                return createPlaceholder((int) width, (int) height);
            }

            Image image;
            if (width > 0 && height > 0) {
                image = new Image(is, width, height, preserveRatio, smooth);
            } else {
                image = new Image(is);
            }

            spriteCache.put(cacheKey, image);
            return image;

        } catch (Exception e) {
            return createPlaceholder((int) width, (int) height);
        }
    }

    // Placeholder màu hồng khi sprite mất
    private Image createPlaceholder(int width, int height) {
        int w = Math.max(width, 50);
        int h = Math.max(height, 50);

        Canvas canvas = new Canvas(w, h);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(Color.MAGENTA);
        gc.fillRect(0, 0, w, h);

        return canvas.snapshot(null, null);
    }
}
