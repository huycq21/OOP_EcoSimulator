package model.herbivore;

import model.Vector2D;
import model.strategy.ForagingStrategy;
import model.strategy.PassiveStrategy;
import model.strategy.ScaredStrategy;
import model.strategy.SurvivalStrategy;
import model.Reproducible;
import model.Animal;
import model.Entity;
import model.environment.Environment;
import model.environment.Season;

public class Rabbit extends Herbivore {

    private int reproductionCooldown;

    
    // Thỏ có các chỉ số mặc định: Kích thước 3.0, Máu 50, Năng lượng 100, Tốc độ 5.0, Tầm nhìn 50.0
    public Rabbit(Vector2D position) {
        super(position, 3.0, 50, 100, 5.0, 100.0);
        
        SurvivalStrategy passive = new PassiveStrategy();
        SurvivalStrategy foraging = new ForagingStrategy(passive);
        SurvivalStrategy scared = new ScaredStrategy(foraging); 
        this.setBrain(scared);
    }
    @Override
    protected Entity createBaby(Vector2D position) {
        return new Rabbit(position); // Thỏ đẻ ra Thỏ
    }

}
