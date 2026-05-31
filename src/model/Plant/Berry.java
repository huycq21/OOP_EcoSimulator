package model.plant;

import model.Eatable;
import model.Ageable;
import model.Vector2D;

public class Berry extends Plant {
    private boolean hasFruits;  //Đang có quả hay không
    private int regrowthTime;   //Thời gian mọc lại quả
    private int currentRegrowthTimer;  // Bộ đếm thời gian mọc lại

    public Berry(Vector2D position) {
        super(position, 3.0, 50.0, 5000);
        this.hasFruits = true;
        this.regrowthTime = 1000; // Sau khi bị ăn, mất 1000 tick để mọc lại quả
        this.currentRegrowthTimer = 0;
    }
    @Override
    public void update() {
        super.update();
        
        if (!isAlive) return;

        if (!hasFruits) {
            currentRegrowthTimer++;
            if (currentRegrowthTimer >= regrowthTime) {
                hasFruits = true;
                currentRegrowthTimer = 0;
            }
        }
    }

    @Override
    public double getEnergyValue() {
        if (hasFruits) {
            return this.energyValue; // Nếu còn quả, trả về giá trị dinh dưỡng
        } else {
            return 0; // Nếu không còn quả, không có dinh dưỡng
        }
    }

    @Override
    public void getEaten() {
        if (hasFruits) {
            this.hasFruits = false; // Khi bị ăn, quả biến mất
            this.currentRegrowthTimer = 0;
        }
    }

    public boolean hasFruits() {
        return hasFruits;
    }
    public boolean isAlive() {
        return isAlive;
    }
}
