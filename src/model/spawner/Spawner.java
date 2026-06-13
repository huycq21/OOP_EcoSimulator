package model.spawner;

import model.Animal;
import model.Entity;
import model.Vector2D;
import model.environment.Environment;
import model.herbivore.Rabbit;
import model.plant.*;
import model.Reproducible;
import model.apex.*;

import java.util.List;
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
        
        // spawn ăn thịt
        spawnEntities("wolf", 10);  
        spawnEntities("fox", 10);
        // spawnEntities("hyena",1);
        // spawnEntities("cheetah",1);
        // spawn ăn cỏ
        spawnEntities("rabbit", 5); 
        // spawnEntities("deer", 2); 
        spawnEntities("goat", 5);
        spawnEntities("horse", 10);
        // spawnEntities("elephant", 1);
        // spawnEntities("boar", 2);
        // spawn apex
        spawnEntities("lion", 2);
        // spawnEntities("bear", 1);
        spawnEntities("human", 2);
        // spawnEntities("eagle", 1);
        // spawnEntities("tiger", 1);
        // spawn thực vật
        spawnEntities("grass", 1);
        // spawnEntities("vinetree", 30);
        // spawnEntities("berry",1);
        spawnEntities("treeplant", 1);
        spawnEntities("mushroom", 1);

        spawnEntities("fishone", 5);
        spawnEntities("FishTwo", 5);
        spawnEntities("fishthree", 5);
        spawnEntities("fishfour", 5);
        
    }

    public void update() {
        reproductionTimer++;

        // ==========================================
        // 1. TỰ ĐỘNG ĐẾM SỐ LƯỢNG MỌI LOÀI ĐANG CÓ (Tối ưu bằng Map)
        // ==========================================
        java.util.Map<String, Integer> population = new java.util.HashMap<>();
        
        for (Entity e : env.getEntities()) {
            if (!e.isAlive()) continue;
            String species = e.getClass().getSimpleName();
            population.put(species, population.getOrDefault(species, 0) + 1);
        }

        // ==========================================
        // 2. BÙ ĐẮP NHỮNG LOÀI BỊ THIẾU (DƯỚI MỨC MIN)
        // ==========================================
        double plantMultiplier = env.getWeather().getPlantGrowthMultiplier();
        String[] corePlants = {"Grass", "Berry", "Algae", "Mushroom", "VinePlant"}; 
        for (String plant : corePlants) {
            int currentCount = population.getOrDefault(plant, 0);
            int minRequired = (int) (SimulationConstant.getMinPopulation(plant) * plantMultiplier);
            
            if (currentCount < minRequired) {
                spawnEntities(plant, minRequired - currentCount);
                population.put(plant, minRequired); 
            }
        }

        double seasonMultiplier = env.getWeather().getCurrentSeason().getMultiReproduction();
        String[] coreAnimals = {"Rabbit", "Wolf", "Deer"}; 
        for (String animal : coreAnimals) {
            int currentCount = population.getOrDefault(animal, 0);
            int minRequired = (int)(SimulationConstant.getMinPopulation(animal) * seasonMultiplier);
            
            if (currentCount < minRequired) {
                spawnEntities(animal, minRequired - currentCount);
                population.put(animal, minRequired); 
            }
        }

        // ==========================================
        // 3. LOGIC SINH SẢN GHÉP ĐÔI (Kết hợp kiểm tra Max Pop)
        // ==========================================
        if (reproductionTimer >= SimulationConstant.REPRODUCTION_INTERVAL) {
            reproductionTimer = 0;
            int birthsThisCycle = 0;
            List<Entity> entities = env.getEntities();
            boolean brokeOut = false; 

            for (Entity e1 : entities) {
                if (brokeOut) break;
                if (!(e1 instanceof Animal a1) || !a1.isAlive()) continue;

                String species = a1.getClass().getSimpleName();
                int currentPop = population.getOrDefault(species, 0);
                int maxPop = (int) (SimulationConstant.getMaxPopulation(species) * seasonMultiplier);

                // Dừng xét con vật này nếu loài của nó đã đạt giới hạn dân số
                if (currentPop >= maxPop) continue; 

                for (Entity e2 : entities) {
                    if (!(e2 instanceof Animal a2) || a1 == e2 || !a2.isAlive()) continue;

                    // Kiểm tra điều kiện ghép đôi
                    if (a1.getClass() != a2.getClass()) continue;
                    if (a1.isFemale() == a2.isFemale()) continue;
                    if (!a1.canMate() || !a2.canMate()) continue;

                    // Kiểm tra khoảng cách hình học gần nhau
                    double distance = a1.getPosition().distanceTo(a2.getPosition());
                    if (distance > 400) continue;

                    Animal female = a1.isFemale() ? a1 : a2;
                    Animal male = a1.isMale() ? a1 : a2;

                    if (female instanceof Reproducible reproducible) {
                        Entity baby = reproducible.reproduce(male); 
                        if (baby != null) {
                            env.queueEntity(baby);
                            
                            // Cập nhật dân số ngay lập tức để tránh đẻ lố
                            population.put(species, currentPop + 1);
                            birthsThisCycle++;
                            EventManager.animalBorn(species);
                            
                            // Giới hạn số lượng sinh sản tối đa mỗi chu kỳ (tránh lag)
                            if (birthsThisCycle >= 10) {
                                brokeOut = true;
                                break;
                            }
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
                case "famer":
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
                case "berry":
                    env.queueEntity(new model.plant.Berry(pos));
                    EventManager.plantSpawned("Berry");
                    break;
                case "smalltree":
                    env.queueEntity(new model.plant.SmallTree(pos));
                    EventManager.plantSpawned("SmallTree");
                    break;
                case "treeplant":
                    env.queueEntity(new model.plant.TreePlant(pos));
                    EventManager.plantSpawned("TreePlant");
                    break;
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

    // private double spawnRadiusFor(String entityType) {
    //     switch (entityType.toLowerCase()) {
    //         case "goat":
    //             return 4.5;
    //         case "horse":
    //             return 5.8;
    //         case "cheetah":
    //             return 5.2;
    //         case "lion":
    //             return 8.0;
    //         case "bear":
    //             return 9.0;
    //         case "human":
    //         case "farmer":
    //             return 3.8;
    //         default:
    //             return 8.0;
    //     }
    // }
}
