package model.carnivore;

import model.Vector2D;
import model.strategy.*;

public class Wolf extends Carnivore {

    public Wolf(Vector2D position) {
        super(position, 5.0, 100.0, 200.0, 5.5, 50.0, 80, 60.0, 60);
        this.setBrain(new HunterStrategy());
    }
}
