package model.strategy;

import model.Animal;
import model.AnimalState;
import model.Entity;
import model.Vector2D;
import model.carnivore.Carnivore;
import model.environment.Environment;
import model.herbivore.Herbivore;
import model.plant.Grass;

import java.util.List;

public class ForagingStrategy implements SurvivalStrategy {
    private final PassiveStrategy wanderLogic;

    public ForagingStrategy() {
        this.wanderLogic = new PassiveStrategy();
    }

    @Override
    public void execute(Animal herbivore) {
        Entity predator = findNearestPredator(herbivore);
        if (predator != null) {
            herbivore.setCurrentState(AnimalState.FLEEING);
            moveAwayFrom(herbivore, predator, herbivore.getSpeed());
            return;
        }

        boolean hungry = herbivore.getEnergy() < herbivore.getMaxEnergy() * 0.65;
        Entity food = hungry ? findNearestGrass(herbivore) : findNearbyGrass(herbivore);
        if (food != null && hungry) {
            herbivore.setCurrentState(AnimalState.FORAGING);
            moveToward(herbivore, food, herbivore.getSpeed() * 0.65);
            return;
        }

        wanderLogic.execute(herbivore);
    }

    private Entity findNearestPredator(Animal herbivore) {
        Entity nearest = null;
        double minDistance = getPredatorDetectionRadius(herbivore);
        List<Entity> allEntities = Environment.getInstance().getEntities();

        for (Entity entity : allEntities) {
            if (entity instanceof Carnivore && entity.isAlive()) {
                double distance = herbivore.getPosition().distanceTo(entity.getPosition());
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = entity;
                }
            }
        }

        return nearest;
    }

    private double getPredatorDetectionRadius(Animal herbivore) {
        if (herbivore instanceof Herbivore) {
            return ((Herbivore) herbivore).getPredatorDetectionRadius();
        }
        return herbivore.getVisionRadius() * 0.75;
    }

    private Entity findNearbyGrass(Animal herbivore) {
        Entity nearest = null;
        double minDistance = herbivore.getSize() * 8;
        List<Entity> allEntities = Environment.getInstance().getEntities();

        for (Entity entity : allEntities) {
            if (entity instanceof Grass && entity.isAlive()) {
                double distance = herbivore.getPosition().distanceTo(entity.getPosition());
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = entity;
                }
            }
        }

        return nearest;
    }

    private Entity findNearestGrass(Animal herbivore) {
        Entity nearest = null;
        double minDistance = herbivore.getVisionRadius() * 2.0;
        List<Entity> allEntities = Environment.getInstance().getEntities();

        for (Entity entity : allEntities) {
            if (entity instanceof Grass && entity.isAlive()) {
                double distance = herbivore.getPosition().distanceTo(entity.getPosition());
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = entity;
                }
            }
        }

        return nearest;
    }

    private void moveToward(Animal animal, Entity target, double speed) {
        Vector2D direction = new Vector2D(
                target.getPosition().getX() - animal.getPosition().getX(),
                target.getPosition().getY() - animal.getPosition().getY()
        );
        applyVelocity(animal, direction, speed);
    }

    private void moveAwayFrom(Animal animal, Entity threat, double speed) {
        Vector2D direction = new Vector2D(
                animal.getPosition().getX() - threat.getPosition().getX(),
                animal.getPosition().getY() - threat.getPosition().getY()
        );
        applyVelocity(animal, direction, speed);
    }

    private void applyVelocity(Animal animal, Vector2D direction, double speed) {
        direction.normalize();
        animal.getVelocity().setX(direction.getX() * speed);
        animal.getVelocity().setY(direction.getY() * speed);
    }
}
