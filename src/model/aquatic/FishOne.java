package model.aquatic;

import model.Vector2D;
import model.strategy.ForagingStrategy;
import model.strategy.PassiveStrategy;
import model.strategy.ScaredStrategy;
import model.strategy.SurvivalStrategy;

public class FishOne extends Fish {
    public FishOne(Vector2D position) {
        super(position, 2.0, 20, 120, 2.8, 45);
        SurvivalStrategy passive = new PassiveStrategy();
        SurvivalStrategy foraging = new ForagingStrategy(passive);
        SurvivalStrategy scared = new ScaredStrategy(foraging); 
        this.setBrain(scared);
    }
}
