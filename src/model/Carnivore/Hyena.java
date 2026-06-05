package model.carnivore;

import model.Vector2D;
import model.strategy.*;
import model.herbivore.Deer;
import model.herbivore.Boar;
import model.herbivore.Rabbit;

public class Hyena extends Carnivore {
    public Hyena(Vector2D position) {
        // Tầm nhìn xa (150.0) cực kỳ lợi thế cho việc đánh hơi xác chết!
        super(position, 4.5, 90, 160, 4.5, 500.0, 50.0, 35.0, 45);
        
        // --- 1. THỰC ĐƠN PHÒNG KHI ĐÓI MÀ KHÔNG CÓ XÁC ---
        this.addPreyType(model.herbivore.Rabbit.class);
        this.addPreyType(model.herbivore.Deer.class);
        this.addPreyType(model.herbivore.Boar.class);
        this.addPreyType(model.herbivore.Goat.class);
        this.addPreyType(model.domestic.Cow.class);
        this.addPreyType(model.domestic.Pig.class);
        
        // --- 2. LẮP RÁP BỘ NÃO 5 TẦNG ---
        SurvivalStrategy passive = new PassiveStrategy(); // Rảnh rỗi đi dạo
        SurvivalStrategy packLogic = new PackFlockingStrategy(passive, 0.30, 2);
        SurvivalStrategy hunter = new HunterStrategy(packLogic); 
        SurvivalStrategy scared = new ScaredStrategy(hunter);
        // NHẶT XÁC ĐẶT RA NGOÀI CÙNG!
        SurvivalStrategy scavenger = new ScavengerStrategy(scared);
        
        this.setBrain(scavenger);
    }
}