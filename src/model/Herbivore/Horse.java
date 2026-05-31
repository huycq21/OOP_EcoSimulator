package model.herbivore;

import model.Vector2D;
import model.strategy.FlockingStrategy;
import model.strategy.PassiveStrategy;
import model.strategy.ScaredStrategy;
import model.strategy.ForagingStrategy;

public class Horse extends Herbivore {

    public Horse(Vector2D position) {
        // Kích thước: 5.0, Máu: 80, Năng lượng: 150
        // Tốc độ: 5.5, Tầm nhìn: 60.0
        super(position, 5.0, 80, 150, 5.5, 60.0);
        // Trong constructor của Horse:
        this.setBrain(new ForagingStrategy(new FlockingStrategy(new PassiveStrategy())));
    }
}
