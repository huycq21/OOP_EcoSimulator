package view;

import controller.SimulationTime;
import model.Animal;
import model.AnimalState;
import model.Entity;
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
import model.herbivore.Rabbit;
import model.environment.Environment;
import model.plant.Algae;
import model.plant.Berry;
import model.plant.Grass;
import model.plant.GrowthStage;
import model.plant.Mushroom;
import model.plant.Plant;
import model.environment.Bush;
import model.plant.SmallTree;
import model.environment.OldTree;
import model.plant.TreePlant;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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
    private boolean basicMode = false;
    private String buildMode = "FOOD_PLANT";
    

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
    }

    public void setWorldSize(double width, double height) {
        this.worldWidth = width;
        this.worldHeight = height;
    }

    public double getWorldWidth() {
        return worldWidth;
    }

    public double getWorldHeight() {
        return worldHeight;
    }

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
            if (!(e instanceof Animal)) {
                drawEntity(g2, e);
            }
        }

        for (Entity e : renderEntities) {
            if (e instanceof Animal) {
                drawEntity(g2, e);
            }
        }
        drawBuildUI(g2);
        drawInfoPanel(g2);
        g2.dispose();
    }

    private void drawInfoPanel(Graphics2D g) {

        if (entities == null) return;

        int rabbit = 0;
        int deer = 0;
        int boar = 0;
        int fox = 0;
        int wolf = 0;
        int grass = 0;

        String weather = "UNKNOWN";

        if (Environment.getInstance() != null
                && Environment.getInstance().getWeather() != null) {

            weather = Environment.getInstance()
                    .getWeather()
                    .getCurrentWeather()
                    .toString();
        }

        for(Entity e : entities) {

            if(e instanceof Rabbit) rabbit++;
            else if(e instanceof Deer) deer++;
            else if(e instanceof Boar) boar++;
            else if(e instanceof Fox) fox++;
            else if(e instanceof Wolf) wolf++;
            else if(e instanceof Grass) grass++;
        }

        g.setColor(new Color(0,0,0,180));
        g.fillRoundRect(10,10,180,190,10,10);

        g.setColor(Color.WHITE);

        g.drawString("Rabbit : " + rabbit,20,35);
        g.drawString("Deer : " + deer,20,55);
        g.drawString("Boar : " + boar,20,75);
        g.drawString("Fox : " + fox,20,95);
        g.drawString("Wolf : " + wolf,20,115);
        g.drawString("Grass : " + grass,20,135);
        g.drawString("Weather : " + weather,20,165);
    }

    private void drawBuildUI(Graphics2D g) {

        int panelHeight = getHeight();

        drawBuildButton(g, "Plant", 20, panelHeight - 80,
                buildMode.equals("FOOD_PLANT"));

        drawBuildButton(g, "Bush", 150, panelHeight - 80,
                buildMode.equals("BUSH"));

        drawBuildButton(g, "Tree", 280, panelHeight - 80,
                buildMode.equals("TREE"));
    }

    private void drawBuildButton(Graphics2D g,
                                String text,
                                int x,
                                int y,
                                boolean active) {

        g.setColor(active
                ? new Color(90, 180, 90)
                : new Color(40, 40, 40, 220));

        g.fillRoundRect(x, y, 110, 50, 18, 18);

        g.setColor(Color.WHITE);

        g.setFont(new Font("Arial", Font.BOLD, 16));

        FontMetrics fm = g.getFontMetrics();

        int tx = x + (110 - fm.stringWidth(text)) / 2;
        int ty = y + 31;

        g.drawString(text, tx, ty);
    }

    private void drawEntity(Graphics2D g, Entity e) {
        

        int cx = worldToScreenX(e.getPosition().getX());
        int cy = worldToScreenY(e.getPosition().getY());
        if (basicMode) {
            drawBasicEntity(g, e, cx, cy);
            return;
        }
        int size = Math.max(12, (int) (e.getSize() * 4 * renderScale));
        boolean drawn = false;

         if (e instanceof Grass) {

            g.setColor(Color.GREEN);
            g.fillOval(cx - 4, cy - 4, 8, 8);
            return;
        }

        if (e instanceof Bush) {

            g.setColor(Color.BLUE);
            g.fillOval(cx - 10, cy - 10, 20, 20);
            return;
        }

        if (e instanceof OldTree) {

            g.setColor(Color.RED);
            g.fillOval(cx - 12, cy - 12, 24, 24);
            return;
        }


        if (!(e instanceof Animal)) return;

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
        } else {
            drawDefault(g, cx, cy, size);
        }

        if (e instanceof Animal) {
            drawHealthBar(g, (Animal) e, cx, cy, size);
        }
    }

    private void drawBasicEntity(
            Graphics2D g,
            Entity e,
            int cx,
            int cy) {

        int size = Math.max(
                8,
                (int)(e.getSize() * 4 * renderScale)
        );

        if (e instanceof Rabbit) {
            g.setColor(Color.GREEN);

        } else if (e instanceof Deer) {
            g.setColor(Color.ORANGE);

        } else if (e instanceof Boar) {
            g.setColor(Color.GRAY);

        } else if (e instanceof Fox) {
            g.setColor(Color.RED);

        } else if (e instanceof Wolf) {
            g.setColor(Color.DARK_GRAY);

        } else if (e instanceof Grass) {
            g.setColor(Color.GREEN.darker());

        } else {
            g.setColor(Color.WHITE);
        }

        g.fillOval(
                cx - size/2,
                cy - size/2,
                size,
                size
        );
    }

    private void loadSprites() {
        loadEntitySpriteSet("hare", "Hare");
        loadEntitySpriteSet("fox", "Fox");
        loadEntitySpriteSet("boar", "Boar");
        loadEntitySpriteSet("deer", "Deer");
        loadEntitySpriteSet("black_grouse", "BlackGrouse");
        loadColumnAnimationSpriteSet("chicken", "assets/Entities/Chicken/Chicken_animation.png", 8);
        loadColumnAnimationSpriteSet("cow", "assets/Entities/Cow/Cow_animation.png", 8);
        loadColumnAnimationSpriteSet("pig", "assets/Entities/Pig/Pig_animation.png", 8);
        loadFishSpriteSet("fish_one", "Fish1_animation.png");
        loadFishSpriteSet("fish_two", "Fish2_animation.png");
        loadFishSpriteSet("fish_three", "Fish3_animation.png");
        loadFishSpriteSet("fish_four", "Fish4_animation.png");

        loadSprite("rabbit", "assets/Entities/Rabbit.png");
        loadSprite("wolf", "assets/Entities/Wolf.png");
    }

    private void loadPlantSprites() {
        PlantSpriteSet smallTree = new PlantSpriteSet();
        String plantsPath = "assets/Environment/Forest/Plants2.png";
        smallTree.put(GrowthStage.SEED, cropImage(plantsPath, 0, 0, 16, 16));
        smallTree.put(GrowthStage.SPROUT, cropImage(plantsPath, 16, 0, 16, 32));
        smallTree.put(GrowthStage.YOUNG, cropImage(plantsPath, 32, 0, 32, 32));
        smallTree.put(GrowthStage.MATURE, cropImage(plantsPath, 96, 0, 48, 48));
        smallTree.put(GrowthStage.OLD, cropImage(plantsPath, 144, 0, 64, 64));
        if (smallTree.hasAnyImage()) {
            plantSprites.put("small_tree", smallTree);
            plantSprites.put("oak", smallTree);
        }
        PlantSpriteSet pine = new PlantSpriteSet();
        pine.put(GrowthStage.SEED, cropImage(plantsPath, 0, 80, 16, 16));
        pine.put(GrowthStage.SPROUT, cropImage(plantsPath, 16, 80, 16, 16));
        pine.put(GrowthStage.YOUNG, cropImage(plantsPath, 80, 80, 32, 32));
        pine.put(GrowthStage.MATURE, cropImage(plantsPath, 128, 80, 48, 48));
        pine.put(GrowthStage.OLD, cropImage(plantsPath, 176, 80, 64, 64));
        if (pine.hasAnyImage()) {
            plantSprites.put("pine", pine);
        }
        PlantSpriteSet palm = new PlantSpriteSet();
        palm.put(GrowthStage.SEED, cropImage(plantsPath, 112, 208, 16, 16));
        palm.put(GrowthStage.SPROUT, cropImage(plantsPath, 144, 192, 32, 32));
        palm.put(GrowthStage.YOUNG, cropImage(plantsPath, 192, 176, 48, 48));
        palm.put(GrowthStage.MATURE, cropImage(plantsPath, 256, 160, 64, 64));
        palm.put(GrowthStage.OLD, cropImage(plantsPath, 336, 160, 64, 64));
        if (palm.hasAnyImage()) {
            plantSprites.put("palm", palm);
        }
        PlantSpriteSet vine = new PlantSpriteSet();
        String vinePath = "assets/Environment/Forest/Vine.png";
        vine.put(GrowthStage.SEED, cropImage(vinePath, 0, 0, 32, 48));
        vine.put(GrowthStage.SPROUT, cropImage(vinePath, 32, 0, 32, 48));
        vine.put(GrowthStage.YOUNG, cropImage(vinePath, 64, 0, 32, 48));
        vine.put(GrowthStage.MATURE, cropImage(vinePath, 96, 0, 32, 48));
        vine.put(GrowthStage.OLD, cropImage(vinePath, 96, 0, 32, 48));
        if (vine.hasAnyImage()) {
            plantSprites.put("vine", vine);
            plantSprites.put("nho", vine);
        }
    }

    private void loadSprite(String key, String path) {
        File file = new File(path);
        if (!file.exists()) return;

        try {
            sprites.put(key, ImageIO.read(file));
        } catch (IOException e) {
            System.err.println("Cannot load sprite: " + path);
        }
    }

    private void loadEntitySpriteSet(String key, String assetName) {
        String folderPath = "assets/Entities/" + assetName;

        EntitySpriteSet set = new EntitySpriteSet();
        set.idle = loadSpriteSheet(folderPath + "/" + assetName + "Idle.png", 4);
        set.walk = loadSpriteSheet(folderPath + "/" + assetName + "Walk.png", 4);
        set.run = loadSpriteSheet(folderPath + "/" + assetName + "Run.png", 4);
        set.attack = loadSpriteSheet(folderPath + "/" + assetName + "Attack.png", 4);
        set.hurt = loadSpriteSheet(folderPath + "/" + assetName + "Hurt.png", 4);
        set.death = loadSpriteSheet(folderPath + "/" + assetName + "Death.png", 4);
        set.shadow = loadImage(folderPath + "/" + assetName + "Shadow.png");

        if (set.hasAnyAnimation()) {
            entitySprites.put(key, set);
        }
    }

    private void loadFishSpriteSet(String key, String fileName) {
        EntitySpriteSet set = new EntitySpriteSet();
        set.walk = loadSpriteSheet("assets/Entities/Firsh/" + fileName, 1, 9);
        set.idle = set.walk;

        if (set.hasAnyAnimation()) {
            entitySprites.put(key, set);
        }
    }

    private void loadFixedFrameAnimationSpriteSet(String key, String path, int frameWidth, int frameHeight) {
        EntitySpriteSet set = new EntitySpriteSet();
        set.walk = loadFixedFrameSpriteSheet(path, frameWidth, frameHeight);
        set.idle = set.walk;

        if (set.hasAnyAnimation()) {
            entitySprites.put(key, set);
        }
    }

    private void loadColumnAnimationSpriteSet(String key, String path, int columns) {
        EntitySpriteSet set = new EntitySpriteSet();
        set.walk = loadColumnSpriteSheet(path, columns);
        set.idle = set.walk;
        set.directionFramesInFirstRow = true;

        if (set.hasAnyAnimation()) {
            entitySprites.put(key, set);
        }
    }

    private SpriteSheet loadSpriteSheet(String path, int rows) {
        if (path == null) return null;

        File file = new File(path);
        if (!file.exists()) return null;

        try {
            return new SpriteSheet(ImageIO.read(file), rows);
        } catch (IOException e) {
            System.err.println("Cannot load spritesheet: " + path);
            return null;
        }
    }

    private SpriteSheet loadSpriteSheet(String path, int rows, int columns) {
        if (path == null) return null;

        File file = new File(path);
        if (!file.exists()) return null;

        try {
            return new SpriteSheet(ImageIO.read(file), rows, columns);
        } catch (IOException e) {
            System.err.println("Cannot load spritesheet: " + path);
            return null;
        }
    }

    private SpriteSheet loadFixedFrameSpriteSheet(String path, int frameWidth, int frameHeight) {
        if (path == null) return null;

        File file = new File(path);
        if (!file.exists()) return null;

        try {
            return new SpriteSheet(ImageIO.read(file), frameWidth, frameHeight, true);
        } catch (IOException e) {
            System.err.println("Cannot load spritesheet: " + path);
            return null;
        }
    }

    private SpriteSheet loadColumnSpriteSheet(String path, int columns) {
        if (path == null) return null;

        File file = new File(path);
        if (!file.exists()) return null;

        try {
            BufferedImage image = ImageIO.read(file);
            int frameWidth = Math.max(1, image.getWidth() / columns);
            int frameHeight = frameWidth;
            int rows = Math.max(1, image.getHeight() / frameHeight);
            return new SpriteSheet(image, rows, columns);
        } catch (IOException e) {
            System.err.println("Cannot load spritesheet: " + path);
            return null;
        }
    }

    private boolean drawSprite(Graphics2D g, String key, int cx, int cy, int width, int height) {
        BufferedImage image = sprites.get(key);
        if (image == null) return false;

        g.drawImage(image, cx - width / 2, cy - height / 2, width, height, null);
        return true;
    }

    private boolean drawEntitySprite(Graphics2D g, String key, Entity entity, int cx, int cy, int width, int height) {
        EntitySpriteSet set = entitySprites.get(key);
        if (set == null) return false;

        if (set.shadow != null) {
            int shadowWidth = Math.max(12, width / 2);
            int shadowHeight = Math.max(6, height / 4);
            g.drawImage(set.shadow, cx - shadowWidth / 2, cy + height / 5, shadowWidth, shadowHeight, null);
        }

        SpriteSheet sheet = selectAnimation(set, entity);
        if (sheet == null) return false;

        int row = set.directionFramesInFirstRow ? 0 : getDirectionRow(entity);
        int frame = set.directionFramesInFirstRow
                ? getDirectionColumn(entity)
                : (int) ((System.currentTimeMillis() / 120) % sheet.columns);
        BufferedImage image = sheet.getFrame(frame, row);

        g.drawImage(image, cx - width / 2, cy - height / 2, width, height, null);
        return true;
    }

    private SpriteSheet selectAnimation(EntitySpriteSet set, Entity entity) {
        if (!entity.isAlive() && set.death != null) return set.death;
        if (!(entity instanceof Animal)) return set.idle;

        Animal animal = (Animal) entity;
        AnimalState state = animal.getCurrentState();
        double speed = animal.getVelocity().magnitude();

        if (state == AnimalState.DEAD && set.death != null) return set.death;
        if (state == AnimalState.ATTACKING && set.attack != null) return set.attack;
        if (state == AnimalState.HURT && set.hurt != null) return set.hurt;
        if (speed > animal.getSpeed() * 0.7 && set.run != null) return set.run;
        if (speed > 0.05 && set.walk != null) return set.walk;
        if (set.idle != null) return set.idle;
        if (set.walk != null) return set.walk;
        return set.run;
    }

    private int getDirectionRow(Entity entity) {
        if (!(entity instanceof Animal)) return 0;

        Animal animal = (Animal) entity;
        double vx = animal.getVelocity().getX();
        double vy = animal.getVelocity().getY();

        if (Math.abs(vx) > Math.abs(vy)) {
            return vx >= 0 ? 3 : 2;
        }
        return vy >= 0 ? 0 : 1;
    }

    private int getDirectionColumn(Entity entity) {
        if (!(entity instanceof Animal)) return 0;

        Animal animal = (Animal) entity;
        double vx = animal.getVelocity().getX();
        double vy = animal.getVelocity().getY();

        if (Math.abs(vx) > Math.abs(vy)) {
            return vx < 0 ? 2 : 3;
        }
        return vy < 0 ? 1 : 0;
    }

    private void drawRabbit(Graphics2D g, int cx, int cy, int size) {
        int bodyW = size;
        int bodyH = Math.max(12, size - 4);
        int x = cx - bodyW / 2;
        int y = cy - bodyH / 2;

        g.setColor(new Color(245, 245, 238));
        g.fillOval(x, y, bodyW, bodyH);
        g.fillOval(cx + size / 5, cy - size / 3, size / 2, size / 2);

        g.setColor(new Color(245, 245, 238));
        g.fillRoundRect(cx + size / 4, cy - size, size / 6, size / 2, 8, 8);
        g.fillRoundRect(cx + size / 2, cy - size, size / 6, size / 2, 8, 8);

        g.setColor(new Color(255, 178, 190));
        g.fillRoundRect(cx + size / 4 + 2, cy - size + 4, Math.max(2, size / 10), size / 3, 6, 6);
        g.fillRoundRect(cx + size / 2 + 2, cy - size + 4, Math.max(2, size / 10), size / 3, 6, 6);

        g.setColor(Color.BLACK);
        g.fillOval(cx + size / 2, cy - size / 5, 3, 3);
    }

    private void drawWolf(Graphics2D g, int cx, int cy, int size) {
        int bodyW = size + 6;
        int bodyH = Math.max(14, size - 2);
        int x = cx - bodyW / 2;
        int y = cy - bodyH / 2;

        g.setColor(new Color(91, 101, 111));
        g.fillOval(x, y, bodyW, bodyH);

        Polygon head = new Polygon();
        head.addPoint(cx + bodyW / 2 - 2, cy - bodyH / 3);
        head.addPoint(cx + bodyW / 2 + size / 2, cy);
        head.addPoint(cx + bodyW / 2 - 2, cy + bodyH / 3);
        g.fillPolygon(head);

        Polygon ear = new Polygon();
        ear.addPoint(cx + bodyW / 4, cy - bodyH / 2);
        ear.addPoint(cx + bodyW / 3, cy - bodyH);
        ear.addPoint(cx + bodyW / 2, cy - bodyH / 3);
        g.fillPolygon(ear);

        g.setColor(new Color(70, 78, 86));
        Polygon tail = new Polygon();
        tail.addPoint(x + 4, cy - 3);
        tail.addPoint(x - size / 2, cy - bodyH / 2);
        tail.addPoint(x, cy + bodyH / 3);
        g.fillPolygon(tail);

        g.setColor(Color.BLACK);
        g.fillOval(cx + bodyW / 2 + size / 4, cy - 3, 4, 4);
    }

    private void drawDefault(Graphics2D g, int cx, int cy, int size) {
        g.setColor(Color.WHITE);
        g.fillOval(cx - size / 2, cy - size / 2, size, size);
    }

    private void drawPlant(Graphics2D g, Plant plant) {
        int cx = worldToScreenX(plant.getPosition().getX());
        int cy = worldToScreenY(plant.getPosition().getY());
        int size = Math.max(4, (int) Math.round(plant.getSize() * 4 * renderScale));
        if (drawPlantSprite(g, plant.getSpeciesKey(), plant, cx, cy, size)) {
            return;
        }
        if (plant instanceof SmallTree || plant instanceof TreePlant) {
            if (!drawPlantSprite(g, "small_tree", plant, cx, cy, size)) {
                drawSmallTree(g, cx, cy, size, plant.getGrowthStage());
            }
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
        int width = Math.max(8, (int) Math.round(image.getWidth() * scale));
        int height = Math.max(8, (int) Math.round(image.getHeight() * scale));
        g.drawImage(image, cx - width / 2, cy - height + size / 4, width, height, null);
        return true;
    }
    private void drawSmallTree(Graphics2D g, int cx, int cy, int size, GrowthStage stage) {
        int trunkHeight = Math.max(6, size / 2);
        int trunkWidth = Math.max(3, size / 7);
        int canopySize = Math.max(8, size);
        if (stage == GrowthStage.SEED) {
            g.setColor(new Color(96, 67, 38));
            g.fillOval(cx - size / 8, cy - size / 8, Math.max(3, size / 4), Math.max(3, size / 4));
            return;
        }
        g.setColor(new Color(104, 70, 38));
        g.fillRoundRect(cx - trunkWidth / 2, cy - trunkHeight / 2, trunkWidth, trunkHeight, 4, 4);
        Color leafColor = stage == GrowthStage.OLD
                ? new Color(91, 128, 55)
                : new Color(42, 139, 61);
        g.setColor(leafColor);
        g.fillOval(cx - canopySize / 2, cy - trunkHeight / 2 - canopySize / 2, canopySize, canopySize);
        g.setColor(new Color(29, 104, 49));
        g.fillOval(cx - canopySize / 3, cy - trunkHeight / 2 - canopySize / 3, canopySize / 2, canopySize / 2);
    }
    private void drawGrass(Graphics2D g, int cx, int cy, int size) {
        g.setColor(new Color(55, 154, 58));
        int bladeHeight = Math.max(5, size);
        g.drawLine(cx, cy, cx, cy - bladeHeight);
        g.drawLine(cx, cy, cx - size / 3, cy - bladeHeight * 2 / 3);
        g.drawLine(cx, cy, cx + size / 3, cy - bladeHeight * 2 / 3);
    }
    private void drawBerry(Graphics2D g, int cx, int cy, int size, boolean hasFruits) {
        drawGrass(g, cx, cy, size);
        if (!hasFruits) return;
        g.setColor(new Color(182, 38, 62));
        int berrySize = Math.max(3, size / 4);
        g.fillOval(cx - berrySize, cy - size, berrySize, berrySize);
        g.fillOval(cx + berrySize / 2, cy - size * 3 / 4, berrySize, berrySize);
    }
    private void drawMushroom(Graphics2D g, int cx, int cy, int size) {
        int stemWidth = Math.max(3, size / 4);
        int stemHeight = Math.max(5, size / 2);
        g.setColor(new Color(232, 220, 185));
        g.fillRoundRect(cx - stemWidth / 2, cy - stemHeight / 2, stemWidth, stemHeight, 4, 4);
        g.setColor(new Color(185, 45, 48));
        g.fillArc(cx - size / 2, cy - stemHeight, size, size, 0, 180);
    }

    private void drawHealthBar(Graphics2D g, Animal animal, int cx, int cy, int size) {
        if (animal.getCurrentState() == AnimalState.DEAD) return;

        int barWidth = Math.max(18, size * 2);
        int barHeight = 4;
        int x = cx - barWidth / 2;
        int y = cy - size - 8;
        double hpPercent = Math.max(0, Math.min(1, animal.getHp() / animal.getMaxHp()));
        int fillWidth = (int) Math.round(barWidth * hpPercent);

        g.setColor(new Color(20, 20, 20, 180));
        g.fillRect(x - 1, y - 1, barWidth + 2, barHeight + 2);

        g.setColor(new Color(154, 35, 35));
        g.fillRect(x, y, barWidth, barHeight);

        g.setColor(new Color(58, 190, 74));
        g.fillRect(x, y, fillWidth, barHeight);
    }

    private void updateRenderTransform() {
        renderScale = 1.0;

        double scaledWorldWidth = worldWidth * renderScale;
        double scaledWorldHeight = worldHeight * renderScale;
        double overflowX = Math.max(0, scaledWorldWidth - getWidth());
        double overflowY = Math.max(0, scaledWorldHeight - getHeight());

        renderOffsetX = overflowX > 0
                ? -(int) Math.floor(overflowX * cameraFocusX)
                : (int) Math.floor((getWidth() - scaledWorldWidth) / 2.0);
        renderOffsetY = overflowY > 0
                ? -(int) Math.floor(overflowY * cameraFocusY)
                : (int) Math.floor((getHeight() - scaledWorldHeight) / 2.0);
    }

    private int worldToScreenX(double x) {
        return renderOffsetX + (int) Math.round(x * renderScale);
    }

    private int worldToScreenY(double y) {
        return renderOffsetY + (int) Math.round(y * renderScale);
    }

    private void drawWorldBounds(Graphics2D g) {
        int width = (int) Math.round(worldWidth * renderScale);
        int height = (int) Math.round(worldHeight * renderScale);

        if (forestTileMap.isLoaded()) {
            forestTileMap.draw(g, renderOffsetX, renderOffsetY, width, height, renderScale);
        } else {
            g.setColor(new Color(27, 112, 49));
            g.fillRect(renderOffsetX, renderOffsetY, width, height);
        }

        g.setColor(new Color(23, 82, 40));
        g.drawRect(renderOffsetX, renderOffsetY, width - 1, height - 1);
    }

    private void installMouseCamera() {
        MouseAdapter mouseCamera = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                updateCameraFocus(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                updateCameraFocus(e);
            }
        };

        addMouseMotionListener(mouseCamera);
    }

    private void installBuildControls() {

        addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {

                int panelWidth = getWidth();
                int panelHeight = getHeight();

                Rectangle plantBtn = new Rectangle(20, panelHeight - 80, 110, 50);
                Rectangle bushBtn = new Rectangle(150, panelHeight - 80, 110, 50);
                Rectangle treeBtn = new Rectangle(280, panelHeight - 80, 110, 50);

                // CLICK BUTTON
                if (plantBtn.contains(e.getPoint())) {
                    buildMode = "FOOD_PLANT";
                    repaint();
                    return;
                }

                if (bushBtn.contains(e.getPoint())) {
                    buildMode = "BUSH";
                    repaint();
                    return;
                }

                if (treeBtn.contains(e.getPoint())) {
                    buildMode = "TREE";
                    repaint();
                    return;
                }

                // CLICK WORLD
                double worldX = (e.getX() - renderOffsetX) / renderScale;
                double worldY = (e.getY() - renderOffsetY) / renderScale;

                Environment env = Environment.getInstance();

                if (env == null) return;

                try {

                    if (buildMode.equals("FOOD_PLANT")) {

                        Grass grass = new Grass(
                                new model.Vector2D(worldX, worldY)
                        );

                        grass.setRuntimePlaced(true);

                        env.queueEntity(grass);
                        System.out.println("Grass planted");
                    }

                    else if (buildMode.equals("BUSH")) {

                        Bush bush = new Bush(
                                new model.Vector2D(worldX, worldY),
                                18
                        );

                        bush.setRuntimePlaced(true);

                        env.queueEntity(bush);
                        System.out.println("Bush planted");
                    }

                    else if (buildMode.equals("TREE")) {

                        OldTree tree = new OldTree(
                                new model.Vector2D(worldX, worldY)
                        );

                        tree.setRuntimePlaced(true);

                        env.queueEntity(tree);
                        System.out.println("Tree planted");
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void updateCameraFocus(MouseEvent e) {
        int width = Math.max(1, getWidth());
        int height = Math.max(1, getHeight());

        cameraFocusX = clamp(e.getX() / (double) width, 0, 1);
        cameraFocusY = clamp(e.getY() / (double) height, 0, 1);
        repaint();
    }

    private void installTimelineControls() {
        timelinePanel = new JPanel();
        timelinePanel.setLayout(new BorderLayout(8, 4));
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

            viewModeButton.setText(
                    basicMode
                            ? "Switch to Graphics"
                            : "Switch to Basic"
            );

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

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private BufferedImage loadImage(String path) {
        if (path == null) return null;

        File file = new File(path);
        if (!file.exists()) return null;

        try {
            return ImageIO.read(file);
        } catch (IOException e) {
            System.err.println("Cannot load image: " + path);
            return null;
        }
    }

    private BufferedImage cropImage(String path, int x, int y, int width, int height) {
        BufferedImage image = loadImage(path);
        if (image == null) return null;
        if (x < 0 || y < 0 || x + width > image.getWidth() || y + height > image.getHeight()) {
            return null;
        }
        return image.getSubimage(x, y, width, height);
    }
    private static class PlantSpriteSet {
        private final Map<GrowthStage, BufferedImage> images = new EnumMap<>(GrowthStage.class);
        private void put(GrowthStage stage, BufferedImage image) {
            if (image != null) {
                images.put(stage, image);
            }
        }
        private BufferedImage get(GrowthStage stage) {
            BufferedImage image = images.get(stage);
            if (image != null) return image;
            GrowthStage[] stages = GrowthStage.values();
            int stageIndex = stage.ordinal();
            for (int i = stageIndex - 1; i >= 0; i--) {
                image = images.get(stages[i]);
                if (image != null) return image;
            }
            for (int i = stageIndex + 1; i < stages.length; i++) {
                image = images.get(stages[i]);
                if (image != null) return image;
            }
            return null;
        }

        private boolean hasAnyImage() {
            return !images.isEmpty();
        }
    }

    private static class EntitySpriteSet {
        private SpriteSheet idle;
        private SpriteSheet walk;
        private SpriteSheet run;
        private SpriteSheet attack;
        private SpriteSheet hurt;
        private SpriteSheet death;
        private BufferedImage shadow;
        private boolean directionFramesInFirstRow;

        private boolean hasAnyAnimation() {
            return idle != null || walk != null || run != null || attack != null || hurt != null || death != null;
        }
    }

    private static class SpriteSheet {
        private final BufferedImage image;
        private final int columns;
        private final int rows;
        private final int frameWidth;
        private final int frameHeight;

        private SpriteSheet(BufferedImage image, int rows) {
            this.image = image;
            this.rows = rows;
            this.frameHeight = image.getHeight() / rows;
            this.frameWidth = frameHeight;
            this.columns = Math.max(1, image.getWidth() / frameWidth);
        }

        private SpriteSheet(BufferedImage image, int rows, int columns) {
            this.image = image;
            this.rows = rows;
            this.columns = columns;
            this.frameWidth = image.getWidth() / columns;
            this.frameHeight = image.getHeight() / rows;
        }

        private SpriteSheet(BufferedImage image, int frameWidth, int frameHeight, boolean fixedFrameSize) {
            this.image = image;
            this.frameWidth = frameWidth;
            this.frameHeight = frameHeight;
            this.columns = Math.max(1, image.getWidth() / frameWidth);
            this.rows = Math.max(1, image.getHeight() / frameHeight);
        }

        private BufferedImage getFrame(int column, int row) {
            int safeColumn = Math.max(0, Math.min(column, columns - 1));
            int safeRow = Math.max(0, Math.min(row, rows - 1));
            return image.getSubimage(
                    safeColumn * frameWidth,
                    safeRow * frameHeight,
                    frameWidth,
                    frameHeight
            );
        }
    }
}
