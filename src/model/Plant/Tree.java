package model.plant;

import model.Vector2D;
import model.*;

public class Tree extends Plant implements Eatable {
    private double energyValue;

    public Tree(Vector2D position) {
        super(position, 8.0, 150, 20000); // Kích thước khổng lồ
    }

    // --- Triển khai 2 hàm bắt buộc của interface Eatable ---
    
    @Override
    public double getEnergyValue() {
        return this.energyValue;
    }

    @Override
    public void getEaten() {
        this.destroy();
    }
}