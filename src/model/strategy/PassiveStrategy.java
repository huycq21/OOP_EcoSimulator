package model.strategy;

import model.Animal;
import model.AnimalState;
import model.Vector2D;
import model.environment.Environment;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import controller.SimulationConstant;

public class PassiveStrategy implements SurvivalStrategy {
    private final Random random;
    private final Map<Integer, Vector2D> wanderDirections;
    private final Map<Integer, Integer> directionTimers;
    private final Map<Integer, Integer> restTimers;

    public PassiveStrategy() {
        this.random = new Random();
        this.wanderDirections = new HashMap<>();
        this.directionTimers = new HashMap<>();
        this.restTimers = new HashMap<>();
    }

    @Override
    public void execute(Animal animal) {
        animal.setCurrentState(AnimalState.WANDERING);

        int id = animal.getId();
        int restLeft = restTimers.getOrDefault(id, 0);
        if (restLeft > 0) {
            restTimers.put(id, restLeft - 1);
            animal.getVelocity().setX(0);
            animal.getVelocity().setY(0);
            return;
        }

        int timer = directionTimers.getOrDefault(id, 0);
        if (timer <= 0 || !wanderDirections.containsKey(id)) {
            chooseNewWanderState(animal);
            if (restTimers.getOrDefault(id, 0) > 0) {
                animal.getVelocity().setX(0);
                animal.getVelocity().setY(0);
                return;
            }
        }

        Vector2D direction = wanderDirections.get(id);
        double pace = animal.getSpeed() * 0.25;
        animal.getVelocity().setX(direction.getX() * pace);
        animal.getVelocity().setY(direction.getY() * pace);
        steerAwayFromMapEdges(animal);
        directionTimers.put(id, directionTimers.get(id) - 1);
    }

    private void chooseNewWanderState(Animal animal) {
        int id = animal.getId();

        if (random.nextDouble() < 0.25) {
            restTimers.put(id, 30 + random.nextInt(90));
            directionTimers.put(id, 0);
            return;
        }

        double angle = random.nextDouble() * 2 * Math.PI;
        Vector2D direction = new Vector2D(Math.cos(angle), Math.sin(angle));
        wanderDirections.put(id, direction);
        directionTimers.put(id, 45 + random.nextInt(150));
    }

    private void steerAwayFromMapEdges(Animal animal) {
        Environment env = Environment.getInstance();
        if (env == null) return;

        double margin = SimulationConstant.EDGE_MARGIN;
        double x = animal.getPosition().getX();
        double y = animal.getPosition().getY();
        Vector2D velocity = animal.getVelocity();

        if (x < margin) {
            velocity.setX(Math.abs(velocity.getX()));
        } else if (x > env.getWidth() - margin) {
            velocity.setX(-Math.abs(velocity.getX()));
        }

        if (y < margin) {
            velocity.setY(Math.abs(velocity.getY()));
        } else if (y > env.getHeight() - margin) {
            velocity.setY(-Math.abs(velocity.getY()));
        }
    }
}
