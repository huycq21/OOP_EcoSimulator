package view;

import model.Animal;
import model.AnimalState;
import model.Entity;
import model.carnivore.Fox;
import model.carnivore.Wolf;
import model.herbivore.BlackGrouse;
import model.herbivore.Boar;
import model.herbivore.Deer;
import model.herbivore.Rabbit;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

public class SimulationPanel extends JPanel {
    private List<Entity> entities;
    private final Map<String, BufferedImage> sprites;
    private final Map<String, EntitySpriteSet> entitySprites;
    private double worldWidth;
    private double worldHeight;
    private double renderScale;
    private int renderOffsetX;
    private int renderOffsetY;
    private final ForestTileMap forestTileMap;

    public SimulationPanel() {
        this.sprites = new HashMap<>();
        this.entitySprites = new HashMap<>();
        this.forestTileMap = new ForestTileMap("assets/Environment/Forest/Forest.tmx");
        this.worldWidth = forestTileMap.isLoaded() ? forestTileMap.getPixelWidth() : 800;
        this.worldHeight = forestTileMap.isLoaded() ? forestTileMap.getPixelHeight() : 600;
        this.renderScale = 1.0;
        setBackground(new Color(34, 139, 34));
        loadSprites();
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

        g2.dispose();
    }

    private void drawEntity(Graphics2D g, Entity e) {
        if (!(e instanceof Animal)) return;

        int cx = worldToScreenX(e.getPosition().getX());
        int cy = worldToScreenY(e.getPosition().getY());
        int size = Math.max(12, (int) (e.getSize() * 4 * renderScale));
        boolean drawn = false;

        if (e instanceof Rabbit) {
            drawn = drawEntitySprite(g, "hare", e, cx, cy, size * 2, size * 2)
                    || drawSprite(g, "rabbit", cx, cy, size * 2, size * 2);
            if (!drawn) drawRabbit(g, cx, cy, size);
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

    private void loadSprites() {
        loadEntitySpriteSet("hare", "Hare");
        loadEntitySpriteSet("fox", "Fox");
        loadEntitySpriteSet("boar", "Boar");
        loadEntitySpriteSet("deer", "Deer");
        loadEntitySpriteSet("black_grouse", "BlackGrouse");

        loadSprite("rabbit", "assets/Entities/Rabbit.png");
        loadSprite("wolf", "assets/Entities/Wolf.png");
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

        int row = getDirectionRow(entity);
        int frame = (int) ((System.currentTimeMillis() / 120) % sheet.columns);
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
        double scaleX = getWidth() / worldWidth;
        double scaleY = getHeight() / worldHeight;
        renderScale = Math.max(scaleX, scaleY);
        renderOffsetX = (int) Math.floor((getWidth() - worldWidth * renderScale) / 2.0);
        renderOffsetY = (int) Math.floor((getHeight() - worldHeight * renderScale) / 2.0);
    }

    private int worldToScreenX(double x) {
        return renderOffsetX + (int) Math.round(x * renderScale);
    }

    private int worldToScreenY(double y) {
        return renderOffsetY + (int) Math.round(y * renderScale);
    }

    private void drawWorldBounds(Graphics2D g) {
        int width = (int) Math.ceil(worldWidth * renderScale) + 2;
        int height = (int) Math.ceil(worldHeight * renderScale) + 2;

        if (forestTileMap.isLoaded()) {
            forestTileMap.draw(g, renderOffsetX, renderOffsetY, width, height, renderScale);
        } else {
            g.setColor(new Color(27, 112, 49));
            g.fillRect(renderOffsetX, renderOffsetY, width, height);
        }

        g.setColor(new Color(23, 82, 40));
        g.drawRect(renderOffsetX, renderOffsetY, width - 1, height - 1);
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

    private static class EntitySpriteSet {
        private SpriteSheet idle;
        private SpriteSheet walk;
        private SpriteSheet run;
        private SpriteSheet attack;
        private SpriteSheet hurt;
        private SpriteSheet death;
        private BufferedImage shadow;

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
