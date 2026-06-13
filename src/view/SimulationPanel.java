package view;

import controller.SimulationTime;
import model.Animal;
import model.AnimalState;
import model.Entity;
import model.Vector2D;
import model.apex.Bear;
import model.apex.Eagle;
import model.apex.Human;
import model.apex.Lion;
import model.apex.Tiger;
import model.carnivore.Cheetah;
import model.aquatic.FishFour;
import model.aquatic.FishOne;
import model.aquatic.FishThree;
import model.aquatic.FishTwo;
import model.carnivore.Fox;
import model.carnivore.Wolf;
import model.domestic.Chicken;
import model.domestic.Cow;
import model.domestic.Pig;
import model.herbivore.BlackGrouse;
import model.herbivore.Boar;
import model.herbivore.Deer;
import model.herbivore.Elephant;
import model.herbivore.Goat;
import model.herbivore.Horse;
import model.herbivore.Rabbit;
import model.environment.Environment;
import model.plant.Algae;
import model.plant.Berry;
import model.plant.Grass;
import model.plant.GrowthStage;
import model.plant.Mushroom;
import model.plant.Plant;
import model.plant.SmallTree;
import model.environment.obstacle.Bush;
import model.environment.obstacle.OldTree;
import model.environment.obstacle.Rock;
import model.plant.TreePlant;
import model.environment.map.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

public class SimulationPanel extends JPanel {
    private List<Entity> entities;
    private final Map<String, BufferedImage> sprites;
    private final Map<String, EntitySpriteSet> entitySprites;
    private final Map<String, PlantSpriteSet> plantSprites;
    private JPanel timelinePanel;
    private JLabel speedLabel;
    private JSlider speedSlider;
    private JButton viewModeButton;
    private double worldWidth;
    private double worldHeight;
    private double renderScale;
    private int renderOffsetX;
    private int renderOffsetY;
    private double cameraFocusX;
    private double cameraFocusY;
    private final ForestTileMap forestTileMap;
    private Environment env;
    private boolean basicMode = false;
    private String buildMode = "FOOD_PLANT";
    public static final int MAX_FLOCK_TICKS = 500;
    public static final int REST_TICKS = 150;

    // Build sprites (từ file cũ)
    private BufferedImage grassSprite;
    private BufferedImage bushSprite;
    private BufferedImage treeSprite;
    private BufferedImage rockSprite;

    // Constructor có Environment (từ file mới)
    public SimulationPanel(Environment env) {
        this.env = env;
        this.sprites = new HashMap<>();
        this.entitySprites = new HashMap<>();
        this.plantSprites = new HashMap<>();

        if (env instanceof EmptyMap) {
            this.forestTileMap = null;
            this.worldWidth = env.getWidth();
            this.worldHeight = env.getHeight();
        } else {
            this.forestTileMap = new ForestTileMap("assets/Environment/Forest/Forest.tmx");
            boolean mapLoaded = (forestTileMap != null && forestTileMap.isLoaded());
            this.worldWidth = mapLoaded ? forestTileMap.getPixelWidth() : env.getWidth();
            this.worldHeight = mapLoaded ? forestTileMap.getPixelHeight() : env.getHeight();
        }

        this.renderScale = 1.0;
        this.cameraFocusX = 0.5;
        this.cameraFocusY = 0.5;
        setLayout(null);
        setBackground(new Color(34, 139, 34));
        installMouseCamera();
        installTimelineControls();
        installBuildControls();
        loadSprites();
        loadPlantSprites();
        loadBuildSprites();
    }

    // Constructor không có Environment (từ file cũ, giữ để tương thích)
    public SimulationPanel() {
        this.sprites = new HashMap<>();
        this.entitySprites = new HashMap<>();
        this.plantSprites = new HashMap<>();
        this.forestTileMap = new ForestTileMap("assets/Environment/Forest/Forest.tmx");
        this.worldWidth = forestTileMap.isLoaded() ? forestTileMap.getPixelWidth() : 800;
        this.worldHeight = forestTileMap.isLoaded() ? forestTileMap.getPixelHeight() : 600;
        this.renderScale = 1.0;
        this.cameraFocusX = 0.5;
        this.cameraFocusY = 0.5;
        setLayout(null);
        setBackground(new Color(34, 139, 34));
        installMouseCamera();
        installTimelineControls();
        installBuildControls();
        loadSprites();
        loadPlantSprites();
        loadBuildSprites();
    }

    public void setWorldSize(double width, double height) {
        this.worldWidth = width;
        this.worldHeight = height;
    }

    public double getWorldWidth() { return worldWidth; }
    public double getWorldHeight() { return worldHeight; }

    @Override
    public void doLayout() {
        super.doLayout();
        if (timelinePanel == null) return;
        int width = 240;
        int height = 96;
        int margin = 12;
        timelinePanel.setBounds(Math.max(margin, getWidth() - width - margin), margin, width, height);
    }

    public void setEntities(List<Entity> entities) {
        this.entities = entities;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        List<Entity> renderEntities = entities;
        if (renderEntities == null) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        updateRenderTransform();
        drawWorldBounds(g2);

        for (Entity e : renderEntities) {
            if (!(e instanceof Animal)) drawEntity(g2, e);
        }
        for (Entity e : renderEntities) {
            if (e instanceof Animal) drawEntity(g2, e);
        }

        drawBuildUI(g2);
        drawInfoPanel(g2);
        g2.dispose();
    }

    // ===================== INFO PANEL (từ file mới - đầy đủ hơn) =====================

    private void drawInfoPanel(Graphics2D g) {
        if (entities == null) return;

        int rabbit = 0, deer = 0, boar = 0, fox = 0, wolf = 0;
        int goat = 0, horse = 0, cheetah = 0, lion = 0, bear = 0, human = 0, grass = 0;
        String weather = "UNKNOWN";

        if (Environment.getInstance() != null && Environment.getInstance().getWeather() != null) {
            weather = Environment.getInstance().getWeather().getCurrentWeather().toString();
        }

        for (Entity e : entities) {
            if (e instanceof Rabbit) rabbit++;
            else if (e instanceof Deer) deer++;
            else if (e instanceof Boar) boar++;
            else if (e instanceof Fox) fox++;
            else if (e instanceof Wolf) wolf++;
            else if (e instanceof Goat) goat++;
            else if (e instanceof Horse) horse++;
            else if (e instanceof Cheetah) cheetah++;
            else if (e instanceof Lion) lion++;
            else if (e instanceof Bear) bear++;
            else if (e instanceof Human) human++;
            else if (e instanceof Grass) grass++;
        }

        g.setColor(new Color(0, 0, 0, 180));
        g.fillRoundRect(10, 10, 190, 310, 10, 10);
        g.setColor(Color.WHITE);
        g.drawString("Rabbit : "  + rabbit,  20, 35);
        g.drawString("Deer : "    + deer,    20, 55);
        g.drawString("Boar : "    + boar,    20, 75);
        g.drawString("Fox : "     + fox,     20, 95);
        g.drawString("Wolf : "    + wolf,    20, 115);
        g.drawString("Goat : "    + goat,    20, 135);
        g.drawString("Horse : "   + horse,   20, 155);
        g.drawString("Cheetah : " + cheetah, 20, 175);
        g.drawString("Lion : "    + lion,    20, 195);
        g.drawString("Bear : "    + bear,    20, 215);
        g.drawString("Human : "   + human,   20, 235);
        g.drawString("Grass : "   + grass,   20, 255);
        g.drawString("Weather : " + weather, 20, 285);
    }

    // ===================== BUILD UI (file cũ có Rock) =====================

    private void drawBuildUI(Graphics2D g) {
        int panelHeight = getHeight();
        drawBuildButton(g, "Plant", 20,  panelHeight - 80, buildMode.equals("FOOD_PLANT"));
        drawBuildButton(g, "Bush",  150, panelHeight - 80, buildMode.equals("BUSH"));
        drawBuildButton(g, "Tree",  280, panelHeight - 80, buildMode.equals("TREE"));
        drawBuildButton(g, "Rock",  410, panelHeight - 80, buildMode.equals("ROCK"));
    }

    private void drawBuildButton(Graphics2D g, String text, int x, int y, boolean active) {
        g.setColor(active ? new Color(90, 180, 90) : new Color(40, 40, 40, 220));
        g.fillRoundRect(x, y, 110, 50, 18, 18);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, x + (110 - fm.stringWidth(text)) / 2, y + 31);
    }

    // ===================== DRAW ENTITY =====================

    private void drawEntity(Graphics2D g, Entity e) {
        int cx = worldToScreenX(e.getPosition().getX());
        int cy = worldToScreenY(e.getPosition().getY());

        if (basicMode) {
            drawBasicEntity(g, e, cx, cy);
            return;
        }

        int size = Math.max(12, (int) (e.getSize() * 4 * renderScale));
        boolean drawn = false;

        // Plants & obstacles
        if (e instanceof Grass) {
            if (grassSprite != null) g.drawImage(grassSprite, cx - 12, cy - 12, 24, 24, null);
            return;
        }
        if (e instanceof Bush) {
            if (bushSprite != null) g.drawImage(bushSprite, cx - 24, cy - 24, 48, 48, null);
            return;
        }
        if (e instanceof OldTree) {
            if (treeSprite != null) g.drawImage(treeSprite, cx - 40, cy - 60, 80, 80, null);
            return;
        }
        if (e instanceof Rock) {
            if (rockSprite != null) g.drawImage(rockSprite, cx - 28, cy - 28, 56, 56, null);
            return;
        }
        if (!(e instanceof Animal)) return;

        // Animals
        if (e instanceof Rabbit) {
            drawn = drawEntitySprite(g, "hare", e, cx, cy, size * 2, size * 2)
                    || drawSprite(g, "rabbit", cx, cy, size * 2, size * 2);
            if (!drawn) drawRabbit(g, cx, cy, size);
        } else if (e instanceof Chicken) {
            drawn = drawEntitySprite(g, "chicken", e, cx, cy, size * 2, size * 2);
            if (!drawn) drawDefault(g, cx, cy, size);
        } else if (e instanceof Cow) {
            drawn = drawEntitySprite(g, "cow", e, cx, cy, size * 2, size * 2);
            if (!drawn) drawDefault(g, cx, cy, size);
        } else if (e instanceof Pig) {
            drawn = drawEntitySprite(g, "pig", e, cx, cy, size * 2, size * 2);
            if (!drawn) drawDefault(g, cx, cy, size);
        } else if (e instanceof FishOne) {
            drawn = drawEntitySprite(g, "fish_one", e, cx, cy, size * 3, size * 2);
            if (!drawn) drawDefault(g, cx, cy, size);
        } else if (e instanceof FishTwo) {
            drawn = drawEntitySprite(g, "fish_two", e, cx, cy, size * 3, size * 2);
            if (!drawn) drawDefault(g, cx, cy, size);
        } else if (e instanceof FishThree) {
            drawn = drawEntitySprite(g, "fish_three", e, cx, cy, size * 3, size * 2);
            if (!drawn) drawDefault(g, cx, cy, size);
        } else if (e instanceof FishFour) {
            drawn = drawEntitySprite(g, "fish_four", e, cx, cy, size * 3, size * 2);
            if (!drawn) drawDefault(g, cx, cy, size);
        } else if (e instanceof BlackGrouse) {
            drawn = drawEntitySprite(g, "black_grouse", e, cx, cy, size * 2, size * 2);
            if (!drawn) drawDefault(g, cx, cy, size);
        } else if (e instanceof Wolf) {
            drawn = drawEntitySprite(g, "fox", e, cx, cy, size * 2, size * 2)
                    || drawSprite(g, "wolf", cx, cy, size * 2, size * 2);
            if (!drawn) drawWolf(g, cx, cy, size);
        } else if (e instanceof Fox) {
            drawn = drawEntitySprite(g, "fox", e, cx, cy, size * 2, size * 2);
            if (!drawn) drawWolf(g, cx, cy, size);
        } else if (e instanceof Boar) {
            drawn = drawEntitySprite(g, "boar", e, cx, cy, size * 2, size * 2);
            if (!drawn) drawDefault(g, cx, cy, size);
        } else if (e instanceof Deer) {
            drawn = drawEntitySprite(g, "deer", e, cx, cy, size * 2, size * 2);
            if (!drawn) drawDefault(g, cx, cy, size);
        } else if (e instanceof Goat) {
            int spriteSize = spritePixels(size, 1.75);
            drawn = drawEntitySprite(g, "goat", e, cx, cy, spriteSize, spriteSize);
            if (!drawn) drawDefault(g, cx, cy, size);
        } else if (e instanceof Horse) {
            int spriteSize = spritePixels(size, 2.3);
            drawn = drawEntitySprite(g, "horse", e, cx, cy, spriteSize, spriteSize);
            if (!drawn) drawDefault(g, cx, cy, size);
        } else if (e instanceof Cheetah) {
            int spriteSize = spritePixels(size, 2.15);
            drawn = drawEntitySprite(g, "cheetah", e, cx, cy, spriteSize, spriteSize);
            if (!drawn) drawDefault(g, cx, cy, size);
        } else if (e instanceof Bear) {
            int spriteSize = spritePixels(size, 1.5);
            drawn = drawEntitySprite(g, "bear", e, cx, cy, spriteSize, spriteSize);
            if (!drawn) drawDefault(g, cx, cy, size);
        } else if (e instanceof Lion) {
            int spriteSize = spritePixels(size, 1.45);
            drawn = drawEntitySprite(g, "lion", e, cx, cy, spriteSize, spriteSize);
            if (!drawn) drawDefault(g, cx, cy, size);
        } else if (e instanceof Human) {
            int spriteSize = spritePixels(size, 1.6);
            drawn = drawEntitySprite(g, "human", e, cx, cy, spriteSize, spriteSize);
            if (!drawn) drawDefault(g, cx, cy, size);
        } else if (e instanceof Elephant) {
            g.setColor(Color.GRAY);
            g.fillOval(cx - size, cy - size, size * 2, size * 2);
        } else {
            drawDefault(g, cx, cy, size);
        }

        if (e instanceof Animal) drawHealthBar(g, (Animal) e, cx, cy, size);
    }

    private int spritePixels(int size, double multiplier) {
        return Math.max(12, (int) Math.round(size * multiplier));
    }

    // ===================== BASIC MODE =====================

    private void drawBasicEntity(Graphics2D g, Entity e, int cx, int cy) {
        int size = 10;

        if (e instanceof Grass)    { g.setColor(new Color(0, 180, 0)); drawTriangle(g, cx, cy, size); return; }
        if (e instanceof Berry)    { g.setColor(Color.MAGENTA);        drawTriangle(g, cx, cy, size); return; }
        if (e instanceof Mushroom) { g.setColor(Color.PINK);           drawTriangle(g, cx, cy, size); return; }
        if (e instanceof Rock) {
            g.setColor(Color.LIGHT_GRAY);
            Polygon p = new Polygon();
            p.addPoint(cx, cy - size); p.addPoint(cx + size, cy - size / 2);
            p.addPoint(cx + size, cy + size); p.addPoint(cx - size, cy + size);
            p.addPoint(cx - size, cy - size / 2);
            g.fillPolygon(p); return;
        }
        if (e instanceof Rabbit)   { g.setColor(Color.PINK);                    g.fillOval(cx-size,cy-size,size*2,size*2); return; }
        if (e instanceof Deer)     { g.setColor(Color.ORANGE);                  g.fillOval(cx-size,cy-size,size*2,size*2); return; }
        if (e instanceof Boar)     { g.setColor(new Color(120, 70, 20));         g.fillOval(cx-size,cy-size,size*2,size*2); return; }
        if (e instanceof Elephant) { g.setColor(Color.GRAY);                    g.fillOval(cx-size,cy-size,size*2,size*2); return; }
        if (e instanceof Goat)     { g.setColor(new Color(232, 232, 220));       g.fillOval(cx-size,cy-size,size*2,size*2); return; }
        if (e instanceof Horse)    { g.setColor(new Color(112, 70, 38));         g.fillOval(cx-size,cy-size,size*2,size*2); return; }
        if (e instanceof Fox)      { g.setColor(Color.RED);                     g.fillRect(cx-size,cy-size,size*2,size*2); return; }
        if (e instanceof Wolf)     { g.setColor(Color.DARK_GRAY);               g.fillRect(cx-size,cy-size,size*2,size*2); return; }
        if (e instanceof Cheetah)  { g.setColor(new Color(218, 172, 73));        g.fillRect(cx-size,cy-size,size*2,size*2); return; }
        if (e instanceof Lion)     { g.setColor(new Color(201, 139, 49));        g.fillRect(cx-size,cy-size,size*2,size*2); return; }
        if (e instanceof Bear)     { g.setColor(new Color(92, 58, 36));          g.fillRect(cx-size,cy-size,size*2,size*2); return; }
        if (e instanceof model.aquatic.Fish) {
            g.setColor(Color.CYAN);
            Polygon d = new Polygon();
            d.addPoint(cx,cy-size); d.addPoint(cx+size,cy); d.addPoint(cx,cy+size); d.addPoint(cx-size,cy);
            g.fillPolygon(d); return;
        }
        if (e instanceof Human) { g.setColor(new Color(60, 120, 210)); g.fillRoundRect(cx-size,cy-size,size*2,size*2,8,8); return; }
        if (e instanceof Bush)    { g.setColor(Color.BLUE);                     g.fillRoundRect(cx-size,cy-size,size*2,size*2,6,6); return; }
        if (e instanceof OldTree) { g.setColor(new Color(120, 70, 20));          g.fillRect(cx-size,cy-size,size*2,size*2); return; }
        g.setColor(Color.BLACK); g.fillOval(cx-4,cy-4,8,8);
    }

    private void drawTriangle(Graphics2D g, int cx, int cy, int size) {
        Polygon p = new Polygon();
        p.addPoint(cx, cy - size); p.addPoint(cx - size, cy + size); p.addPoint(cx + size, cy + size);
        g.fillPolygon(p);
    }

    // ===================== LOAD SPRITES =====================

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
        loadSprite("rabbit", "assets/Entities/Rabbit.png");
        loadSprite("wolf",   "assets/Entities/Wolf.png");
    }

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

    private void loadBuildSprites() {
        BufferedImage objects = loadImage("assets/Environment/Forest/Objects.png");
        if (objects != null) {
            // Tree: cây thông nhỏ góc trái (64x96)
            treeSprite = safeSubimage(objects, 0,   0,   64, 96);
            // Bush: bush góc trái (48x48) tại row 96
            bushSprite = safeSubimage(objects, 0,   96,  48, 48);
            // Rock lớn (80x80) tại (224, 144)
            rockSprite = safeSubimage(objects, 224, 144, 80, 80);
        }
        // Grass từ tileset cỏ riêng nếu có, hoặc bỏ qua
        BufferedImage plants = loadImage("assets/Environment/Forest/Plants.png");
        if (plants != null) {
            grassSprite = safeSubimage(plants, 0, 64, 16, 16);
        }
    }

    // Safe getSubimage — không crash nếu out of bounds
    private BufferedImage safeSubimage(BufferedImage img, int x, int y, int w, int h) {
        if (img == null) return null;
        if (x < 0 || y < 0 || x + w > img.getWidth() || y + h > img.getHeight()) {
            System.err.println("safeSubimage out of bounds: " + x + "," + y + " " + w + "x" + h
                + " (image=" + img.getWidth() + "x" + img.getHeight() + ")");
            return null;
        }
        return img.getSubimage(x, y, w, h);
    }

    private void loadSprite(String key, String path) {
        File file = new File(path);
        if (!file.exists()) return;
        try { sprites.put(key, ImageIO.read(file)); }
        catch (IOException e) { System.err.println("Cannot load sprite: " + path); }
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

    // ===================== DRAW HELPERS =====================

    private boolean drawSprite(Graphics2D g, String key, int cx, int cy, int w, int h) {
        BufferedImage image = sprites.get(key);
        if (image == null) return false;
        g.drawImage(image, cx - w / 2, cy - h / 2, w, h, null);
        return true;
    }

    private boolean drawEntitySprite(Graphics2D g, String key, Entity entity, int cx, int cy, int w, int h) {
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

    private SpriteSheet selectAnimation(EntitySpriteSet set, Entity entity) {
        if (!entity.isAlive() && set.death != null) return set.death;
        if (!(entity instanceof Animal)) return set.idle;
        Animal animal = (Animal) entity;
        AnimalState state = animal.getCurrentState();
        double speed = animal.getVelocity().magnitude();
        if (state == AnimalState.DEAD     && set.death  != null) return set.death;
        if (state == AnimalState.ATTACKING && set.attack != null) return set.attack;
        if (state == AnimalState.HURT     && set.hurt   != null) return set.hurt;
        if (speed > animal.getSpeed() * 0.7 && set.run  != null) return set.run;
        if (speed > 0.05                    && set.walk != null) return set.walk;
        if (set.idle != null) return set.idle;
        if (set.walk != null) return set.walk;
        return set.run;
    }

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

    private void drawRabbit(Graphics2D g, int cx, int cy, int size) {
        int bodyW = size, bodyH = Math.max(12, size - 4);
        g.setColor(new Color(245, 245, 238));
        g.fillOval(cx - bodyW/2, cy - bodyH/2, bodyW, bodyH);
        g.fillOval(cx + size/5, cy - size/3, size/2, size/2);
        g.fillRoundRect(cx + size/4, cy - size, size/6, size/2, 8, 8);
        g.fillRoundRect(cx + size/2, cy - size, size/6, size/2, 8, 8);
        g.setColor(new Color(255, 178, 190));
        g.fillRoundRect(cx + size/4 + 2, cy - size + 4, Math.max(2, size/10), size/3, 6, 6);
        g.fillRoundRect(cx + size/2 + 2, cy - size + 4, Math.max(2, size/10), size/3, 6, 6);
        g.setColor(Color.BLACK);
        g.fillOval(cx + size/2, cy - size/5, 3, 3);
    }

    private void drawWolf(Graphics2D g, int cx, int cy, int size) {
        int bodyW = size + 6, bodyH = Math.max(14, size - 2);
        int x = cx - bodyW/2;
        g.setColor(new Color(91, 101, 111));
        g.fillOval(x, cy - bodyH/2, bodyW, bodyH);
        Polygon head = new Polygon();
        head.addPoint(cx + bodyW/2 - 2, cy - bodyH/3);
        head.addPoint(cx + bodyW/2 + size/2, cy);
        head.addPoint(cx + bodyW/2 - 2, cy + bodyH/3);
        g.fillPolygon(head);
        Polygon ear = new Polygon();
        ear.addPoint(cx + bodyW/4, cy - bodyH/2);
        ear.addPoint(cx + bodyW/3, cy - bodyH);
        ear.addPoint(cx + bodyW/2, cy - bodyH/3);
        g.fillPolygon(ear);
        g.setColor(new Color(70, 78, 86));
        Polygon tail = new Polygon();
        tail.addPoint(x + 4, cy - 3); tail.addPoint(x - size/2, cy - bodyH/2); tail.addPoint(x, cy + bodyH/3);
        g.fillPolygon(tail);
        g.setColor(Color.BLACK);
        g.fillOval(cx + bodyW/2 + size/4, cy - 3, 4, 4);
    }

    private void drawDefault(Graphics2D g, int cx, int cy, int size) {
        g.setColor(Color.WHITE);
        g.fillOval(cx - size/2, cy - size/2, size, size);
    }

    private void drawPlant(Graphics2D g, Plant plant) {
        int cx = worldToScreenX(plant.getPosition().getX());
        int cy = worldToScreenY(plant.getPosition().getY());
        int size = Math.max(4, (int) Math.round(plant.getSize() * 4 * renderScale));
        if (drawPlantSprite(g, plant.getSpeciesKey(), plant, cx, cy, size)) return;
        if (plant instanceof SmallTree || plant instanceof TreePlant) {
            if (!drawPlantSprite(g, "small_tree", plant, cx, cy, size)) drawSmallTree(g, cx, cy, size, plant.getGrowthStage());
        } else if (plant instanceof Grass || plant instanceof Algae) {
            drawGrass(g, cx, cy, size);
        } else if (plant instanceof Berry) {
            drawBerry(g, cx, cy, size, ((Berry) plant).hasFruits());
        } else if (plant instanceof Mushroom) {
            drawMushroom(g, cx, cy, size);
        } else {
            drawGrass(g, cx, cy, size);
        }
    }

    private boolean drawPlantSprite(Graphics2D g, String key, Plant plant, int cx, int cy, int size) {
        PlantSpriteSet set = plantSprites.get(key);
        if (set == null) return false;
        BufferedImage image = set.get(plant.getGrowthStage());
        if (image == null) return false;
        double scale = size / 32.0;
        int w = Math.max(8, (int) Math.round(image.getWidth() * scale));
        int h = Math.max(8, (int) Math.round(image.getHeight() * scale));
        g.drawImage(image, cx - w/2, cy - h + size/4, w, h, null);
        return true;
    }

    private void drawSmallTree(Graphics2D g, int cx, int cy, int size, GrowthStage stage) {
        int th = Math.max(6, size/2), tw = Math.max(3, size/7), cs = Math.max(8, size);
        if (stage == GrowthStage.SEED) { g.setColor(new Color(96,67,38)); g.fillOval(cx-size/8,cy-size/8,Math.max(3,size/4),Math.max(3,size/4)); return; }
        g.setColor(new Color(104,70,38)); g.fillRoundRect(cx-tw/2,cy-th/2,tw,th,4,4);
        g.setColor(stage==GrowthStage.OLD ? new Color(91,128,55) : new Color(42,139,61));
        g.fillOval(cx-cs/2,cy-th/2-cs/2,cs,cs);
        g.setColor(new Color(29,104,49)); g.fillOval(cx-cs/3,cy-th/2-cs/3,cs/2,cs/2);
    }

    private void drawGrass(Graphics2D g, int cx, int cy, int size) {
        g.setColor(new Color(55,154,58));
        int bh = Math.max(5, size);
        g.drawLine(cx,cy,cx,cy-bh);
        g.drawLine(cx,cy,cx-size/3,cy-bh*2/3);
        g.drawLine(cx,cy,cx+size/3,cy-bh*2/3);
    }

    private void drawBerry(Graphics2D g, int cx, int cy, int size, boolean hasFruits) {
        drawGrass(g, cx, cy, size);
        if (!hasFruits) return;
        g.setColor(new Color(182,38,62));
        int bs = Math.max(3,size/4);
        g.fillOval(cx-bs,cy-size,bs,bs); g.fillOval(cx+bs/2,cy-size*3/4,bs,bs);
    }

    private void drawMushroom(Graphics2D g, int cx, int cy, int size) {
        int sw = Math.max(3,size/4), sh = Math.max(5,size/2);
        g.setColor(new Color(232,220,185)); g.fillRoundRect(cx-sw/2,cy-sh/2,sw,sh,4,4);
        g.setColor(new Color(185,45,48));   g.fillArc(cx-size/2,cy-sh,size,size,0,180);
    }

    private void drawHealthBar(Graphics2D g, Animal animal, int cx, int cy, int size) {
        if (animal.getCurrentState() == AnimalState.DEAD) return;
        int bw = Math.max(18, size*2), bh = 4;
        int x = cx - bw/2, y = cy - size - 8;
        double pct = Math.max(0, Math.min(1, animal.getHp() / animal.getMaxHp()));
        int fw = (int) Math.round(bw * pct);
        g.setColor(new Color(20,20,20,180)); g.fillRect(x-1,y-1,bw+2,bh+2);
        g.setColor(new Color(154,35,35));    g.fillRect(x,y,bw,bh);
        g.setColor(new Color(58,190,74));    g.fillRect(x,y,fw,bh);
    }

    // ===================== CAMERA & RENDER TRANSFORM =====================

    private void updateRenderTransform() {
        renderScale = 1.0;
        double sw = worldWidth * renderScale, sh = worldHeight * renderScale;
        double ox = Math.max(0, sw - getWidth()), oy = Math.max(0, sh - getHeight());
        renderOffsetX = ox > 0 ? -(int) Math.floor(ox * cameraFocusX) : (int) Math.floor((getWidth()  - sw) / 2.0);
        renderOffsetY = oy > 0 ? -(int) Math.floor(oy * cameraFocusY) : (int) Math.floor((getHeight() - sh) / 2.0);
    }

    private int worldToScreenX(double x) { return renderOffsetX + (int) Math.round(x * renderScale); }
    private int worldToScreenY(double y) { return renderOffsetY + (int) Math.round(y * renderScale); }

    private void drawWorldBounds(Graphics2D g) {
        int width  = (int) Math.round(worldWidth  * renderScale);
        int height = (int) Math.round(worldHeight * renderScale);
        if (forestTileMap != null && forestTileMap.isLoaded()) {
            forestTileMap.draw(g, renderOffsetX, renderOffsetY, width, height, renderScale);
        } else {
            g.setColor(new Color(27, 112, 49));
            g.fillRect(renderOffsetX, renderOffsetY, width, height);
        }
        g.setColor(new Color(23, 82, 40));
        g.drawRect(renderOffsetX, renderOffsetY, width - 1, height - 1);
    }

    // ===================== INPUT HANDLERS =====================

    private void installMouseCamera() {
        MouseAdapter mc = new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e)   { updateCameraFocus(e); }
            @Override public void mouseDragged(MouseEvent e) { updateCameraFocus(e); }
        };
        addMouseMotionListener(mc);
    }

    private void installBuildControls() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int ph = getHeight();
                Rectangle plantBtn = new Rectangle(20,  ph-80, 110, 50);
                Rectangle bushBtn  = new Rectangle(150, ph-80, 110, 50);
                Rectangle treeBtn  = new Rectangle(280, ph-80, 110, 50);
                Rectangle rockBtn  = new Rectangle(410, ph-80, 110, 50);

                if (plantBtn.contains(e.getPoint())) { buildMode = "FOOD_PLANT"; repaint(); return; }
                if (bushBtn.contains(e.getPoint()))  { buildMode = "BUSH";       repaint(); return; }
                if (treeBtn.contains(e.getPoint()))  { buildMode = "TREE";       repaint(); return; }
                if (rockBtn.contains(e.getPoint()))  { buildMode = "ROCK";       repaint(); return; }

                double worldX = (e.getX() - renderOffsetX) / renderScale;
                double worldY = (e.getY() - renderOffsetY) / renderScale;
                Environment env = Environment.getInstance();
                if (env == null) return;
                try {
                    if (buildMode.equals("FOOD_PLANT")) {
                        Grass g2 = new Grass(new model.Vector2D(worldX, worldY));
                        g2.setRuntimePlaced(true); env.queueEntity(g2);
                        System.out.println("Grass planted");
                    } else if (buildMode.equals("BUSH")) {
                        Bush b = new Bush(new model.Vector2D(worldX, worldY), 18);
                        b.setRuntimePlaced(true); env.queueEntity(b);
                        System.out.println("Bush planted");
                    } else if (buildMode.equals("TREE")) {
                        OldTree t = new OldTree(new model.Vector2D(worldX, worldY));
                        t.setRuntimePlaced(true); env.queueEntity(t);
                        System.out.println("Tree planted");
                    } else if (buildMode.equals("ROCK")) {
                        Rock r = new Rock(new Vector2D(worldX, worldY));
                        r.setRuntimePlaced(true); env.queueEntity(r);
                        System.out.println("Rock placed");
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        });
    }

    private void updateCameraFocus(MouseEvent e) {
        cameraFocusX = clamp(e.getX() / (double) Math.max(1, getWidth()),  0, 1);
        cameraFocusY = clamp(e.getY() / (double) Math.max(1, getHeight()), 0, 1);
        repaint();
    }

    private void installTimelineControls() {
        timelinePanel = new JPanel(new BorderLayout(8, 4));
        timelinePanel.setOpaque(true);
        timelinePanel.setBackground(new Color(20, 32, 25, 210));
        timelinePanel.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        speedLabel = new JLabel("Time x1.00");
        speedLabel.setForeground(Color.WHITE);
        speedLabel.setFont(speedLabel.getFont().deriveFont(Font.BOLD, 12f));

        speedSlider = new JSlider(0, 800, 100);
        speedSlider.setOpaque(false);
        speedSlider.setFocusable(false);
        speedSlider.setMajorTickSpacing(100);
        speedSlider.setMinorTickSpacing(25);
        speedSlider.addChangeListener(event -> {
            double scale = speedSlider.getValue() / 100.0;
            SimulationTime.setTimeScale(scale);
            speedLabel.setText(String.format("Time x%.2f", SimulationTime.getTimeScale()));
        });

        viewModeButton = new JButton("Switch to Basic");
        viewModeButton.addActionListener(e -> {
            basicMode = !basicMode;
            viewModeButton.setText(basicMode ? "Switch to Graphics" : "Switch to Basic");
            repaint();
        });

        JPanel speedPanel = new JPanel(new BorderLayout(0, 4));
        speedPanel.setOpaque(false);
        speedPanel.add(speedLabel, BorderLayout.NORTH);
        speedPanel.add(speedSlider, BorderLayout.CENTER);
        timelinePanel.add(speedPanel, BorderLayout.CENTER);
        timelinePanel.add(viewModeButton, BorderLayout.SOUTH);
        add(timelinePanel);
    }

    private double clamp(double v, double min, double max) { return Math.max(min, Math.min(max, v)); }

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

    // ===================== INNER CLASSES =====================

    private static class PlantSpriteSet {
        private final Map<GrowthStage, BufferedImage> images = new EnumMap<>(GrowthStage.class);
        private void put(GrowthStage stage, BufferedImage image) { if (image != null) images.put(stage, image); }
        private BufferedImage get(GrowthStage stage) {
            BufferedImage img = images.get(stage);
            if (img != null) return img;
            GrowthStage[] all = GrowthStage.values();
            int idx = stage.ordinal();
            for (int i = idx-1; i >= 0; i--)       { img = images.get(all[i]); if (img != null) return img; }
            for (int i = idx+1; i < all.length; i++) { img = images.get(all[i]); if (img != null) return img; }
            return null;
        }
        private boolean hasAnyImage() { return !images.isEmpty(); }
    }

    private static class EntitySpriteSet {
        private SpriteSheet idle, walk, run, attack, hurt, death;
        private BufferedImage shadow;
        private boolean directionFramesInFirstRow;
        private boolean hasAnyAnimation() { return idle!=null||walk!=null||run!=null||attack!=null||hurt!=null||death!=null; }
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
            int sc = Math.max(0, Math.min(column, columns-1));
            int sr = Math.max(0, Math.min(row,    rows-1));
            return image.getSubimage(sc*frameWidth, sr*frameHeight, frameWidth, frameHeight);
        }
    }
}