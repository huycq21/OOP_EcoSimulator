package model.herbivore;

import model.Vector2D;
import model.strategy.FlockingStrategy;
import model.strategy.ForagingStrategy;
import model.strategy.PassiveStrategy;
import model.strategy.ScaredStrategy;
import model.strategy.SurvivalStrategy;

public class Goat extends Herbivore {

    public Goat(Vector2D position) {
        // Kích thước: 4.0, Máu: 60, Năng lượng: 120
        // Tốc độ: 6.5, Tầm nhìn: 70.0
        super(position, 4.0, 60, 120, 6.5, 70.0);
        
        SurvivalStrategy passive = new PassiveStrategy();
        SurvivalStrategy foraging = new ForagingStrategy(passive);
        SurvivalStrategy flocking = new FlockingStrategy(foraging);
        SurvivalStrategy scared = new ScaredStrategy(flocking); 
        
        this.setBrain(scared);
    }
}
 
