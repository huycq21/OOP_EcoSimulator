package model.carnivore;

import model.Vector2D;
import model.strategy.FlockingStrategy;
import model.strategy.HunterStrategy;
import model.strategy.PassiveStrategy;
import model.strategy.ScavengerStrategy;
import model.strategy.SurvivalStrategy;

public class Hyena extends Carnivore {

    public Hyena(Vector2D position) {
        // Tầm nhìn xa 150.0 để đánh hơi xác chết
        super(position, 4.5, 90, 160, 4.5, 150.0, 50.0, 35.0, 45);
        SurvivalStrategy huntLogic = new HunterStrategy(new PassiveStrategy());
        SurvivalStrategy flockLogic = new FlockingStrategy(huntLogic);
        SurvivalStrategy hyenaBrain = new ScavengerStrategy(flockLogic);
        this.setBrain(hyenaBrain);
        //Ưu tiên xác > Đi theo bầy > Không có xác mới săn mồi
    }
}