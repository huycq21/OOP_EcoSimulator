package model.carnivore;

import model.Vector2D;
import model.strategy.*;
import model.herbivore.Deer;
import model.herbivore.Boar;
import model.herbivore.Rabbit;

public class Wolf extends Carnivore {

    public Wolf(Vector2D position) {
        // Stats: Kích thước 5.0, HP 100, Năng lượng 200, Tốc độ 5.5, Tầm nhìn 50.0
        // Khí chất đe dọa 80.0, Sát thương 60.0, Cooldown 60 tick (~1 giây)
        super(position, 5.0, 100.0, 200.0, 7.5, 150.0, 80.0, 60.0, 60);

        // --- 1. THỰC ĐƠN CỦA SÓI ---
        this.addPreyType(model.herbivore.Rabbit.class);
        this.addPreyType(model.herbivore.Deer.class);
        this.addPreyType(model.herbivore.Boar.class);        // Chơi hội đồng Lợn rừng
        this.addPreyType(model.herbivore.Goat.class);
        this.addPreyType(model.herbivore.Horse.class);       // Ngựa cũng không tha
        this.addPreyType(model.domestic.Cow.class);          // Bò
        this.addPreyType(model.domestic.Pig.class);

        // --- 2. LẮP RÁP BỘ NÃO (5 TẦNG) ---
        // Đói thì chuyển sang chế độ đi săn
        
        // TẬP TÍNH BẦY ĐÀN (Bọc ngoài Đi săn)
        // Tìm đồng loại hú hét đi chung. Mỗi đồng bọn buff 30% sức mạnh (0.3), Tối đa x1.9 sức mạnh.
        // Sói đi 3 con: 80.0 * 1.6 = 128.0 (Đủ sức dọa lợn rừng và linh cẩu)
        // rảnh thì đi cùng nhau
        SurvivalStrategy passive = new PassiveStrategy(); // Rảnh rỗi đi dạo
        SurvivalStrategy flocking = new FlockingStrategy(passive);
        SurvivalStrategy hunter = new HunterStrategy(flocking); 
        // Rảnh thì đi cùng nhau
        SurvivalStrategy scared = new ScaredStrategy(hunter);
        // NHẶT XÁC ĐẶT RA NGOÀI CÙNG!
        SurvivalStrategy scavenger = new ScavengerStrategy(scared);
        SurvivalStrategy packflock = new PackFlockingStrategy(scavenger, 0.3, 2);
        this.setBrain(packflock);
    }
}