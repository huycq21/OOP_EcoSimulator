package model.carnivore;

import model.Vector2D;
import model.strategy.FlockingStrategy;
import model.strategy.HunterStrategy;

public class Hyena extends Carnivore {

    public Hyena(Vector2D position) {
        // Tầm nhìn xa 150.0 để đánh hơi xác chết
        super(position, 4.5, 90, 160, 4.5, 150.0, 50.0, 35.0, 45);
        this.setBrain(new HunterStrategy(new FlockingStrategy()));
    }
}