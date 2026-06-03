package model.spawner;

import model.Entity;
import model.Vector2D;
import model.environment.Environment;
import model.herbivore.Rabbit;
import model.plant.Grass;
import model.Reproducible;

import java.util.Random;

import controller.EventManager;
import controller.SimulationConstant;

public class Spawner {

    private final Environment env;
    private final Random random;

    private int reproductionTimer = 0;

    public Spawner(Environment env) {
        this.env = env;
        this.random = new Random();
    }

    public void update() {
        reproductionTimer++;
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

        double multiplier =
        env.getWeather().getGrassGrowthMultiplier();

        int targetGrass =
                (int)(SimulationConstant.MIN_GRASS * multiplier);

        if (grassCount < targetGrass) {
            spawnGrass(targetGrass - grassCount);
        }

        if (rabbitCount < SimulationConstant.MIN_RABBIT) {
            spawnRabbit(SimulationConstant.MIN_RABBIT - rabbitCount);
        }

        if (reproductionTimer >= SimulationConstant.REPRODUCTION_INTERVAL) {

            reproductionTimer = 0;

            int birthsThisCycle = 0;

            if (rabbitCount < SimulationConstant.MAX_RABBIT) {

                for (Entity e : env.getEntities()) {

                    if (e instanceof Reproducible reproducible
                            && reproducible.canReproduce()) {

                        Entity baby = reproducible.reproduce();

                        env.queueEntity(baby);

                        birthsThisCycle++;
                        rabbitCount++;

                        EventManager.animalBorn(
                                baby.getClass().getSimpleName()
                        );

                        if (birthsThisCycle >= 2) {
                            break;
                        }

                        if (rabbitCount >= SimulationConstant.MAX_RABBIT) {
                            break;
                        }
                    }
                }
            }
        }
    }

    private void spawnGrass(int amount) {

        for (int i = 0; i < amount; i++) {

            Vector2D pos =
                    env.randomOpenPosition(random, 4);

            env.queueEntity(new Grass(pos));
        }
        EventManager.plantSpawned("Grass");
    }

    private void spawnRabbit(int amount) {

        for (int i = 0; i < amount; i++) {

            Vector2D pos =
                    env.randomOpenPosition(random, 8);

            env.queueEntity(new Rabbit(pos));
        }
        EventManager.animalBorn("Rabbit");
    }
} 