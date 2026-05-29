package model.strategy;

import model.Animal;
import model.AnimalState;
import model.Entity;
import model.Vector2D;
import model.environment.Environment;
import model.plant.Grass;

import java.util.List;

public class AggressiveStrategy implements SurvivalStrategy {

    private final PassiveStrategy wanderLogic;

    public AggressiveStrategy() {
        this.wanderLogic = new PassiveStrategy();
    }

    @Override
    public void execute(Animal animal) {

        Entity target = findNearestFood(animal);

        // Có thức ăn -> lao tới bất chấp
        if (target != null) {

            animal.setCurrentState(AnimalState.CHASING);

            moveToward(
                    animal,
                    target,
                    animal.getSpeed()
            );

            return;
        }

        // Không có gì -> đi lang thang
        wanderLogic.execute(animal);
    }

    private Entity findNearestFood(Animal animal) {

        Entity nearest = null;

        double minDistance =
                animal.getVisionRadius() * 1.5;

        List<Entity> allEntities =
                Environment.getInstance().getEntities();

        for (Entity entity : allEntities) {

            if (entity instanceof Grass
                    && entity.isAlive()) {

                double distance =
                        animal.getPosition()
                                .distanceTo(entity.getPosition());

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

        direction.normalize();

        animal.getVelocity().setX(
                direction.getX() * speed
        );

        animal.getVelocity().setY(
                direction.getY() * speed
        );
    }
}
