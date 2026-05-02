package model.plant;

import model.Vector2D;
import model.*;

public class SmallTree extends Plant {
    private double energyValue;

    public SmallTree(Vector2D position) {
        super(position, 6.0, 150, 10000); // Kích thước lớn
    }

}