package model.domestic;

import model.Entity;
import model.Vector2D;
import model.strategy.ForagingStrategy;
import model.strategy.PassiveStrategy;
import model.strategy.ScaredStrategy;
import model.strategy.SurvivalStrategy;

public class Cow extends DomesticAnimal {
    public Cow(Vector2D position) {
        super(position, 5.5, 120, 180, 0.7, 45);
        SurvivalStrategy passive = new PassiveStrategy();
        SurvivalStrategy foraging = new ForagingStrategy(passive);
        SurvivalStrategy scared = new ScaredStrategy(foraging); 
        this.setBrain(scared);
    }
    @Override
    protected Entity createBaby(Vector2D position) {
        return new Cow(position); 
    }
}
