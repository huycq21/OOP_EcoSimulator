package model.aquatic;

import model.Entity;
import model.Vector2D;
import model.domestic.Pig;
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
    @Override
    protected Entity createBaby(Vector2D position) {
        return new FishOne(position); 
    }
}
