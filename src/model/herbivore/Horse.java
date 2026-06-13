package model.herbivore;

import model.Vector2D;
import model.strategy.FlockingStrategy;
import model.strategy.PassiveStrategy;
import model.strategy.ScaredStrategy;
import model.strategy.SurvivalStrategy;
import model.strategy.ForagingStrategy;

public class Horse extends Herbivore {

    public Horse(Vector2D position) {
        // Kích thước: 5.0, Máu: 80, Năng lượng: 150
        // Tốc độ: 5.5, Tầm nhìn: 60.0
        super(position, 4.5, 80, 150, 5.5, 120.0);
        // Trong constructor của Horse:
        SurvivalStrategy passive = new PassiveStrategy();
        SurvivalStrategy flocking = new FlockingStrategy(passive);
        SurvivalStrategy foraging = new ForagingStrategy(flocking);
        SurvivalStrategy scared = new ScaredStrategy(foraging); 
        this.setBrain(scared);
    }
}
