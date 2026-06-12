package model.spawner;

import model.Entity;
import model.Vector2D;
import model.environment.Environment;
import model.herbivore.Rabbit;
import model.plant.*;
import model.Reproducible;
import model.apex.*;

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
        
        // spawnEntities("human",5);
        // spawnEntities("eagle",5);
        // spawnEntities("Lion", 5);
        // spawnEntities("bear", 5);
        // spawnEntities("tiger",5);
        // spawnEntities("rabbit", 20);
        // spawnEntities("deer", 10);
        // spawnEntities("grass", 10);
        // spawnEntities("wolf", 10);
        spawnEntities("fishone", 30);
        spawnEntities("fishtwo", 10);
        spawnEntities("fishthree", 10);

        // spawnEntities("fox", 10);

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
            spawnEntities("grass",targetGrass - grassCount);
        }

        if (rabbitCount < SimulationConstant.MIN_RABBIT) {
            spawnEntities("rabbit",SimulationConstant.MIN_RABBIT - rabbitCount);
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

    public void spawnEntities(String entityType, int amount) {
        for (int i = 0; i < amount; i++) {
            Vector2D pos = null;
            String type = entityType.toLowerCase();

            // 1. Phân loại địa hình spawn bằng kiến trúc linh hoạt mới
            if (type.contains("fish")) {
                // Nhóm cá bắt buộc phải spawn dưới nước
                pos = env.randomTerrainPosition(model.environment.TerrainType.WATER, random, 8);
            } 
            else if (type.equals("cow") || type.equals("pig") || type.equals("chicken")) {
                // Nhóm gia súc ưu tiên spawn trong khu vực chuồng trại
                pos = env.randomTerrainPosition(model.environment.TerrainType.PEN, random, 8);
                // Nếu map không có chuồng, rớt xuống spawn trên đất trống
                if (pos == null) pos = env.randomOpenPosition(random, 8);
            } 
            else {
                // Thú rừng và thực vật spawn trên đất trống
                pos = env.randomOpenPosition(random, 8);
            }

            // Bỏ qua vòng lặp nếu bản đồ đã quá chật, không tìm được chỗ đứng
            if (pos == null) continue;

            switch (type) {
                // Thú ăn thịt
                case "wolf":
                    env.queueEntity(new model.carnivore.Wolf(pos));
                    EventManager.animalBorn("Wolf"); // Đã thêm EventManager
                    break;
                case "fox":
                    env.queueEntity(new model.carnivore.Fox(pos));
                    EventManager.animalBorn("Fox");
                    break;
                case "hyena":
                    env.queueEntity(new model.carnivore.Hyena(pos));
                    EventManager.animalBorn("Hyena");
                    break;
                case "cheetah":
                    env.queueEntity(new model.carnivore.Cheetah(pos));
                    EventManager.animalBorn("Cheetah");
                    break;
                // Thú ăn thịt đỉnh cao
                case "lion":
                    env.queueEntity(new model.apex.Lion(pos));
                    EventManager.animalBorn("Lion");
                    break;
                case "tiger":
                    env.queueEntity(new model.apex.Tiger(pos));
                    EventManager.animalBorn("Tiger");
                    break;
                case "eagle":
                    env.queueEntity(new model.apex.Eagle(pos));
                    EventManager.animalBorn("Eagle");
                    break;
                case "human":
                    env.queueEntity(new model.apex.Human(pos));
                    EventManager.animalBorn("Human");
                    break;
                case "bear":
                    env.queueEntity(new model.apex.Bear(pos));
                    EventManager.animalBorn("Bear");
                    break;
                // Thú ăn cỏ
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
                case "elephant":
                    env.queueEntity(new model.herbivore.Elephant(pos));
                    EventManager.animalBorn("Elephant");
                    break;
                case "goat":
                    env.queueEntity(new model.herbivore.Goat(pos));
                    EventManager.animalBorn("Goat");
                    break;
                case "horse":
                    env.queueEntity(new model.herbivore.Horse(pos));
                    EventManager.animalBorn("Horse");
                    break;
                // Thực vật
                case "grass": // ĐÃ THÊM CASE CHO CỎ
                    env.queueEntity(new Grass(pos));
                    EventManager.plantSpawned("Grass");
                    break;
                case "algae":
                    env.queueEntity(new model.plant.Algae(pos));
                    EventManager.plantSpawned("Algae");
                    break;
                case "berry":
                    env.queueEntity(new model.plant.Berry(pos));
                    EventManager.plantSpawned("Berry");
                    break;
                case "smalltree":
                    env.queueEntity(new model.plant.SmallTree(pos));
                    EventManager.plantSpawned("SmallTree");
                    break;
                // case "treeplant":
                //     env.queueEntity(new model.plant.TreePlant(pos));
                //     EventManager.plantSpawned("TreePlant");
                //     break;
                case "vineplant":
                    env.queueEntity(new model.plant.VinePlant(pos));
                    EventManager.plantSpawned("VinePlant");
                    break;
                case "mushroom":
                    env.queueEntity(new model.plant.Mushroom(pos));
                    EventManager.plantSpawned("Mushroom");
                    break;
                // Động vật dưới nước
                case "fishone":
                    env.queueEntity(new model.aquatic.FishOne(pos));
                    EventManager.animalBorn("FishOne");
                    break;
                case "fishtwo":
                    env.queueEntity(new model.aquatic.FishTwo(pos));
                    EventManager.animalBorn("FishTwo");
                    break;
                case "fishthree":
                    env.queueEntity(new model.aquatic.FishThree(pos));
                    EventManager.animalBorn("FishThree");
                    break;
                case "fishfour":
                    env.queueEntity(new model.aquatic.FishFour(pos));
                    EventManager.animalBorn("FishFour");
                    break;
                
                default:
                    System.out.println("DEV CẢNH BÁO: Chưa cấu hình casen cho: " + entityType);
            }
        }
        System.out.println("DEVn: Đã spawn " + amount + " " + entityType);
    } 
}