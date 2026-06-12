package model.spawner;

import model.Animal;
import model.Entity;
import model.Vector2D;
import model.carnivore.Carnivore;
import model.carnivore.Fox;
import model.carnivore.Wolf;
import model.environment.Environment;
import model.herbivore.BlackGrouse;
import model.herbivore.Boar;
import model.herbivore.Deer;
import model.herbivore.Herbivore;
import model.herbivore.Rabbit;
import model.plant.Grass;
import model.Reproducible;
import model.herbivore.Herbivore;
import model.carnivore.Carnivore;

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
        int herbivoreCount = 0;
        int carnivoreCount = 0;

        for (Entity e : env.getEntities()) {

            if (e instanceof Grass) {
                grassCount++;
            }

            if (e instanceof Herbivore) {
                herbivoreCount++;
            }

            if (e instanceof Carnivore) {
                carnivoreCount++;
            }
        }

        double multiplier =
        env.getWeather().getGrassGrowthMultiplier();

        int targetGrass =
                (int)(SimulationConstant.MIN_GRASS * multiplier);

        if (grassCount < targetGrass) {
            spawnGrass(targetGrass - grassCount);
        }

        if (herbivoreCount < SimulationConstant.MIN_HERBIVORE) {
            spawnRandomHerbivore(
                SimulationConstant.MIN_HERBIVORE - herbivoreCount
            );
        }

        if (carnivoreCount < SimulationConstant.MIN_CARNIVORE) {
            spawnRandomCarnivore(
                SimulationConstant.MIN_CARNIVORE - carnivoreCount
            );
        }

        if (reproductionTimer >= SimulationConstant.REPRODUCTION_INTERVAL) {

            reproductionTimer = 0;
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
                    if (distance > 400)
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
                        birthsThisCycle++;

                        EventManager.animalBorn(baby.getClass().getSimpleName());

                        // Kiểm tra điều kiện dừng chu kỳ quét để tránh bùng nổ dân số quá nhanh
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

            Vector2D pos =
                    env.randomOpenPosition(random, 4);

            env.queueEntity(new Grass(pos));
        }
        EventManager.plantSpawned("Grass");
    }

    private void spawnRandomHerbivore(int amount) {

        for (int i = 0; i < amount; i++) {

            Vector2D pos =
                env.randomOpenPosition(random, 8);

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

            Vector2D pos =
                env.randomOpenPosition(random, 8);

            if (random.nextBoolean()) {
                env.queueEntity(new Fox(pos));
            } else {
                env.queueEntity(new Wolf(pos));
            }
        }
    }
} 