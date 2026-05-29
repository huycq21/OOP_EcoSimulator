package model.herbivore;

import model.Vector2D;
import model.strategy.ForagingStrategy;

public class Elephant extends Herbivore {

    public Elephant(Vector2D position) {
        // Kích thước: 12.0 (Khổng lồ), Máu: 500 (Cực trâu), Năng lượng: 400
        // Tốc độ: 2.0 (Chậm), Tầm nhìn: 50.0
        super(position, 12.0, 500, 400, 2.0, 50.0);
        
        // Voi thì cứ đi dạongẫu nhiên thôi, thú nhỏ tự phải né nó
        this.setBrain(new ForagingStrategy());
    }
}
