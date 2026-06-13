package view;

import controller.SimulationTime;
import model.Animal;
import model.Entity;
import model.apex.Bear;
import model.apex.Human;
import model.apex.Lion;
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
import java.util.List;

public class SimulationPanel extends JPanel {
    private List<Entity> entities;
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
    private BuildMode buildMode = BuildMode.FOOD_PLANT;
    private final SpriteSheetStore spriteStore;
    public static final int MAX_FLOCK_TICKS = 500;
    public static final int REST_TICKS = 150;

    // Constructor có Environment
    public SimulationPanel(Environment env) {
        this.env = env;
        this.spriteStore = new SpriteSheetStore();

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
    }

    // Constructor không có Environment (giữ tương thích)
    public SimulationPanel() {
        this.spriteStore = new SpriteSheetStore();
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

        BuildUIRenderer.draw(g2, getHeight(), buildMode);
        InfoPanelRenderer.draw(g2, entities);
        g2.dispose();
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
            BufferedImage s = spriteStore.getGrassSprite();
            if (s != null) g.drawImage(s, cx - 12, cy - 12, 24, 24, null);
            return;
        }
        if (e instanceof Bush) {
            BufferedImage s = spriteStore.getBushSprite();
            if (s != null) g.drawImage(s, cx - 24, cy - 24, 48, 48, null);
            return;
        }
        if (e instanceof OldTree) {
            BufferedImage s = spriteStore.getTreeSprite();
            if (s != null) g.drawImage(s, cx - 40, cy - 60, 80, 80, null);
            return;
        }
        if (e instanceof Rock) {
            BufferedImage s = spriteStore.getRockSprite();
            if (s != null) g.drawImage(s, cx - 28, cy - 28, 56, 56, null);
            return;
        }
        if (!(e instanceof Animal)) return;

        // Animals
        if (e instanceof Rabbit) {
            drawn = spriteStore.drawEntitySprite(g, "hare", e, cx, cy, size * 2, size * 2)
                    || drawSprite(g, "rabbit", cx, cy, size * 2, size * 2);
            if (!drawn) EntityRenderer.drawRabbit(g, cx, cy, size);
        } else if (e instanceof Chicken) {
            drawn = spriteStore.drawEntitySprite(g, "chicken", e, cx, cy, size * 2, size * 2);
            if (!drawn) EntityRenderer.drawDefault(g, cx, cy, size);
        } else if (e instanceof Cow) {
            drawn = spriteStore.drawEntitySprite(g, "cow", e, cx, cy, size * 2, size * 2);
            if (!drawn) EntityRenderer.drawDefault(g, cx, cy, size);
        } else if (e instanceof Pig) {
            drawn = spriteStore.drawEntitySprite(g, "pig", e, cx, cy, size * 2, size * 2);
            if (!drawn) EntityRenderer.drawDefault(g, cx, cy, size);
        } else if (e instanceof FishOne) {
            drawn = spriteStore.drawEntitySprite(g, "fish_one", e, cx, cy, size * 3, size * 2);
            if (!drawn) EntityRenderer.drawDefault(g, cx, cy, size);
        } else if (e instanceof FishTwo) {
            drawn = spriteStore.drawEntitySprite(g, "fish_two", e, cx, cy, size * 3, size * 2);
            if (!drawn) EntityRenderer.drawDefault(g, cx, cy, size);
        } else if (e instanceof FishThree) {
            drawn = spriteStore.drawEntitySprite(g, "fish_three", e, cx, cy, size * 3, size * 2);
            if (!drawn) EntityRenderer.drawDefault(g, cx, cy, size);
        } else if (e instanceof FishFour) {
            drawn = spriteStore.drawEntitySprite(g, "fish_four", e, cx, cy, size * 3, size * 2);
            if (!drawn) EntityRenderer.drawDefault(g, cx, cy, size);
        } else if (e instanceof BlackGrouse) {
            drawn = spriteStore.drawEntitySprite(g, "black_grouse", e, cx, cy, size * 2, size * 2);
            if (!drawn) EntityRenderer.drawDefault(g, cx, cy, size);
        } else if (e instanceof Wolf) {
            drawn = spriteStore.drawEntitySprite(g, "fox", e, cx, cy, size * 2, size * 2)
                    || drawSprite(g, "wolf", cx, cy, size * 2, size * 2);
            if (!drawn) EntityRenderer.drawWolf(g, cx, cy, size);
        } else if (e instanceof Fox) {
            drawn = spriteStore.drawEntitySprite(g, "fox", e, cx, cy, size * 2, size * 2);
            if (!drawn) EntityRenderer.drawWolf(g, cx, cy, size);
        } else if (e instanceof Boar) {
            drawn = spriteStore.drawEntitySprite(g, "boar", e, cx, cy, size * 2, size * 2);
            if (!drawn) EntityRenderer.drawDefault(g, cx, cy, size);
        } else if (e instanceof Deer) {
            drawn = spriteStore.drawEntitySprite(g, "deer", e, cx, cy, size * 2, size * 2);
            if (!drawn) EntityRenderer.drawDefault(g, cx, cy, size);
        } else if (e instanceof Goat) {
            int spriteSize = spritePixels(size, 1.75);
            drawn = spriteStore.drawEntitySprite(g, "goat", e, cx, cy, spriteSize, spriteSize);
            if (!drawn) EntityRenderer.drawDefault(g, cx, cy, size);
        } else if (e instanceof Horse) {
            int spriteSize = spritePixels(size, 2.3);
            drawn = spriteStore.drawEntitySprite(g, "horse", e, cx, cy, spriteSize, spriteSize);
            if (!drawn) EntityRenderer.drawDefault(g, cx, cy, size);
        } else if (e instanceof Cheetah) {
            int spriteSize = spritePixels(size, 2.15);
            drawn = spriteStore.drawEntitySprite(g, "cheetah", e, cx, cy, spriteSize, spriteSize);
            if (!drawn) EntityRenderer.drawDefault(g, cx, cy, size);
        } else if (e instanceof Bear) {
            int spriteSize = spritePixels(size, 1.5);
            drawn = spriteStore.drawEntitySprite(g, "bear", e, cx, cy, spriteSize, spriteSize);
            if (!drawn) EntityRenderer.drawDefault(g, cx, cy, size);
        } else if (e instanceof Lion) {
            int spriteSize = spritePixels(size, 1.45);
            drawn = spriteStore.drawEntitySprite(g, "lion", e, cx, cy, spriteSize, spriteSize);
            if (!drawn) EntityRenderer.drawDefault(g, cx, cy, size);
        } else if (e instanceof Human) {
            int spriteSize = spritePixels(size, 1.6);
            drawn = spriteStore.drawEntitySprite(g, "human", e, cx, cy, spriteSize, spriteSize);
            if (!drawn) EntityRenderer.drawDefault(g, cx, cy, size);
        } else if (e instanceof Elephant) {
            g.setColor(Color.GRAY);
            g.fillOval(cx - size, cy - size, size * 2, size * 2);
        } else {
            EntityRenderer.drawDefault(g, cx, cy, size);
        }

        if (e instanceof Animal) EntityRenderer.drawHealthBar(g, (Animal) e, cx, cy, size);
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
        if (e instanceof Rabbit)   { g.setColor(Color.PINK);                    g.fillOval(cx - size, cy - size, size * 2, size * 2); return; }
        if (e instanceof Deer)     { g.setColor(Color.ORANGE);                  g.fillOval(cx - size, cy - size, size * 2, size * 2); return; }
        if (e instanceof Boar)     { g.setColor(new Color(120, 70, 20));        g.fillOval(cx - size, cy - size, size * 2, size * 2); return; }
        if (e instanceof Elephant) { g.setColor(Color.GRAY);                    g.fillOval(cx - size, cy - size, size * 2, size * 2); return; }
        if (e instanceof Goat)     { g.setColor(new Color(232, 232, 220));      g.fillOval(cx - size, cy - size, size * 2, size * 2); return; }
        if (e instanceof Horse)    { g.setColor(new Color(112, 70, 38));        g.fillOval(cx - size, cy - size, size * 2, size * 2); return; }
        if (e instanceof Fox)      { g.setColor(Color.RED);                     g.fillRect(cx - size, cy - size, size * 2, size * 2); return; }
        if (e instanceof Wolf)     { g.setColor(Color.DARK_GRAY);               g.fillRect(cx - size, cy - size, size * 2, size * 2); return; }
        if (e instanceof Cheetah)  { g.setColor(new Color(218, 172, 73));       g.fillRect(cx - size, cy - size, size * 2, size * 2); return; }
        if (e instanceof Lion)     { g.setColor(new Color(201, 139, 49));       g.fillRect(cx - size, cy - size, size * 2, size * 2); return; }
        if (e instanceof Bear)     { g.setColor(new Color(92, 58, 36));         g.fillRect(cx - size, cy - size, size * 2, size * 2); return; }
        if (e instanceof model.aquatic.Fish) {
            g.setColor(Color.CYAN);
            Polygon d = new Polygon();
            d.addPoint(cx, cy - size); d.addPoint(cx + size, cy); d.addPoint(cx, cy + size); d.addPoint(cx - size, cy);
            g.fillPolygon(d); return;
        }
        if (e instanceof Human)   { g.setColor(new Color(60, 120, 210)); g.fillRoundRect(cx - size, cy - size, size * 2, size * 2, 8, 8); return; }
        if (e instanceof Bush)    { g.setColor(Color.BLUE);              g.fillRoundRect(cx - size, cy - size, size * 2, size * 2, 6, 6); return; }
        if (e instanceof OldTree) { g.setColor(new Color(120, 70, 20));  g.fillRect(cx - size, cy - size, size * 2, size * 2); return; }
        g.setColor(Color.BLACK); g.fillOval(cx - 4, cy - 4, 8, 8);
    }

    private void drawTriangle(Graphics2D g, int cx, int cy, int size) {
        Polygon p = new Polygon();
        p.addPoint(cx, cy - size); p.addPoint(cx - size, cy + size); p.addPoint(cx + size, cy + size);
        g.fillPolygon(p);
    }

    // ===================== DRAW HELPERS =====================

    private boolean drawSprite(Graphics2D g, String key, int cx, int cy, int w, int h) {
        BufferedImage image = SpriteManager.get(key);
        if (image == null) return false;
        g.drawImage(image, cx - w / 2, cy - h / 2, w, h, null);
        return true;
    }

    private void drawPlant(Graphics2D g, Plant plant) {
        int cx = worldToScreenX(plant.getPosition().getX());
        int cy = worldToScreenY(plant.getPosition().getY());
        int size = Math.max(4, (int) Math.round(plant.getSize() * 4 * renderScale));
        if (spriteStore.drawPlantSprite(g, plant.getSpeciesKey(), plant, cx, cy, size)) return;
        if (plant instanceof SmallTree || plant instanceof TreePlant) {
            if (!spriteStore.drawPlantSprite(g, "small_tree", plant, cx, cy, size))
                PlantRenderer.drawSmallTree(g, cx, cy, size, plant.getGrowthStage());
        } else if (plant instanceof Grass || plant instanceof Algae) {
            PlantRenderer.drawGrass(g, cx, cy, size);
        } else if (plant instanceof Berry) {
            PlantRenderer.drawBerry(g, cx, cy, size, ((Berry) plant).hasFruits());
        } else if (plant instanceof Mushroom) {
            PlantRenderer.drawMushroom(g, cx, cy, size);
        } else {
            PlantRenderer.drawGrass(g, cx, cy, size);
        }
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
                Rectangle plantBtn = new Rectangle(20,  ph - 80, 110, 50);
                Rectangle bushBtn  = new Rectangle(150, ph - 80, 110, 50);
                Rectangle treeBtn  = new Rectangle(280, ph - 80, 110, 50);
                Rectangle rockBtn  = new Rectangle(410, ph - 80, 110, 50);

                if (plantBtn.contains(e.getPoint())) { buildMode = BuildMode.FOOD_PLANT; repaint(); return; }
                if (bushBtn.contains(e.getPoint()))  { buildMode = BuildMode.BUSH;       repaint(); return; }
                if (treeBtn.contains(e.getPoint()))  { buildMode = BuildMode.TREE;       repaint(); return; }
                if (rockBtn.contains(e.getPoint()))  { buildMode = BuildMode.ROCK;       repaint(); return; }

                double worldX = (e.getX() - renderOffsetX) / renderScale;
                double worldY = (e.getY() - renderOffsetY) / renderScale;
                Environment env = Environment.getInstance();
                if (env == null) return;
                try {
                    BuildPlacementService.place(buildMode, worldX, worldY, env);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
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
}