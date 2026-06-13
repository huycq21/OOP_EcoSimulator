package view;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import model.Entity;
import model.Animal;
import model.AnimalState;
import model.plant.GrowthStage;

public class SpriteSheetStore {
    private final Map<String, EntitySpriteSet> entitySprites;
    private final Map<String, PlantSpriteSet> plantSprites;

    // Build sprites (exposed via getters)
    private BufferedImage grassSprite;
    private BufferedImage bushSprite;
    private BufferedImage treeSprite;
    private BufferedImage rockSprite;

    public SpriteSheetStore() {
        entitySprites = new HashMap<>();
        plantSprites = new HashMap<>();
        loadSprites();
        loadPlantSprites();
        loadBuildSprites();
    }

    // ===================== Inner types =====================

    private static class PlantSpriteSet {
        private final Map<GrowthStage, BufferedImage> images = new EnumMap<>(GrowthStage.class);
        private void put(GrowthStage stage, BufferedImage image) { if (image != null) images.put(stage, image); }
        private BufferedImage get(GrowthStage stage) {
            BufferedImage img = images.get(stage);
            if (img != null) return img;
            GrowthStage[] all = GrowthStage.values();
            int idx = stage.ordinal();
            for (int i = idx - 1; i >= 0; i--)       { img = images.get(all[i]); if (img != null) return img; }
            for (int i = idx + 1; i < all.length; i++) { img = images.get(all[i]); if (img != null) return img; }
            return null;
        }
        private boolean hasAnyImage() { return !images.isEmpty(); }
    }

    private static class EntitySpriteSet {
        private SpriteSheet idle, walk, run, attack, hurt, death;
        private BufferedImage shadow;
        private boolean directionFramesInFirstRow;
        private boolean hasAnyAnimation() { return idle != null || walk != null || run != null || attack != null || hurt != null || death != null; }
    }

    private static class SpriteSheet {
        private final BufferedImage image;
        private final int columns, rows, frameWidth, frameHeight;

        private SpriteSheet(BufferedImage image, int rows) {
            this.image = image; this.rows = rows;
            this.frameHeight = image.getHeight() / rows;
            this.frameWidth = frameHeight;
            this.columns = Math.max(1, image.getWidth() / frameWidth);
        }
        private SpriteSheet(BufferedImage image, int rows, int columns) {
            this.image = image; this.rows = rows; this.columns = columns;
            this.frameWidth = image.getWidth() / columns;
            this.frameHeight = image.getHeight() / rows;
        }
        private SpriteSheet(BufferedImage image, int frameWidth, int frameHeight, boolean fixedFrameSize) {
            this.image = image; this.frameWidth = frameWidth; this.frameHeight = frameHeight;
            this.columns = Math.max(1, image.getWidth() / frameWidth);
            this.rows    = Math.max(1, image.getHeight() / frameHeight);
        }
        private BufferedImage getFrame(int column, int row) {
            int sc = Math.max(0, Math.min(column, columns - 1));
            int sr = Math.max(0, Math.min(row,    rows - 1));
            return image.getSubimage(sc * frameWidth, sr * frameHeight, frameWidth, frameHeight);
        }
    }

    // ===================== Entity sprite loading =====================

    private void loadSprites() {
        loadEntitySpriteSet("hare",        "Hare");
        loadEntitySpriteSet("fox",         "Fox");
        loadEntitySpriteSet("boar",        "Boar");
        loadEntitySpriteSet("deer",        "Deer");
        loadEntitySpriteSet("black_grouse","BlackGrouse");
        loadDirectionalSpriteSet("bear",    "Bear");
        loadDirectionalSpriteSet("cheetah", "Cheetah");
        loadDirectionalSpriteSet("goat",    "Goat");
        loadDirectionalSpriteSet("horse",   "Horse");
        loadDirectionalSpriteSet("lion",    "Lion");
        loadDirectionalSpriteSet("human",   "Farmer");
        loadColumnAnimationSpriteSet("chicken", "assets/Entities/Chicken/Chicken_animation.png", 8);
        loadColumnAnimationSpriteSet("cow",     "assets/Entities/Cow/Cow_animation.png",         8);
        loadColumnAnimationSpriteSet("pig",     "assets/Entities/Pig/Pig_animation.png",         8);
        loadFishSpriteSet("fish_one",   "Fish1_animation.png");
        loadFishSpriteSet("fish_two",   "Fish2_animation.png");
        loadFishSpriteSet("fish_three", "Fish3_animation.png");
        loadFishSpriteSet("fish_four",  "Fish4_animation.png");
        SpriteManager.loadSprite("rabbit", "assets/Entities/Rabbit.png");
        SpriteManager.loadSprite("wolf",   "assets/Entities/Wolf.png");
    }

    private void loadEntitySpriteSet(String key, String assetName) {
        String fp = "assets/Entities/" + assetName;
        EntitySpriteSet set = new EntitySpriteSet();
        set.idle   = loadSpriteSheet(fp + "/" + assetName + "Idle.png",   4);
        set.walk   = loadSpriteSheet(fp + "/" + assetName + "Walk.png",   4);
        set.run    = loadSpriteSheet(fp + "/" + assetName + "Run.png",    4);
        set.attack = loadSpriteSheet(fp + "/" + assetName + "Attack.png", 4);
        set.hurt   = loadSpriteSheet(fp + "/" + assetName + "Hurt.png",   4);
        set.death  = loadSpriteSheet(fp + "/" + assetName + "Death.png",  4);
        set.shadow = loadImage(fp + "/" + assetName + "Shadow.png");
        if (set.hasAnyAnimation()) entitySprites.put(key, set);
    }

    private void loadDirectionalSpriteSet(String key, String assetName) {
        String fp = "assets/Entities/" + assetName + "/" + assetName;
        EntitySpriteSet set = new EntitySpriteSet();
        set.idle = buildDirectionalSpriteSheet(fp + "/rotations", null);
        set.walk = buildDirectionalSpriteSheet(fp + "/animations/Walk", "frame_");
        if (set.walk == null) set.walk = set.idle;
        if (set.hasAnyAnimation()) entitySprites.put(key, set);
    }

    private void loadFishSpriteSet(String key, String fileName) {
        EntitySpriteSet set = new EntitySpriteSet();
        set.walk = loadSpriteSheet("assets/Entities/Firsh/" + fileName, 1, 9);
        set.idle = set.walk;
        if (set.hasAnyAnimation()) entitySprites.put(key, set);
    }

    private void loadColumnAnimationSpriteSet(String key, String path, int columns) {
        EntitySpriteSet set = new EntitySpriteSet();
        set.walk = loadColumnSpriteSheet(path, columns);
        set.idle = set.walk;
        set.directionFramesInFirstRow = true;
        if (set.hasAnyAnimation()) entitySprites.put(key, set);
    }

    // ===================== Plant sprite loading =====================

    private void loadPlantSprites() {
        String plantsPath = "assets/Environment/Forest/Plants2.png";

        PlantSpriteSet smallTree = new PlantSpriteSet();
        smallTree.put(GrowthStage.SEED,   cropImage(plantsPath, 0,   0,  16, 16));
        smallTree.put(GrowthStage.SPROUT, cropImage(plantsPath, 16,  0,  16, 32));
        smallTree.put(GrowthStage.YOUNG,  cropImage(plantsPath, 32,  0,  32, 32));
        smallTree.put(GrowthStage.MATURE, cropImage(plantsPath, 96,  0,  48, 48));
        smallTree.put(GrowthStage.OLD,    cropImage(plantsPath, 144, 0,  64, 64));
        if (smallTree.hasAnyImage()) { plantSprites.put("small_tree", smallTree); plantSprites.put("oak", smallTree); }

        PlantSpriteSet pine = new PlantSpriteSet();
        pine.put(GrowthStage.SEED,   cropImage(plantsPath, 0,   80, 16, 16));
        pine.put(GrowthStage.SPROUT, cropImage(plantsPath, 16,  80, 16, 16));
        pine.put(GrowthStage.YOUNG,  cropImage(plantsPath, 80,  80, 32, 32));
        pine.put(GrowthStage.MATURE, cropImage(plantsPath, 128, 80, 48, 48));
        pine.put(GrowthStage.OLD,    cropImage(plantsPath, 176, 80, 64, 64));
        if (pine.hasAnyImage()) plantSprites.put("pine", pine);

        PlantSpriteSet palm = new PlantSpriteSet();
        palm.put(GrowthStage.SEED,   cropImage(plantsPath, 112, 208, 16, 16));
        palm.put(GrowthStage.SPROUT, cropImage(plantsPath, 144, 192, 32, 32));
        palm.put(GrowthStage.YOUNG,  cropImage(plantsPath, 192, 176, 48, 48));
        palm.put(GrowthStage.MATURE, cropImage(plantsPath, 256, 160, 64, 64));
        palm.put(GrowthStage.OLD,    cropImage(plantsPath, 336, 160, 64, 64));
        if (palm.hasAnyImage()) plantSprites.put("palm", palm);

        String vinePath = "assets/Environment/Forest/Vine.png";
        PlantSpriteSet vine = new PlantSpriteSet();
        vine.put(GrowthStage.SEED,   cropImage(vinePath, 0,  0, 32, 48));
        vine.put(GrowthStage.SPROUT, cropImage(vinePath, 32, 0, 32, 48));
        vine.put(GrowthStage.YOUNG,  cropImage(vinePath, 64, 0, 32, 48));
        vine.put(GrowthStage.MATURE, cropImage(vinePath, 96, 0, 32, 48));
        vine.put(GrowthStage.OLD,    cropImage(vinePath, 96, 0, 32, 48));
        if (vine.hasAnyImage()) { plantSprites.put("vine", vine); plantSprites.put("nho", vine); }
    }

    // ===================== Build sprite loading =====================

    private void loadBuildSprites() {
        BufferedImage plants  = loadImage("assets/Environment/Forest/Plants.png");
        BufferedImage objects = loadImage("assets/Environment/Forest/Objects.png");

        if (plants != null && objects != null) {
            grassSprite = safeSubimage(objects, 16,  464, 48, 48);
            bushSprite  = safeSubimage(objects, 448, 80,  48, 48);
            treeSprite  = safeSubimage(objects, 0,   0,   80, 80);
        }
        if (objects != null) {
            rockSprite = safeSubimage(objects, 400, 176, 48, 48);
        }
    }

    // Getters cho build sprites
    public BufferedImage getGrassSprite() { return grassSprite; }
    public BufferedImage getBushSprite()  { return bushSprite; }
    public BufferedImage getTreeSprite()  { return treeSprite; }
    public BufferedImage getRockSprite()  { return rockSprite; }

    // ===================== Sheet loaders =====================

    private SpriteSheet loadSpriteSheet(String path, int rows) {
        if (path == null) return null;
        File file = new File(path);
        if (!file.exists()) return null;
        try { return new SpriteSheet(ImageIO.read(file), rows); }
        catch (IOException e) { System.err.println("Cannot load spritesheet: " + path); return null; }
    }

    private SpriteSheet loadSpriteSheet(String path, int rows, int columns) {
        if (path == null) return null;
        File file = new File(path);
        if (!file.exists()) return null;
        try { return new SpriteSheet(ImageIO.read(file), rows, columns); }
        catch (IOException e) { System.err.println("Cannot load spritesheet: " + path); return null; }
    }

    private SpriteSheet loadFixedFrameSpriteSheet(String path, int frameWidth, int frameHeight) {
        if (path == null) return null;
        File file = new File(path);
        if (!file.exists()) return null;
        try { return new SpriteSheet(ImageIO.read(file), frameWidth, frameHeight, true); }
        catch (IOException e) { System.err.println("Cannot load spritesheet: " + path); return null; }
    }

    private SpriteSheet loadColumnSpriteSheet(String path, int columns) {
        if (path == null) return null;
        File file = new File(path);
        if (!file.exists()) return null;
        try {
            BufferedImage image = ImageIO.read(file);
            int fw = Math.max(1, image.getWidth() / columns);
            int rows = Math.max(1, image.getHeight() / fw);
            return new SpriteSheet(image, rows, columns);
        } catch (IOException e) { System.err.println("Cannot load spritesheet: " + path); return null; }
    }

    private SpriteSheet buildDirectionalSpriteSheet(String basePath, String framePrefix) {
        String[] directions = {"south", "north", "west", "east"};
        BufferedImage[][] frames = new BufferedImage[directions.length][];
        int maxColumns = 0, frameWidth = 0, frameHeight = 0;

        for (int row = 0; row < directions.length; row++) {
            BufferedImage[] dirFrames = framePrefix == null
                    ? loadRotationFrame(basePath, directions[row])
                    : loadAnimationFrames(basePath, directions[row], framePrefix);
            if (dirFrames.length == 0) return null;
            frames[row] = dirFrames;
            maxColumns = Math.max(maxColumns, dirFrames.length);
            if (frameWidth == 0) { frameWidth = dirFrames[0].getWidth(); frameHeight = dirFrames[0].getHeight(); }
        }

        BufferedImage sheet = new BufferedImage(frameWidth * maxColumns, frameHeight * directions.length, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = sheet.createGraphics();
        for (int row = 0; row < frames.length; row++)
            for (int col = 0; col < maxColumns; col++)
                g.drawImage(frames[row][Math.min(col, frames[row].length - 1)], col * frameWidth, row * frameHeight, frameWidth, frameHeight, null);
        g.dispose();
        return new SpriteSheet(sheet, directions.length, maxColumns);
    }

    private BufferedImage[] loadRotationFrame(String basePath, String direction) {
        BufferedImage image = loadImage(basePath + "/" + direction + ".png");
        return image == null ? new BufferedImage[0] : new BufferedImage[]{image};
    }

    private BufferedImage[] loadAnimationFrames(String basePath, String direction, String framePrefix) {
        File directory = new File(basePath + "/" + direction);
        File[] files = directory.listFiles((dir, name) -> name.startsWith(framePrefix) && name.endsWith(".png"));
        if (files == null || files.length == 0) return new BufferedImage[0];
        Arrays.sort(files, Comparator.comparing(File::getName));
        BufferedImage[] result = new BufferedImage[files.length];
        for (int i = 0; i < files.length; i++) {
            result[i] = loadImage(files[i].getPath());
            if (result[i] == null) return new BufferedImage[0];
        }
        return result;
    }

    private BufferedImage loadImage(String path) {
        if (path == null) return null;
        File file = new File(path);
        if (!file.exists()) return null;
        try { return ImageIO.read(file); }
        catch (IOException e) { System.err.println("Cannot load image: " + path); return null; }
    }

    private BufferedImage cropImage(String path, int x, int y, int w, int h) {
        BufferedImage image = loadImage(path);
        if (image == null) return null;
        if (x < 0 || y < 0 || x + w > image.getWidth() || y + h > image.getHeight()) return null;
        return image.getSubimage(x, y, w, h);
    }

    private BufferedImage safeSubimage(BufferedImage img, int x, int y, int w, int h) {
        if (img == null) return null;
        if (x < 0 || y < 0 || x + w > img.getWidth() || y + h > img.getHeight()) {
            System.err.println("safeSubimage out of bounds: " + x + "," + y + " " + w + "x" + h
                + " (image=" + img.getWidth() + "x" + img.getHeight() + ")");
            return null;
        }
        return img.getSubimage(x, y, w, h);
    }

    // ===================== Direction helpers =====================

    private int getDirectionRow(Entity entity) {
        if (!(entity instanceof Animal)) return 0;
        Animal a = (Animal) entity;
        double vx = a.getVelocity().getX(), vy = a.getVelocity().getY();
        if (Math.abs(vx) > Math.abs(vy)) return vx >= 0 ? 3 : 2;
        return vy >= 0 ? 0 : 1;
    }

    private int getDirectionColumn(Entity entity) {
        if (!(entity instanceof Animal)) return 0;
        Animal a = (Animal) entity;
        double vx = a.getVelocity().getX(), vy = a.getVelocity().getY();
        if (Math.abs(vx) > Math.abs(vy)) return vx < 0 ? 2 : 3;
        return vy < 0 ? 1 : 0;
    }

    // ===================== Public draw API =====================

    public boolean drawEntitySprite(Graphics2D g, String key, Entity entity, int cx, int cy, int w, int h) {
        EntitySpriteSet set = entitySprites.get(key);
        if (set == null) return false;
        if (set.shadow != null) {
            int sw = Math.max(12, w / 2), sh = Math.max(6, h / 4);
            g.drawImage(set.shadow, cx - sw / 2, cy + h / 5, sw, sh, null);
        }
        SpriteSheet sheet = selectAnimation(set, entity);
        if (sheet == null) return false;
        int row   = set.directionFramesInFirstRow ? 0 : getDirectionRow(entity);
        int frame = set.directionFramesInFirstRow ? getDirectionColumn(entity) : (int) ((System.currentTimeMillis() / 120) % sheet.columns);
        g.drawImage(sheet.getFrame(frame, row), cx - w / 2, cy - h / 2, w, h, null);
        return true;
    }

    public boolean drawPlantSprite(Graphics2D g, String key, model.plant.Plant plant, int cx, int cy, int size) {
        PlantSpriteSet set = plantSprites.get(key);
        if (set == null) return false;
        BufferedImage image = set.get(plant.getGrowthStage());
        if (image == null) return false;
        double scale = size / 32.0;
        int w = Math.max(8, (int) Math.round(image.getWidth() * scale));
        int h = Math.max(8, (int) Math.round(image.getHeight() * scale));
        g.drawImage(image, cx - w / 2, cy - h + size / 4, w, h, null);
        return true;
    }

    private SpriteSheet selectAnimation(EntitySpriteSet set, Entity entity) {
        if (!entity.isAlive() && set.death != null) return set.death;
        if (!(entity instanceof Animal)) return set.idle;
        Animal animal = (Animal) entity;
        AnimalState state = animal.getCurrentState();
        double speed = animal.getVelocity().magnitude();
        if (state == AnimalState.DEAD      && set.death  != null) return set.death;
        if (state == AnimalState.ATTACKING && set.attack != null) return set.attack;
        if (state == AnimalState.HURT      && set.hurt   != null) return set.hurt;
        if (speed > animal.getSpeed() * 0.7 && set.run   != null) return set.run;
        if (speed > 0.05                    && set.walk  != null) return set.walk;
        if (set.idle != null) return set.idle;
        if (set.walk != null) return set.walk;
        return set.run;
    }
}