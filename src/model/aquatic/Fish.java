package model.aquatic;

import model.Animal;
import model.Vector2D;
import model.herbivore.Herbivore;
import model.strategy.ForagingStrategy;
import model.strategy.PassiveStrategy;
import model.strategy.ScaredStrategy;
import model.strategy.SurvivalStrategy;

public abstract class Fish extends Herbivore {
    public Fish(Vector2D position, double size, double maxHp, double maxEnergy, double speed, double visionRadius) {
        super(position, size, maxHp, maxEnergy, speed, visionRadius);
        this.canEnterWater = true;
        this.requiresWater = true;
    }
}
