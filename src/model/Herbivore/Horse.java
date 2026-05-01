package model.herbivore;

import model.Vector2D;
import model.strategy.PassiveStrategy;

public class Horse extends Herbivore {

    public Horse(Vector2D position) {
        // Kích thước: 5.0, Máu: 80, Năng lượng: 150
        // Tốc độ: 5.5, Tầm nhìn: 60.0
        super(position, 5.0, 80, 150, 5.5, 60.0);
        
        // Lắp não mặc định. 
        // TODO: Sau này thay bằng FlockingStrategy để chúng tự tìm nhau và đi theo đàn
        this.setBrain(new PassiveStrategy());
    }
}