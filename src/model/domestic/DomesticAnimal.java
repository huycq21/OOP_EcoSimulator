package model.domestic;

import model.Animal;
import model.Vector2D;
import model.strategy.PassiveStrategy;
import model.herbivore.*;

public abstract class DomesticAnimal extends Herbivore {

    public DomesticAnimal(
            Vector2D position,
            double size,
            double maxHp,
            double maxEnergy,
            double speed,
            double visionRadius
    ) 
    {
        super(position, size, maxHp, maxEnergy, speed, visionRadius);
    }
}
