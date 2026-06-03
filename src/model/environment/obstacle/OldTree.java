package model.environment.obstacle;

import model.*;

public class OldTree extends Obstacle implements Eatable, Ageable {
    private double energyValue;
    private int age;
    private int maxAge;

    public OldTree(Vector2D position) {
        super(position, 8.0); // Kích thước khổng lồ
        this.energyValue = 100.0;
        this.age = 0;
        this.maxAge = 15000;
    }

    // --- Triển khai 2 hàm bắt buộc của interface Eatable ---
    
    @Override
    public double getEnergyValue() {
        return this.energyValue;
    }

    @Override
    public void getEaten() {
        //bị ăn phần lá bên dưới vẫn mọc lại được
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
