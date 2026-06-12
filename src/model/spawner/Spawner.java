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
        spawnTestEntities("wolf", 10);  
        spawnTestEntities("rabbit", 10); 
        spawnTestEntities("deer", 10); 
        spawnTestEntities("goat", 6);
        spawnTestEntities("horse", 4);
        spawnTestEntities("cheetah", 3);
        spawnTestEntities("lion", 2);
        spawnTestEntities("bear", 1);
        spawnTestEntities("human", 1);
        spawnTestEntities("grass", 10);
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
            Vector2D pos = env.randomOpenPosition(random, spawnRadiusFor(entityType)); 

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
                case "goat":
                    env.queueEntity(new model.herbivore.Goat(pos));
                    EventManager.animalBorn("Goat");
                    break;
                case "horse":
                    env.queueEntity(new model.herbivore.Horse(pos));
                    EventManager.animalBorn("Horse");
                    break;
                case "cheetah":
                    env.queueEntity(new model.carnivore.Cheetah(pos));
                    EventManager.animalBorn("Cheetah");
                    break;
                case "lion":
                    env.queueEntity(new model.apex.Lion(pos));
                    EventManager.animalBorn("Lion");
                    break;
                case "bear":
                    env.queueEntity(new model.apex.Bear(pos));
                    EventManager.animalBorn("Bear");
                    break;
                case "human":
                case "farmer":
                    env.queueEntity(new model.apex.Human(pos));
                    EventManager.animalBorn("Human");
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

    private double spawnRadiusFor(String entityType) {
        switch (entityType.toLowerCase()) {
            case "goat":
                return 4.5;
            case "horse":
                return 5.8;
            case "cheetah":
                return 5.2;
            case "lion":
                return 8.0;
            case "bear":
                return 9.0;
            case "human":
            case "farmer":
                return 3.8;
            default:
                return 8.0;
        }
    }
}
