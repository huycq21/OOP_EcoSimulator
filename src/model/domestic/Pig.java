package model.domestic;

import model.Entity;
import model.Vector2D;
import model.herbivore.Horse;
import model.strategy.*;

public class Pig extends DomesticAnimal {
    public Pig(Vector2D position) {
        super(position, 3.8, 70, 140, 0.9, 40);
        SurvivalStrategy passive = new PassiveStrategy();
        SurvivalStrategy foraging = new ForagingStrategy(passive);
        SurvivalStrategy scared = new ScaredStrategy(foraging); 
        this.setBrain(scared);
    }
    @Override
    protected Entity createBaby(Vector2D position) {
        return new Pig(position); 
    }
}
