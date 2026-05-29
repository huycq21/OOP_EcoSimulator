package model.strategy;

import model.Animal;
import model.AnimalState;
import model.Entity;
import model.Vector2D;
import model.environment.Environment;

import java.util.ArrayList;
import java.util.List;

public class FlockingStrategy implements SurvivalStrategy {

    private static final double NEIGHBOR_RADIUS = 120;
    private static final double SEPARATION_DISTANCE = 35;

    @Override
    public void execute(Animal animal) {

        animal.setCurrentState(AnimalState.WANDERING);

        List<Animal> neighbors = findNeighbors(animal);

        // Không có ai gần -> đi lang thang nhẹ
        if (neighbors.isEmpty()) {

            animal.getVelocity().setX(
                    animal.getVelocity().getX() * 0.95
            );

            animal.getVelocity().setY(
                    animal.getVelocity().getY() * 0.95
            );

            return;
        }

        Vector2D separation = calculateSeparation(animal, neighbors);

        Vector2D alignment = calculateAlignment(neighbors);

        Vector2D cohesion = calculateCohesion(animal, neighbors);

        Vector2D finalVector = new Vector2D(0, 0);

        // Weight
        finalVector.setX(
                separation.getX() * 1.8
                        + alignment.getX() * 1.2
                        + cohesion.getX() * 1.0
        );

        finalVector.setY(
                separation.getY() * 1.8
                        + alignment.getY() * 1.2
                        + cohesion.getY() * 1.0
        );

        finalVector.normalize();

        animal.getVelocity().setX(
                finalVector.getX() * animal.getSpeed() * 0.6
        );

        animal.getVelocity().setY(
                finalVector.getY() * animal.getSpeed() * 0.6
        );
    }

    private List<Animal> findNeighbors(Animal animal) {

        List<Animal> neighbors = new ArrayList<>();

        List<Entity> allEntities =
                Environment.getInstance().getEntities();

        for (Entity entity : allEntities) {

            if (!(entity instanceof Animal)) continue;

            Animal other = (Animal) entity;

            if (other == animal) continue;

            if (!other.getClass().equals(animal.getClass())) continue;

            double distance =
                    animal.getPosition()
                            .distanceTo(other.getPosition());

            if (distance < NEIGHBOR_RADIUS) {
                neighbors.add(other);
            }
        }

        return neighbors;
    }

    private Vector2D calculateSeparation(
            Animal animal,
            List<Animal> neighbors
    ) {

        Vector2D steer = new Vector2D(0, 0);

        for (Animal other : neighbors) {

            double distance =
                    animal.getPosition()
                            .distanceTo(other.getPosition());

            if (distance < SEPARATION_DISTANCE
                    && distance > 0) {

                Vector2D diff = new Vector2D(
                        animal.getPosition().getX()
                                - other.getPosition().getX(),

                        animal.getPosition().getY()
                                - other.getPosition().getY()
                );

                diff.normalize();

                steer.setX(steer.getX() + diff.getX());
                steer.setY(steer.getY() + diff.getY());
            }
        }

        return steer;
    }

    private Vector2D calculateAlignment(
            List<Animal> neighbors
    ) {

        Vector2D avgVelocity = new Vector2D(0, 0);

        for (Animal other : neighbors) {

            avgVelocity.setX(
                    avgVelocity.getX()
                            + other.getVelocity().getX()
            );

            avgVelocity.setY(
                    avgVelocity.getY()
                            + other.getVelocity().getY()
            );
        }

        avgVelocity.setX(
                avgVelocity.getX() / neighbors.size()
        );

        avgVelocity.setY(
                avgVelocity.getY() / neighbors.size()
        );

        avgVelocity.normalize();

        return avgVelocity;
    }

    private Vector2D calculateCohesion(
            Animal animal,
            List<Animal> neighbors
    ) {

        double centerX = 0;
        double centerY = 0;

        for (Animal other : neighbors) {

            centerX += other.getPosition().getX();
            centerY += other.getPosition().getY();
        }

        centerX /= neighbors.size();
        centerY /= neighbors.size();

        Vector2D direction = new Vector2D(
                centerX - animal.getPosition().getX(),
                centerY - animal.getPosition().getY()
        );

        direction.normalize();

        return direction;
    }
}
