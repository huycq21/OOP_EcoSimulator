package model.plant;

import model.Vector2D;
import model.*;

public class Grass extends Plant {

    public Grass(Vector2D position) {
        // Kích thước: 1.0 (nhỏ xíu), Dinh dưỡng: 20, Tuổi thọ: 1500 (tick)
        super(position, 1.0, 20.0, 1500);
    }

    @Override
    public double getEnergyValue() {
        return 20.0;
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
