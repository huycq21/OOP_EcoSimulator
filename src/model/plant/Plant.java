package model.plant;

import model.Entity;
import model.Eatable;
import model.Ageable;
import model.Vector2D;

public abstract class Plant extends Entity implements Eatable, Ageable {
    protected double energyValue; // Lượng dinh dưỡng
    protected int age;            // Tuổi hiện tại (tính bằng số Tick hoặc số Giây)
    protected int maxAge;         // Tuổi thọ tối đa

    public Plant(Vector2D position, double size, double energyValue, int maxAge) {
        super(position, size);
        this.energyValue = energyValue;
        this.maxAge = maxAge;
        this.age = 0; // Mới mọc lên thì 0 tuổi
    }

    @Override
    public void update() {
        if (!isAlive) return;

        // Mỗi khung hình, cây sẽ già đi một chút
        growOlder();

        // Nếu quá già, cây tự khô héo và biến mất
        if (isTooOld()) {
            this.destroy(); 
        }
    }

    // --- Triển khai các hàm của Eatable ---
    @Override
    public double getEnergyValue() {
        return energyValue;
    }

    @Override
    public void getEaten() {
        this.destroy(); // Khi bị thỏ cắn, cây lập tức chuyển trạng thái isAlive = false
    }

    // --- Triển khai các hàm của Ageable ---
    @Override
    public void growOlder() {
        this.age++; 
    }

    @Override
    public boolean isTooOld() {
        return this.age >= this.maxAge;
    }
}