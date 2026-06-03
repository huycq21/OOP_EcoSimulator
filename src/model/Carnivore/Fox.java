package model.carnivore;

import model.Vector2D;
import model.strategy.*;
import model.herbivore.BlackGrouse;
import model.herbivore.*;
import model.domestic.*;

public class Fox extends Carnivore {
    
    public Fox(Vector2D position) {
        // Kích thước 3.5, Máu 60, Năng lượng 120, Tốc độ 6.0, Tầm nhìn 70.0
        // Khí chất đe dọa 40.0, Sát thương 30.0, Hồi chiêu 40 tick
        super(position, 3.5, 60, 120, 6.0, 70.0, 40.0, 30.0, 40);
        
        // Cáo có mũi cực thính để săn mồi
        this.setPreyDetectionRadius(110.0);
        
        // Thực đơn của Cáo
        this.addPreyType(model.herbivore.Rabbit.class);
        this.addPreyType(model.herbivore.BlackGrouse.class); // Gà rừng
        this.addPreyType(model.domestic.Chicken.class);      // Gà nhà (Đặc sản của Cáo!)
        
        // --- LẮP RÁP BỘ NÃO 4 TẦNG ---
        SurvivalStrategy passive = new PassiveStrategy();               // Đi dạo
        SurvivalStrategy hunter = new HunterStrategy(passive);          // Săn mồi
        SurvivalStrategy scared = new ScavengerStrategy(hunter);     // Ăn xác
        SurvivalStrategy scavenger = new ScaredStrategy(scared);        // Bỏ chạy khi gặp thú lớn (Hổ, Gấu, Sói...)
        
        this.setBrain(scavenger);
    }
}