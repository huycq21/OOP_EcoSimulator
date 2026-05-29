package model.strategy;

import model.Animal;
import model.AnimalState;
import model.Entity;
import model.Vector2D;
import model.carnivore.Carnivore;
import model.environment.Environment;

import java.util.List;

public class DefensiveStrategy implements SurvivalStrategy {

    private final PassiveStrategy wanderLogic;

    public DefensiveStrategy() {
        this.wanderLogic = new PassiveStrategy();
    }

    @Override
    public void execute(Animal animal) {

        Entity predator = findNearestPredator(animal);

        // Predator quá gần -> chạy mạnh
        if (predator != null) {

            double distance =
                    animal.getPosition()
                            .distanceTo(predator.getPosition());

            // Danger zone
            if (distance < animal.getVisionRadius() * 0.4) {

                animal.setCurrentState(AnimalState.FLEEING);

                moveAwayFrom(
                        animal,
                        predator,
                        animal.getSpeed()
                );

                return;
            }

            // Warning zone
            if (distance < animal.getVisionRadius()) {

                animal.setCurrentState(AnimalState.ALERT);

                moveAwayFrom(
                        animal,
                        predator,
                        animal.getSpeed() * 0.45
                );

                return;
            }
        }

        // Không nguy hiểm -> đi chậm
        wanderLogic.execute(animal);

        animal.getVelocity().setX(
                animal.getVelocity().getX() * 0.6
        );

        animal.getVelocity().setY(
                animal.getVelocity().getY() * 0.6
        );
    }

    private Entity findNearestPredator(Animal animal) {

        Entity nearest = null;

        double minDistance = animal.getVisionRadius();

        List<Entity> allEntities =
                Environment.getInstance().getEntities();

        for (Entity entity : allEntities) {

            if (entity instanceof Carnivore
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

        direction.normalize();

        animal.getVelocity().setX(
                direction.getX() * speed
        );

        animal.getVelocity().setY(
                direction.getY() * speed
        );
    }
}
