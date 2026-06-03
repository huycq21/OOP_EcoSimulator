package model.aquatic;

import model.Animal;
import model.Vector2D;
import model.strategy.PassiveStrategy;

public abstract class Fish extends Animal {
    public Fish(Vector2D position, double size, double maxHp, double maxEnergy, double speed, double visionRadius) {
        super(position, size, maxHp, maxEnergy, speed, visionRadius);
        this.canEnterWater = true;
        this.requiresWater = true;
        this.setBrain(new PassiveStrategy());
    }
}
