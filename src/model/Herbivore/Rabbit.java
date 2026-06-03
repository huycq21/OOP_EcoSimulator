package model.herbivore;

import model.Vector2D;
import model.strategy.ForagingStrategy;
import model.strategy.PassiveStrategy;
import model.strategy.ScaredStrategy;
import model.strategy.SurvivalStrategy;

public class Rabbit extends Herbivore {

    // Thỏ có các chỉ số mặc định: Kích thước 3.0, Máu 50, Năng lượng 100, Tốc độ 5.0, Tầm nhìn 50.0
    public Rabbit(Vector2D position) {
        super(position, 3.0, 50, 100, 5.0, 50.0);
        
        SurvivalStrategy passive = new PassiveStrategy();
        SurvivalStrategy foraging = new ForagingStrategy(passive);
        SurvivalStrategy scared = new ScaredStrategy(foraging); 
        this.setBrain(scared);
    }
}
