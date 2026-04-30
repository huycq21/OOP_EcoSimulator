package model.Carnivore;

import model.Animal;
import model.Hunter;

// Động vật ăn thịt thì chắc chắn phải có kỹ năng Hunter
public abstract class Carnivore extends Animal implements Hunter {
    public Carnivore(double x, double y, int size, double speed) {
        super(x, y, size, speed);
    }
}