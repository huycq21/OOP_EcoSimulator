package model.plant;

import model.Vector2D;
import model.*;

public class Algae extends Plant {

    public Algae(Vector2D position) {
        super(position, 1.5, 15.0, 1200);
    }

    @Override
    public double getEnergyValue() {
        return 15.0;
    }

    @Override
    public void getEaten() {
        this.destroy();
    }

    @Override
    public void growOlder() {
        age++;
    }

    @Override
    public boolean isTooOld() {
        return age > maxAge;
    }
}
