package model.domestic;

import model.Animal;
import model.Vector2D;
import model.strategy.PassiveStrategy;

public abstract class DomesticAnimal extends Animal {
    private final String penLayerName;

    public DomesticAnimal(
            Vector2D position,
            double size,
            double maxHp,
            double maxEnergy,
            double speed,
            double visionRadius,
            String penLayerName
    ) {
        super(position, size, maxHp, maxEnergy, speed, visionRadius);
        this.penLayerName = penLayerName;
        this.setBrain(new PassiveStrategy());
    }

    public String getPenLayerName() {
        return penLayerName;
    }
}
