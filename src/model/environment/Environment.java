package model.environment;

import model.Animal;
import model.Entity;
import model.Vector2D;
import model.domestic.DomesticAnimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import controller.CollisionHandler;
import util.SoundManager;
import javax.sound.sampled.Clip;

public abstract class Environment {
    private static Environment activeEnvironment;
    private QuadTree currentQuadTree; 
    private final List<Entity> pendingEntities = new ArrayList<>();

    protected List<Entity> entities;
    protected List<MapCollider> mapColliders;
    protected List<MapCollider> wicketColliders;
    protected List<MapCollider> waterZones;
    protected Map<String, List<MapCollider>> animalPens;
    protected MapCollider mapBounds;
    protected Weather weather;
    protected double width;
    protected double height;
    private WeatherType lastWeather;
    private Clip rainClip;

    // --- VÒNG LẶP CỐT LÕI (GAME LOOP) ---
    public void update() {
        weather.update();
        WeatherType currentWeather = weather.getCurrentWeather();

        if (currentWeather != lastWeather) {

            boolean wasRaining =
                    lastWeather == WeatherType.RAINY;

            boolean isRaining =
                    currentWeather == WeatherType.RAINY;

            if (!wasRaining && isRaining) {

                rainClip =
                        SoundManager.playLoop("rain.wav");

            } else if (wasRaining && !isRaining) {

                SoundManager.stopSound(rainClip);
                rainClip = null;
            }

            lastWeather = currentWeather;

            System.out.println(
                "Weather changed from "
                + lastWeather
                + " to "
                + currentWeather
            );
        }
        // 1. [QUAN TRỌNG NHẤT] ĐẬP CÂY CŨ, XÂY CÂY MỚI Ở ĐẦU MỖI KHUNG HÌNH!
        Rectangle mapBoundary = new Rectangle(width / 2, height / 2, width / 2, height / 2);
        currentQuadTree = new QuadTree(mapBoundary, 4);
        
        // Nhét thú vào cây để làm radar chung cho mọi class
        for (Entity e : entities) {
            if (e.isAlive()) {
                currentQuadTree.insert(e);
            }
        }

        List<Entity> entitiesToRemove = new ArrayList<>();

        for (Entity entity : entities) {
            double previousX = entity.getPosition().getX();
            double previousY = entity.getPosition().getY();

            // 2. Cập nhật logic (Bây giờ bọn Gấu/Thợ săn gọi getInstance().getQuadTree() thoải mái)
            entity.update(); 

            // 3. Chặn không cho ra khỏi map
            keepWithinBounds(entity);
            resolveWaterAccess(entity, previousX, previousY);
            resolvePenAccess(entity, previousX, previousY);
            resolveMapCollisions(entity);
            resolveWicketCollisions(entity);

            if (!entity.isAlive()) {
                entitiesToRemove.add(entity);
            }
        }
        
        // 4. Xử lý va chạm
        CollisionHandler.processCollisions(this); 
        
        // 5. Dọn dẹp xác chết
        entities.removeAll(entitiesToRemove);
        entities.removeIf(entity -> !entity.isAlive());

        entities.addAll(pendingEntities);
        pendingEntities.clear();
    }

    // --- HÀM HỖ TRỢ VẬT LÝ ---
    protected void keepWithinBounds(Entity entity) {
        double x = entity.getPosition().getX();
        double y = entity.getPosition().getY();
        double radius = getCollisionRadius(entity);
        boolean changed = false;

        if (x < radius) { x = radius; changed = true; } 
        else if (x > width - radius) { x = width - radius; changed = true; }

        if (y < radius) { y = radius; changed = true; } 
        else if (y > height - radius) { y = height - radius; changed = true; }

        if (changed) {
            entity.getPosition().setX(x);
            entity.getPosition().setY(y);
        }
    }

    protected void resolveMapCollisions(Entity entity) {
        if (!(entity instanceof Animal)) return;

        double radius = getCollisionRadius(entity);
        boolean hitMap = false;

        if (mapBounds != null) {
            hitMap = mapBounds.clampCircleInside(entity.getPosition(), radius);
        }

        for (MapCollider collider : mapColliders) {
            if (collider.resolveCircleCollision(entity.getPosition(), radius)) {
                hitMap = true;
            }
        }

        if (hitMap) {
            Animal animal = (Animal) entity;
            animal.getVelocity().setX(animal.getVelocity().getX() * -0.25);
            animal.getVelocity().setY(animal.getVelocity().getY() * -0.25);
        }
    }

    protected void resolveWicketCollisions(Entity entity) {
        if (!(entity instanceof Animal) || entity instanceof DomesticAnimal) return;

        double radius = getCollisionRadius(entity);
        boolean hitWicket = false;

        for (MapCollider collider : wicketColliders) {
            if (collider.resolveCircleCollision(entity.getPosition(), radius)) {
                hitWicket = true;
            }
        }

        if (hitWicket) {
            Animal animal = (Animal) entity;
            animal.getVelocity().setX(animal.getVelocity().getX() * -0.25);
            animal.getVelocity().setY(animal.getVelocity().getY() * -0.25);
        }
    }

    protected void resolveWaterAccess(Entity entity, double previousX, double previousY) {
        if (waterZones.isEmpty() || !(entity instanceof Animal)) return;

        Animal animal = (Animal) entity;
        boolean inWater = isInWaterZone(entity.getPosition(), getCollisionRadius(entity));

        if ((!animal.canEnterWater() && inWater) || (animal.requiresWater() && !inWater)) {
            entity.getPosition().setX(previousX);
            entity.getPosition().setY(previousY);
            animal.getVelocity().setX(animal.getVelocity().getX() * -0.35);
            animal.getVelocity().setY(animal.getVelocity().getY() * -0.35);
        }
    }

    protected void resolvePenAccess(Entity entity, double previousX, double previousY) {
        if (!(entity instanceof DomesticAnimal)) return;

        DomesticAnimal domesticAnimal = (DomesticAnimal) entity;
        if (isInAnimalPen(domesticAnimal.getPenLayerName(), entity.getPosition(), getCollisionRadius(entity))) {
            return;
        }

        entity.getPosition().setX(previousX);
        entity.getPosition().setY(previousY);
        domesticAnimal.getVelocity().setX(domesticAnimal.getVelocity().getX() * -0.35);
        domesticAnimal.getVelocity().setY(domesticAnimal.getVelocity().getY() * -0.35);
    }

    public boolean isCircleBlocked(Vector2D position, double radius) {
        return isCircleBlocked(position, radius, true);
    }

    public boolean isCircleBlocked(Vector2D position, double radius, boolean includeWickets) {
        if (mapBounds != null && !mapBounds.containsCircle(position, radius)) return true;

        for (MapCollider collider : mapColliders) {
            Vector2D testPosition = new Vector2D(position.getX(), position.getY());
            if (collider.resolveCircleCollision(testPosition, radius)) return true;
        }

        if (includeWickets) {
            for (MapCollider collider : wicketColliders) {
                Vector2D testPosition = new Vector2D(position.getX(), position.getY());
                if (collider.resolveCircleCollision(testPosition, radius)) return true;
            }
        }
        return false;
    }

    public boolean isInWaterZone(Vector2D position, double radius) {
        for (MapCollider waterZone : waterZones) {
            if (waterZone.containsCircle(position, radius)) return true;
        }
        return false;
    }

    public Vector2D randomWaterPosition(Random random, double radius) {
        if (waterZones.isEmpty()) return null;

        for (int i = 0; i < 120; i++) {
            MapCollider waterZone = chooseWaterZoneByArea(random);
            Vector2D position = waterZone.randomPointInside(random, radius);
            if (isInWaterZone(position, radius) && !isCircleBlocked(position, radius)) {
                return position;
            }
        }

        return null;
    }

    public Vector2D randomPenPosition(String penLayerName, Random random, double radius) {
        List<MapCollider> pens = animalPens.get(penLayerName);
        if (pens == null || pens.isEmpty()) return null;

        for (int i = 0; i < 120; i++) {
            MapCollider pen = chooseColliderByArea(pens, random);
            Vector2D position = pen.randomPointInside(random, radius);
            if (isInAnimalPen(penLayerName, position, radius) && !isCircleBlocked(position, radius, false)) {
                return position;
            }
        }

        return null;
    }

    private MapCollider chooseWaterZoneByArea(Random random) {
        return chooseColliderByArea(waterZones, random);
    }

    private MapCollider chooseColliderByArea(List<MapCollider> colliders, Random random) {
        double totalArea = 0;
        for (MapCollider collider : colliders) {
            totalArea += collider.getArea();
        }

        double selectedArea = random.nextDouble() * Math.max(1.0, totalArea);
        double currentArea = 0;
        for (MapCollider waterZone : colliders) {
            currentArea += waterZone.getArea();
            if (selectedArea <= currentArea) {
                return waterZone;
            }
        }

        return colliders.get(colliders.size() - 1);
    }

    public Vector2D randomOpenPosition(Random random, double radius) {
        double margin = Math.max(60, radius * 2);
        for (int i = 0; i < 80; i++) {
            double x = margin + random.nextDouble() * Math.max(1, width - margin * 2);
            double y = margin + random.nextDouble() * Math.max(1, height - margin * 2);
            Vector2D position = new Vector2D(x, y);
            if (!isCircleBlocked(position, radius) && !isInWaterZone(position, radius)) {
                return position;
            }
        }

        return new Vector2D(width / 2.0, height / 2.0);
    }

    private double getCollisionRadius(Entity entity) {
        return Math.max(4.0, entity.getSize());
    }

    public void addMapCollider(MapCollider collider) {
        mapColliders.add(collider);
    }

    public void addWicketCollider(MapCollider collider) {
        wicketColliders.add(collider);
    }

    public void setMapBounds(MapCollider mapBounds) {
        this.mapBounds = mapBounds;
    }

    public void addWaterZone(MapCollider waterZone) {
        waterZones.add(waterZone);
    }

    public void addAnimalPen(String penLayerName, MapCollider penZone) {
        animalPens.computeIfAbsent(penLayerName, key -> new ArrayList<>()).add(penZone);
    }

    private boolean isInAnimalPen(String penLayerName, Vector2D position, double radius) {
        List<MapCollider> pens = animalPens.get(penLayerName);
        if (pens == null) return false;

        for (MapCollider pen : pens) {
            if (pen.containsCircle(position, radius)) return true;
        }
        return false;
    }

    public void addEntity(Entity entity) {
        pendingEntities.add(entity);
    }
    public void removeEntity(Entity entity) {
        entities.remove(entity);
    }
    public List<Entity> getEntities() {
        return entities;
    }
    public double getWidth() {
        return width;
    }
    public double getHeight() {
        return height;
    }

    public QuadTree getQuadTree() {
        return currentQuadTree;
    }
    public void setQuadTree(QuadTree qTree) {
        this.currentQuadTree = qTree;
    }
    public Environment(double width, double height) {
        this.width = width;
        this.height = height;
        this.entities = new ArrayList<>();
        this.mapColliders = new ArrayList<>();
        this.wicketColliders = new ArrayList<>();
        this.waterZones = new ArrayList<>();
        this.animalPens = new HashMap<>();
        this.weather = new Weather();
        this.lastWeather = weather.getCurrentWeather();
    }

    public static void setActiveEnvironment(Environment env) {
        activeEnvironment = env;
    }

    public static Environment getInstance() {
        return activeEnvironment;
    }

    public synchronized void queueEntity(Entity entity) {
        pendingEntities.add(entity);
    }

    public TerrainType getTerrainAt(Vector2D position) {

        if (isInWaterZone(position, 0)) {
            return TerrainType.WATER;
        }

        return TerrainType.FOREST;
    }

    public Weather getWeather() {
        return weather;
    }
}


