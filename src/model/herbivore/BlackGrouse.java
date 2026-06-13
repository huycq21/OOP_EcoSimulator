package model.herbivore;

import model.Vector2D;
import model.strategy.ForagingStrategy;
import model.strategy.PassiveStrategy;
import model.strategy.ScaredStrategy;
import model.strategy.SurvivalStrategy;

public class BlackGrouse extends Herbivore {
    public BlackGrouse(Vector2D position) {
        super(position, 2.2, 35, 80, 4.8, 55.0);
        SurvivalStrategy passive = new PassiveStrategy();
        SurvivalStrategy foraging = new ForagingStrategy(passive);
        SurvivalStrategy scared = new ScaredStrategy(foraging); 
        this.setBrain(scared);
    }
}
