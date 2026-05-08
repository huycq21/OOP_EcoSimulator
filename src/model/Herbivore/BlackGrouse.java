package model.herbivore;

import model.Vector2D;
import model.strategy.ForagingStrategy;

public class BlackGrouse extends Herbivore {
    public BlackGrouse(Vector2D position) {
        super(position, 2.2, 35, 80, 4.8, 55.0);
        this.setBrain(new ForagingStrategy());
    }
}
