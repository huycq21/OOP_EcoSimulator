package model.herbivore;

import model.Animal;
import model.Entity;
import model.Reproducible;
import model.Vector2D;

public abstract class Herbivore extends Animal {

    // Constructor gọi thẳng lên lớp cha (Animal)
    public Herbivore(Vector2D position, double size, double maxHp, double maxEnergy, double speed, double visionRadius) {
        super(position, size, maxHp, maxEnergy, speed, visionRadius);
    }

    public double getVisionRadius() {
        return visionRadius;
    }

    public void setVisionRadius(double Radius) {
        this.visionRadius = Radius;
    }
}
