package model.herbivore;

import model.Vector2D;
import model.strategy.FlockingStrategy;
import model.strategy.PassiveStrategy;
import model.strategy.ScaredStrategy;

public class Elephant extends Herbivore {

    public Elephant(Vector2D position) {
        // Kích thước: 12.0 (Khổng lồ), Máu: 500 (Cực trâu), Năng lượng: 400
        // Tốc độ: 2.0 (Chậm), Tầm nhìn: 50.0
        super(position, 12.0, 500, 400, 2.0, 50.0);
        
        // Voi thì cứ đi dạo ngẫu nhiên thôi, thú nhỏ tự phải né nó
        // Trong constructor của Zebra:
        this.setBrain(new ScaredStrategy(new FlockingStrategy(new PassiveStrategy())));
    }
}