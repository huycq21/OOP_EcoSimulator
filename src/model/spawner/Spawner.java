package model.spawner;

import model.Entity;
import model.Vector2D;
import model.environment.Environment;
import model.herbivore.Rabbit;
import model.plant.Grass;

import java.util.Random;

public class Spawner {

    private final Environment env;
    private final Random random;

    private static final int MIN_GRASS = 90;
    private static final int MIN_RABBIT = 18;

    public Spawner(Environment env) {
        this.env = env;
        this.random = new Random();
    }

    public void update() {

        int grassCount = 0;
        int rabbitCount = 0;

        for (Entity e : env.getEntities()) {

            if (e instanceof Grass) {
                grassCount++;
            }

            if (e instanceof Rabbit) {
                rabbitCount++;
            }
        }

        if (grassCount < MIN_GRASS) {
            spawnGrass(MIN_GRASS - grassCount);
        }

        if (rabbitCount < MIN_RABBIT) {
            spawnRabbit(MIN_RABBIT - rabbitCount);
        }
    }

    private void spawnGrass(int amount) {

        for (int i = 0; i < amount; i++) {

            Vector2D pos =
                    env.randomOpenPosition(random, 4);

            env.queueEntity(new Grass(pos));
        }
    }

    private void spawnRabbit(int amount) {

        for (int i = 0; i < amount; i++) {

            Vector2D pos =
                    env.randomOpenPosition(random, 8);

            env.queueEntity(new Rabbit(pos));
        }
    }
}