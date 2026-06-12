package model.herbivore;

import model.Vector2D;
import model.strategy.ForagingStrategy;
import model.strategy.PassiveStrategy;
import model.strategy.ScaredStrategy;
import model.strategy.SurvivalStrategy;

public class Boar extends Herbivore {

    public Boar(Vector2D position) {
        // Kích thước: 4.5, Máu: 120 (Khá trâu), Năng lượng: 150
        // Tốc độ: 4.5, Tầm nhìn: 50.0
        super(position, 4.5, 120, 150, 4.5, 120.0);
        
        SurvivalStrategy passive = new PassiveStrategy();
        SurvivalStrategy foraging = new ForagingStrategy(passive);
        SurvivalStrategy scared = new ScaredStrategy(foraging); 
        this.setBrain(scared);
    }
}
