package model.herbivore;

import model.*;
import model.strategy.PassiveStrategy;

public class Goat extends Herbivore {

    public Goat(Vector2D position) {
        // Kích thước: 4.0, Máu: 60, Năng lượng: 120
        // Tốc độ: 6.5, Tầm nhìn: 70.0
        super(position, 4.0, 60, 120, 6.5, 70.0);
        this.setBrain(new PassiveStrategy());
    }
}
 