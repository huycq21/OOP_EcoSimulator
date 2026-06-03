package model.plant;

import model.Vector2D;
import model.Eatable;
import model.Ageable;

public class Mushroom extends Plant {
    public Mushroom(Vector2D position) {
        super(position, 1.0, -40.0, 800);
    }
}
