package model.carnivore;

import model.Vector2D;
import model.strategy.*;
import model.herbivore.Deer;
import model.herbivore.Boar;
import model.herbivore.Rabbit;

public class Hyena extends Carnivore {
    public Hyena(Vector2D position) {
        // Tầm nhìn xa (150.0) cực kỳ lợi thế cho việc đánh hơi xác chết!
        super(position, 4.5, 90, 160, 4.5, 150.0, 50.0, 35.0, 45);
        
        // --- 1. THỰC ĐƠN PHÒNG KHI ĐÓI MÀ KHÔNG CÓ XÁC ---
        this.addPreyType(model.herbivore.Rabbit.class);
        this.addPreyType(model.herbivore.Deer.class);
        this.addPreyType(model.herbivore.Boar.class);
        this.addPreyType(model.herbivore.Goat.class);
        this.addPreyType(model.domestic.Cow.class);
        this.addPreyType(model.domestic.Pig.class);
        
        // --- 2. LẮP RÁP BỘ NÃO 5 TẦNG ---
        SurvivalStrategy passive = new PassiveStrategy();
        
        SurvivalStrategy hunter = new HunterStrategy(passive);
        
        // Bầy đàn: Đi đông buff mạnh (Tối đa x3.0 sức mạnh)
        SurvivalStrategy packLogic = new PackFlockingStrategy(hunter, 0.20, 3.5);
        
        // Ăn xác chết: Ưu tiên cao hơn tự đi săn!
        SurvivalStrategy scavenger = new ScavengerStrategy(packLogic);
        
        // Sợ hãi: Gặp Apex Predators (Hổ, Gấu) thì vẫn phải vắt chân lên cổ mà chạy
        SurvivalStrategy scared = new ScaredStrategy(scavenger);
        
        this.setBrain(scared);
    }
}