package model.strategy;

import model.Animal;
import model.AnimalState;
import model.Carcass;
import model.Entity;
import model.Vector2D;
import model.carnivore.Carnivore;
import model.environment.Environment;

import java.util.List;

public class ScavengerStrategy implements SurvivalStrategy {

    private final PassiveStrategy wanderLogic;

    public ScavengerStrategy() {
        this.wanderLogic = new PassiveStrategy();
    }

    @Override
    public void execute(Animal animal) {

        // Nếu có thú săn mồi gần đó -> bỏ chạy
        Entity predator = findNearestPredator(animal);

        if (predator != null) {
            animal.setCurrentState(AnimalState.FLEEING);

            moveAwayFrom(animal, predator, animal.getSpeed());
            return;
        }

        // Tìm xác chết gần nhất
        Entity carcass = findNearestCarcass(animal);

        if (carcass != null) {
            animal.setCurrentState(AnimalState.FORAGING);

            moveToward(animal, carcass, animal.getSpeed() * 0.7);
            return;
        }

        // Không có gì -> đi lang thang
        wanderLogic.execute(animal);
    }

    private Entity findNearestPredator(Animal animal) {
        Entity nearest = null;

        double minDistance = animal.getVisionRadius() * 0.8;

        List<Entity> allEntities = Environment.getInstance().getEntities();

        for (Entity entity : allEntities) {

            if (entity instanceof Carnivore && entity.isAlive()) {

                double distance =
                        animal.getPosition().distanceTo(entity.getPosition());

                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = entity;
                }
            }
        }

        return nearest;
    }

    private Entity findNearestCarcass(Animal animal) {
        Entity nearest = null;

        double minDistance = animal.getVisionRadius() * 1.5;

        List<Entity> allEntities = Environment.getInstance().getEntities();

        for (Entity entity : allEntities) {

            if (entity instanceof Carcass && entity.isAlive()) {

                double distance =
                        animal.getPosition().distanceTo(entity.getPosition());

                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = entity;
                }
            }
        }

        return nearest;
    }

    private void moveToward(
            Animal animal,
            Entity target,
            double speed
    ) {

        Vector2D direction = new Vector2D(
                target.getPosition().getX()
                        - animal.getPosition().getX(),

                target.getPosition().getY()
                        - animal.getPosition().getY()
        );

        applyVelocity(animal, direction, speed);
    }

    private void moveAwayFrom(
            Animal animal,
            Entity threat,
            double speed
    ) {

        Vector2D direction = new Vector2D(
                animal.getPosition().getX()
                        - threat.getPosition().getX(),

                animal.getPosition().getY()
                        - threat.getPosition().getY()
        );

        applyVelocity(animal, direction, speed);
    }

    private void applyVelocity(
            Animal animal,
            Vector2D direction,
            double speed
    ) {

        direction.normalize();

        animal.getVelocity().setX(direction.getX() * speed);
        animal.getVelocity().setY(direction.getY() * speed);
    }
}

