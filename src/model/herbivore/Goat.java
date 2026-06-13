package model.herbivore;

import model.Entity;
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
        super(position, 4.0, 60, 120, 6.5, 120.0);
        
        SurvivalStrategy passive = new PassiveStrategy();
        SurvivalStrategy flocking = new FlockingStrategy(passive);
        SurvivalStrategy foraging = new ForagingStrategy(flocking);
        SurvivalStrategy scared = new ScaredStrategy(foraging); 
        this.setBrain(scared);
    }
    @Override
    protected Entity createBaby(Vector2D position) {
        return new Goat(position); 
    }
}
 
