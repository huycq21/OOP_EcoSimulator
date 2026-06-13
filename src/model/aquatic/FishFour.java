package model.aquatic;

import model.Entity;
import model.Vector2D;
import model.domestic.Pig;
import model.strategy.ForagingStrategy;
import model.strategy.PassiveStrategy;
import model.strategy.ScaredStrategy;
import model.strategy.SurvivalStrategy;

public class FishFour extends Fish {
    public FishFour(Vector2D position) {
        super(position, 2.4, 28, 135, 2.6, 50);
        SurvivalStrategy passive = new PassiveStrategy();
        SurvivalStrategy foraging = new ForagingStrategy(passive);
        SurvivalStrategy scared = new ScaredStrategy(foraging); 
        this.setBrain(scared);
    }
    @Override
    protected Entity createBaby(Vector2D position) {
        return new FishFour(position); 
    }
}
