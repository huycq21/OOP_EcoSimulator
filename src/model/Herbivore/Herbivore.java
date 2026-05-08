package model.herbivore;

import model.Animal;
import model.Vector2D;

public abstract class Herbivore extends Animal {
    protected double predatorDetectionRadius;

    // Constructor gọi thẳng lên lớp cha (Animal)
    public Herbivore(Vector2D position, double size, double maxHp, double maxEnergy, double speed, double visionRadius) {
        super(position, size, maxHp, maxEnergy, speed, visionRadius);
        this.predatorDetectionRadius = visionRadius * 0.75;
    }

    public double getPredatorDetectionRadius() {
        return predatorDetectionRadius;
    }

    public void setPredatorDetectionRadius(double predatorDetectionRadius) {
        this.predatorDetectionRadius = predatorDetectionRadius;
    }

    // Tạm thời chưa thêm logic gì phức tạp, 
    // sau này ta có thể thêm các hàm đặc thù của loài ăn cỏ như eatPlant(Plant p) vào đây.
}
