package model.carnivore;

import model.Entity;
import model.Vector2D;
import model.domestic.Pig;
import model.strategy.*;
import model.herbivore.Deer;
import model.herbivore.Boar;
import model.herbivore.Rabbit;

public class Hyena extends Carnivore {
    public Hyena(Vector2D position) {
        // Tầm nhìn xa (150.0) cực kỳ lợi thế cho việc đánh hơi xác chết!
        super(position, 4.5, 90, 160, 7.0, 500.0, 50.0, 35.0, 45);
        
        // --- 1. THỰC ĐƠN PHÒNG KHI ĐÓI MÀ KHÔNG CÓ XÁC ---
        this.addPreyType(model.herbivore.Rabbit.class);
        this.addPreyType(model.herbivore.Deer.class);
        this.addPreyType(model.herbivore.Boar.class);
        this.addPreyType(model.herbivore.Goat.class);
        this.addPreyType(model.domestic.Cow.class);
        this.addPreyType(model.domestic.Pig.class);
        
        // --- 2. LẮP RÁP BỘ NÃO 5 TẦNG ---
        SurvivalStrategy passive = new PassiveStrategy(); // Rảnh rỗi đi dạo
        SurvivalStrategy flocking = new FlockingStrategy(passive);
        SurvivalStrategy hunter = new HunterStrategy(flocking); 
        SurvivalStrategy scared = new ScaredStrategy(hunter);
        SurvivalStrategy scavenger = new ScavengerStrategy(scared);
        SurvivalStrategy packflock = new PackFlockingStrategy(scavenger, 0.3, 2);
        this.setBrain(packflock);
    }
    @Override
    protected Entity createBaby(Vector2D position) {
        return new Hyena(position); 
    }
}