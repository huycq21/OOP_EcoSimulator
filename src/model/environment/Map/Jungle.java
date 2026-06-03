package model.environment.Map;

import model.Vector2D;
import model.aquatic.Fish;
import model.aquatic.FishFour;
import model.aquatic.FishOne;
import model.aquatic.FishThree;
import model.aquatic.FishTwo;
import model.carnivore.Fox;
import model.carnivore.Wolf;
import model.domestic.Chicken;
import model.domestic.Cow;
import model.domestic.DomesticAnimal;
import model.domestic.Pig;
import model.herbivore.BlackGrouse;
import model.herbivore.Boar;
import model.herbivore.Deer;
import model.herbivore.Rabbit;
import model.plant.Grass;
import model.environment.Environment;
import model.environment.TmxCollisionLoader;
import model.environment.obstacle.Bush;

import java.util.Random;

public class Jungle extends Environment {
    private final Random random;
    
    // Khởi tạo Rừng nhiệt đới với kích thước truyền vào
    public Jungle(double width, double height) {
        super(width, height);
        this.random = new Random();
        TmxCollisionLoader.loadInto(this, "assets/Environment/Forest/Forest.tmx");

        spawnHerbivores();
        spawnCarnivores();
        spawnFish();
        spawnDomesticAnimals();
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

    private void spawnFish() {
        for (int i = 0; i < 8; i++) {
            addFish(new FishOne(randomWaterPosition(random, 4)));
            addFish(new FishTwo(randomWaterPosition(random, 4)));
            addFish(new FishThree(randomWaterPosition(random, 5)));
            addFish(new FishFour(randomWaterPosition(random, 5)));
        }
    }

    private void addFish(Fish fish) {
        if (fish.getPosition() != null) {
            addEntity(fish);
        }
    }

    private void spawnDomesticAnimals() {
        for (int i = 0; i < 4; i++) {
            addDomesticAnimal(new Chicken(randomPenPosition("coop", random, 4)));
        }

        for (int i = 0; i < 2; i++) {
            addDomesticAnimal(new Cow(randomPenPosition("cowshed", random, 7)));
        }

        for (int i = 0; i < 3; i++) {
            addDomesticAnimal(new Pig(randomPenPosition("pigsty", random, 6)));
        }
    }

    private void addDomesticAnimal(DomesticAnimal animal) {
        if (animal.getPosition() != null) {
            addEntity(animal);
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
        return randomOpenPosition(random, 8);
    }
}
