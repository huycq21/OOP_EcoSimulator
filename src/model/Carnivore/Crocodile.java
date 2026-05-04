package model.carnivore;

import model.Vector2D;
import model.*;
import model.strategy.HunterStrategy;

public class Crocodile extends Carnivore {

    public Crocodile(Vector2D position) {
        // Tốc độ 1.5 cực chậm, nhưng damage 150.0 cực khủng
        super(position, 6.0, 250, 150, 1.5, 50.0, 120.0, 150.0, 120);
        this.setBrain(new HunterStrategy());
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
