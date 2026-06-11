package model.spawner;

import model.Animal;
import model.Entity;
import model.Vector2D;
import model.environment.Environment;
import model.herbivore.Rabbit;
import model.plant.Grass;
import model.Reproducible;

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

            model.environment.Season season = env.getWeather().getCurrentSeason();
            if (season != model.environment.Season.SPRING) {
                return; 
            }

            int birthsThisCycle = 0;

            List<Entity> entities = env.getEntities();
            boolean brokeOut = false; // Biến cờ để thoát vòng lặp ngoài cùng khi đạt giới hạn

            for (Entity e1 : entities) {
                if (brokeOut) break;

                if (!(e1 instanceof Animal))
                    continue;

                for (Entity e2 : entities) {

                    if (!(e2 instanceof Animal))
                        continue;

                    if (e1 == e2)
                        continue;

                    Animal a1 = (Animal) e1;
                    Animal a2 = (Animal) e2;

                    // Kiểm tra cùng loài
                    if (a1.getClass() != a2.getClass())
                        continue;

                    // Kiểm tra giới tính (Phải khác giới tính) 
                    if (a1.isFemale() == a2.isFemale()) { // Hoặc dùng a1.getGender() == a2.getGender()
                        continue;
                    }

                    // Kiểm tra tuổi và năng lượng 
                    if (!a1.canMate())
                        continue;

                    if (!a2.canMate())
                        continue;

                    // Kiểm tra khoảng cách hình học 
                    double distance = a1.getPosition().distanceTo(a2.getPosition());
                    if (distance > 500)
                        continue;

                    // Xác định ai là con cái, ai là con đực
                    Animal female = a1.isFemale() ? a1 : a2;
                    Animal male = a1.isMale() ? a1 : a2;

                    // Sinh con bằng kỹ thuật đa hình
                    System.out.println(
                        "PAIR FOUND: "
                        + female.getClass().getSimpleName()
                    );
                    if (female instanceof Reproducible reproducible) {

                        // Gọi hàm sinh sản, truyền con đực làm partner
                        Entity baby = reproducible.reproduce(male);
                        System.out.println(
                            "BIRTH: "
                            + baby.getClass().getSimpleName()
                        );

                        // Đưa con non vào hàng đợi thế giới
                        env.queueEntity(baby);

                        // Tránh đẻ vô hạn: Đánh dấu chặn đẻ ngay lập tức trong mùa xuân này
                        female.markReproduced();
                        male.markReproduced();

                        birthsThisCycle++;
                        
                        // Cập nhật số lượng thỏ cục bộ nếu con non sinh ra là thỏ
                        if (baby instanceof Rabbit) {
                            rabbitCount++;
                        }

                        EventManager.animalBorn(baby.getClass().getSimpleName());

                        // Kiểm tra điều kiện dừng chu kỳ quét để tránh bùng nổ dân số quá nhanh
                        if (birthsThisCycle >= 10 || rabbitCount >= SimulationConstant.MAX_RABBIT) {
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