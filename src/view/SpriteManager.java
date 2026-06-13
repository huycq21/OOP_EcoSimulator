package view;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class SpriteManager {

    private static final Map<String, BufferedImage> sprites = new HashMap<>();

    private SpriteManager() {}

    public static void loadSprite(String key, String path) {
        File file = new File(path);

        if (!file.exists()) {
            return;
        }

        try {
            sprites.put(key, ImageIO.read(file));
        }
        catch (IOException e) {
            System.err.println("Cannot load sprite: " + path);
        }
    }

    public static BufferedImage get(String key) {
        return sprites.get(key);
    }

    public static boolean contains(String key) {
        return sprites.containsKey(key);
    }
}