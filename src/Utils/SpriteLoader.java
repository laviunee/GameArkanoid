package Utils;

import javafx.scene.image.Image;
import java.util.HashMap;
import java.util.Map;

public class SpriteLoader {
    private static SpriteLoader instance;
    private Map<String, Image> spriteCache;

    private SpriteLoader() {
        spriteCache = new HashMap<>();
    }

    public static SpriteLoader getInstance() {
        if (instance == null) {
            instance = new SpriteLoader();
        }
        return instance;
    }

    public Image loadSprite(String path) {
        return loadSprite(path, 0, 0, false, false);
    }

    public Image loadSprite(String path, double width, double height, boolean preserveRatio, boolean smooth) {
        String cacheKey = path + "_" + width + "x" + height + "_smooth:" + smooth;
        if (spriteCache.containsKey(cacheKey)) {
            return spriteCache.get(cacheKey);
        }

        try {
            System.out.println("Loading: " + path + " [smooth: " + smooth + "]");

            java.io.InputStream is = getClass().getResourceAsStream(path);
            if (is == null) {
                System.err.println("InputStream is NULL for: " + path);
                return createPlaceholder((int)width, (int)height);
            }

            Image image;
            if (width > 0 && height > 0) {
                image = new Image(is, width, height, preserveRatio, smooth); // smooth parameter here
            } else {
                image = new Image(is); // Không scale - giữ nguyên chất lượng
            }

            System.out.println("Loaded: " + path + " [" + image.getWidth() + "x" + image.getHeight() + "]");
            spriteCache.put(cacheKey, image);
            return image;

        } catch (Exception e) {
            System.err.println("Exception loading: " + path);
            return createPlaceholder((int)width, (int)height);
        }
    }

    private Image createPlaceholder(int width, int height) {
        int w = Math.max(width, 50);
        int h = Math.max(height, 50);

        javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(w, h);
        javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(javafx.scene.paint.Color.MAGENTA);
        gc.fillRect(0, 0, w, h);
        gc.setStroke(javafx.scene.paint.Color.BLACK);
        gc.strokeRect(0, 0, w, h);
        gc.setFill(javafx.scene.paint.Color.WHITE);
        gc.fillText("IMG", w/2-10, h/2+5);

        return canvas.snapshot(null, null);
    }

    public void clearCache() {
        spriteCache.clear();
    }
}