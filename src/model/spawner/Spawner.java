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
        
        // Đã sửa "rabit" thành "rabbit"
        spawnTestEntities("wolf", 5);  
        spawnTestEntities("rabbit", 10); 
        spawnTestEntities("deer", 15); 
        spawnTestEntities("grass", 30);
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

    public void spawnTestEntities(String entityType, int amount) {
        for (int i = 0; i < amount; i++) {
            // Lấy tọa độ ngẫu nhiên KHÔNG dính vật cản
            Vector2D pos = env.randomOpenPosition(random, 8); 

            switch (entityType.toLowerCase()) {
                case "wolf":
                    env.queueEntity(new model.carnivore.Wolf(pos));
                    EventManager.animalBorn("Wolf"); // Đã thêm EventManager
                    break;
                case "fox":
                    env.queueEntity(new model.carnivore.Fox(pos));
                    EventManager.animalBorn("Fox");
                    break;
                case "boar":
                    env.queueEntity(new model.herbivore.Boar(pos));
                    EventManager.animalBorn("Boar");
                    break;
                case "deer":
                    env.queueEntity(new model.herbivore.Deer(pos));
                    EventManager.animalBorn("Deer");
                    break;
                case "rabbit":
                    env.queueEntity(new Rabbit(pos));
                    EventManager.animalBorn("Rabbit");
                    break;
                case "grass": // ĐÃ THÊM CASE CHO CỎ
                    env.queueEntity(new Grass(pos));
                    EventManager.plantSpawned("Grass");
                    break;
                default:
                    System.out.println("DEV CẢNH BÁO: Chưa cấu hình case test cho: " + entityType);
            }
        }
        System.out.println("DEV TEST: Đã spawn " + amount + " " + entityType);
    } 
}