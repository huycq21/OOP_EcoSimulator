package model.spawner;

import model.Animal;
import model.Entity;
import model.Vector2D;
import model.Reproducible;
import model.carnivore.Carnivore;
import model.carnivore.Fox;
import model.carnivore.Wolf;
import model.carnivore.Cheetah;
import model.herbivore.Herbivore;
import model.herbivore.Rabbit;
import model.herbivore.Deer;
import model.herbivore.Boar;
import model.herbivore.BlackGrouse;
import model.herbivore.Goat;
import model.herbivore.Horse;
import model.apex.Lion;
import model.apex.Bear;
import model.apex.Human;
import model.plant.Grass;
import model.environment.Environment;

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
        
        // Giữ nguyên logic khởi tạo thế giới với danh sách thực thể test phong phú từ bản mới
        spawnTestEntities("wolf", 10);  
        spawnTestEntities("rabbit", 15); 
        spawnTestEntities("deer", 10); 
        spawnTestEntities("boar", 5);
        spawnTestEntities("fox", 5);
        spawnTestEntities("blackgrouse", 5);
        spawnTestEntities("goat", 6);
        spawnTestEntities("horse", 4);
        spawnTestEntities("cheetah", 3);
        spawnTestEntities("lion", 2);
        spawnTestEntities("bear", 1);
        spawnTestEntities("human", 1);
        spawnTestEntities("grass", 20);
    }

    public void update() {
        reproductionTimer++;
        
        int grassCount = 0;
        int herbivoreCount = 0;
        int carnivoreCount = 0;
        int rabbitCount = 0; // Theo dõi riêng loài Rabbit theo tiêu chuẩn bản mới

        // Quét hệ sinh thái một lần duy nhất để phân loại số lượng (Tối ưu CPU thay vì quét nhiều lần)
        for (Entity e : env.getEntities()) {
            if (e instanceof Grass) {
                grassCount++;
            }
            if (e instanceof Herbivore) {
                herbivoreCount++;
                if (e instanceof Rabbit) {
                    rabbitCount++;
                }
            }
            if (e instanceof Carnivore) {
                carnivoreCount++;
            }
        }

        // --- 1. KIỂM SOÁT THỰC VẬT (CỎ) ---
        double multiplier = env.getWeather().getGrassGrowthMultiplier();
        int targetGrass = (int) (SimulationConstant.MIN_GRASS * multiplier);

        if (grassCount < targetGrass) {
            spawnGrass(targetGrass - grassCount);
        }

        // --- 2. KIỂM SOÁT ĐỘNG VẬT NỀN (Bảo hiểm chống tuyệt chủng đột ngột) ---
        // Giữ ngưỡng tối thiểu cho riêng Thỏ (Bản mới)
        if (rabbitCount < SimulationConstant.MIN_RABBIT) {
            spawnRabbit(SimulationConstant.MIN_RABBIT - rabbitCount);
        }
        // Giữ ngưỡng tối thiểu cho tổng nhóm Ăn cỏ (Bản cũ)
        if (herbivoreCount < SimulationConstant.MIN_HERBIVORE) {
            spawnRandomHerbivore(SimulationConstant.MIN_HERBIVORE - herbivoreCount);
        }
        // Giữ ngưỡng tối thiểu cho tổng nhóm Ăn thịt (Bản cũ)
        if (carnivoreCount < SimulationConstant.MIN_CARNIVORE) {
            spawnRandomCarnivore(SimulationConstant.MIN_CARNIVORE - carnivoreCount);
        }

        // --- 3. LOGIC SINH SẢN CHU KỲ (REPRODUCTION CYCLE) ---
        if (reproductionTimer >= SimulationConstant.REPRODUCTION_INTERVAL) {
            reproductionTimer = 0;
            int birthsThisCycle = 0;

            List<Entity> entities = env.getEntities();
            boolean brokeOut = false; 

            // CƠ CHẾ 1: Sinh sản ghép đôi hình học (Từ Bản Cũ - Áp dụng cho các lớp kế thừa Animal)
            for (Entity e1 : entities) {
                if (brokeOut) break;
                if (!(e1 instanceof Animal a1)) continue;

                for (Entity e2 : entities) {
                    if (!(e2 instanceof Animal a2) || a1 == e2) continue;

                    // Kiểm tra điều kiện ghép đôi: Cùng loài + Khác giới tính + Đủ tuổi/năng lượng
                    if (a1.getClass() != a2.getClass()) continue;
                    if (a1.isFemale() == a2.isFemale()) continue;
                    if (!a1.canMate() || !a2.canMate()) continue;

                    // Kiểm tra khoảng cách hình học gần nhau để giao phối
                    double distance = a1.getPosition().distanceTo(a2.getPosition());
                    if (distance > 400) continue;

                    Animal female = a1.isFemale() ? a1 : a2;
                    Animal male = a1.isMale() ? a1 : a2;

                    if (female instanceof Reproducible reproducible) {
                        // Sinh sản đa hình có truyền partner đực
                        Entity baby = reproducible.reproduce(male); 
                        if (baby != null) {
                            env.queueEntity(baby);
                            birthsThisCycle++;
                            EventManager.animalBorn(baby.getClass().getSimpleName());
                            System.out.println("BIRTH (PAIRED): " + baby.getClass().getSimpleName());
                        }

                        // Giới hạn số lượng sinh sản tối đa mỗi chu kỳ để tránh tràn bộ nhớ/nổ dân số
                        if (birthsThisCycle >= 10) {
                            brokeOut = true;
                            break;
                        }
                    }
                }
            }
        }
    }

    private void spawnGrass(int amount) {
        for (int i = 0; i < amount; i++) {
            Vector2D pos = env.randomOpenPosition(random, 4);
            env.queueEntity(new Grass(pos));
        }
        EventManager.plantSpawned("Grass");
    }

    private void spawnRabbit(int amount) {
        for (int i = 0; i < amount; i++) {
            Vector2D pos = env.randomOpenPosition(random, 8);
            env.queueEntity(new Rabbit(pos));
        }
        EventManager.animalBorn("Rabbit");
    }

    private void spawnRandomHerbivore(int amount) {
        for (int i = 0; i < amount; i++) {
            Vector2D pos = env.randomOpenPosition(random, 8);
            int type = random.nextInt(4);
            switch (type) {
                case 0 -> env.queueEntity(new Rabbit(pos));
                case 1 -> env.queueEntity(new Deer(pos));
                case 2 -> env.queueEntity(new Boar(pos));
                case 3 -> env.queueEntity(new BlackGrouse(pos));
            }
        }
    }

    private void spawnRandomCarnivore(int amount) {
        for (int i = 0; i < amount; i++) {
            Vector2D pos = env.randomOpenPosition(random, 8);
            if (random.nextBoolean()) {
                env.queueEntity(new Fox(pos));
            } else {
                env.queueEntity(new Wolf(pos));
            }
        }
    }

    // --- LOGIC HỖ TRỢ ĐỂ CHẠY TEST (TỪ BẢN MỚI) ---
    public void spawnTestEntities(String entityType, int amount) {
        for (int i = 0; i < amount; i++) {
            Vector2D pos = env.randomOpenPosition(random, spawnRadiusFor(entityType)); 

            switch (entityType.toLowerCase()) {
                case "wolf" -> {
                    env.queueEntity(new Wolf(pos));
                    EventManager.animalBorn("Wolf");
                }
                case "fox" -> {
                    env.queueEntity(new Fox(pos));
                    EventManager.animalBorn("Fox");
                }
                case "boar" -> {
                    env.queueEntity(new Boar(pos));
                    EventManager.animalBorn("Boar");
                }
                case "deer" -> {
                    env.queueEntity(new Deer(pos));
                    EventManager.animalBorn("Deer");
                }
                case "blackgrouse" -> {
                    env.queueEntity(new BlackGrouse(pos));
                    EventManager.animalBorn("BlackGrouse");
                }
                case "rabbit" -> {
                    env.queueEntity(new Rabbit(pos));
                    EventManager.animalBorn("Rabbit");
                }
                case "goat" -> {
                    env.queueEntity(new model.herbivore.Goat(pos));
                    EventManager.animalBorn("Goat");
                }
                case "horse" -> {
                    env.queueEntity(new model.herbivore.Horse(pos));
                    EventManager.animalBorn("Horse");
                }
                case "cheetah" -> {
                    env.queueEntity(new model.carnivore.Cheetah(pos));
                    EventManager.animalBorn("Cheetah");
                }
                case "lion" -> {
                    env.queueEntity(new model.apex.Lion(pos));
                    EventManager.animalBorn("Lion");
                }
                case "bear" -> {
                    env.queueEntity(new model.apex.Bear(pos));
                    EventManager.animalBorn("Bear");
                }
                case "human", "farmer" -> {
                    env.queueEntity(new model.apex.Human(pos));
                    EventManager.animalBorn("Human");
                }
                case "grass" -> {
                    env.queueEntity(new Grass(pos));
                    EventManager.plantSpawned("Grass");
                }
                default -> System.out.println("DEV CẢNH BÁO: Chưa cấu hình ô test sinh sản cho loại: " + entityType);
            }
        }
        System.out.println("DEV TEST: Đã spawn thành công " + amount + " " + entityType);
    } 

    private double spawnRadiusFor(String entityType) {
        return switch (entityType.toLowerCase()) {
            case "goat" -> 4.5;
            case "horse" -> 5.8;
            case "cheetah" -> 5.2;
            case "lion" -> 8.0;
            case "bear" -> 9.0;
            case "human", "farmer" -> 3.8;
            default -> 8.0;
        };
    }
}