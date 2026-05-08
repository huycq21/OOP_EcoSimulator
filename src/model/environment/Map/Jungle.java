package model.environment.Map;

import model.Vector2D;
import model.carnivore.Fox;
import model.carnivore.Wolf;
import model.herbivore.BlackGrouse;
import model.herbivore.Boar;
import model.herbivore.Deer;
import model.herbivore.Rabbit;
import model.plant.Grass;
import model.environment.Bush;
import model.environment.Environment;

import java.util.Random;

public class Jungle extends Environment {
    private final Random random;
    
    // Khởi tạo Rừng nhiệt đới với kích thước truyền vào
    public Jungle(double width, double height) {
        super(width, height);
        this.random = new Random();

        spawnHerbivores();
        spawnCarnivores();
        spawnPlantsAndCover();
    }

    private void spawnHerbivores() {
        for (int i = 0; i < 18; i++) {
            addEntity(new Rabbit(randomPosition()));
        }

        for (int i = 0; i < 14; i++) {
            addEntity(new BlackGrouse(randomPosition()));
        }

        for (int i = 0; i < 8; i++) {
            addEntity(new Deer(randomPosition()));
        }

        for (int i = 0; i < 6; i++) {
            addEntity(new Boar(randomPosition()));
        }
    }

    private void spawnCarnivores() {
        for (int i = 0; i < 5; i++) {
            addEntity(new Wolf(randomPosition()));
        }

        for (int i = 0; i < 4; i++) {
            addEntity(new Fox(randomPosition()));
        }
    }

    private void spawnPlantsAndCover() {
        for (int i = 0; i < 90; i++) {
            addEntity(new Grass(randomPosition()));
        }

        for (int i = 0; i < 22; i++) {
            addEntity(new Bush(randomPosition(), 28));
        }
    }

    private Vector2D randomPosition() {
        double margin = 60;
        double x = margin + random.nextDouble() * (width - margin * 2);
        double y = margin + random.nextDouble() * (height - margin * 2);
        return new Vector2D(x, y);
    }
}
