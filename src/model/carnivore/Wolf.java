package model.carnivore;

import model.Animal;
import model.Entity;
import model.Vector2D;
import model.Reproducible;
import model.strategy.*;
import model.herbivore.*;
import model.domestic.*;

public class Wolf extends Carnivore implements Reproducible {

    public Wolf(Vector2D position) {
        // --- THÔNG SỐ CƠ BẢN (Ưu tiên chỉ số tối ưu từ bản cũ) ---
        // Kích thước: 5.0 | Máu (Max HP): 100.0 | Năng lượng (Max Energy): 200.0
        // Tốc độ di chuyển: 7.5 (Đảm bảo tốc độ săn đuổi theo đàn)
        // Tầm nhìn cốt lõi (Vision Radius): 150.0 (Để quét đồng đội và con mồi diện rộng)
        // Khí chất đe dọa gốc (Strength Weight): 80.0
        // Sát thương (Attack Damage): 60.0
        // Hồi chiêu tấn công (Attack Cooldown): 60 tick (~2 giây ở 30 FPS)
        super(position, 5.0, 100.0, 200.0, 7.5, 150.0, 80.0, 60.0, 60);

        // Đặt bán kính săn mồi và vồ mồi bổ trợ từ Carnivore
        this.setPreyDetectionRadius(150.0);
        this.setStrikeRadius(75.0); 

        // --- 1. THỰC ĐƠN SĂN MỒI CỦA SÓI ---
        this.addPreyType(Rabbit.class);       // Thỏ
        this.addPreyType(Deer.class);         // Hươu
        this.addPreyType(Boar.class);         // Lợn rừng (Chơi hội đồng)
        this.addPreyType(Goat.class);         // Dê hoang
        this.addPreyType(Horse.class);        // Ngựa
        this.addPreyType(Cow.class);          // Bò nhà
        this.addPreyType(Pig.class);          // Lợn nhà

        // --- 2. LẮP RÁP BỘ NÃO AI ĐA TẦNG (Decorator Pattern) ---
        // Cấu hình tập tính bầy đàn: Mỗi đồng bọn trong tầm nhìn buff 30% (0.3) sức mạnh, tối đa 2 con đồng hành (x1.9)
        SurvivalStrategy passiveState      = new PassiveStrategy();
        SurvivalStrategy packFlockState    = new PackFlockingStrategy(passiveState, 0.30, 2); 
        SurvivalStrategy hunterState       = new HunterStrategy(packFlockState);
        SurvivalStrategy scaredState       = new ScaredStrategy(hunterState);
        SurvivalStrategy scavengerState    = new ScavengerStrategy(scaredState); 
        
        // Nạp bộ não 5 tầng hoàn chỉnh vào thực thể Sói
        this.setBrain(scavengerState);
    }

    // --- CƠ CHẾ SINH TRƯỞNG & SINH SẢN CHUẨN (Từ Bản Mới) ---

    @Override
    public void growOlder() {
        this.age++;
    }

    @Override
    public boolean isTooOld() {
        return this.age > this.maxAge;
    }

    @Override
    public Entity reproduce(Animal partner) {
        // Sinh sản tiêu tốn của cả bố và mẹ sói 50% lượng năng lượng hiện tại
        this.setEnergy(this.getEnergy() * 0.5);
        partner.setEnergy(partner.getEnergy() * 0.5);

        // Tạo ra một chú Sói con nằm lệch một khoảng nhỏ so với vị trí của mẹ (+15px)
        Vector2D babyPosition = new Vector2D(
            this.getPosition().getX() + 15,
            this.getPosition().getY() + 15
        );

        System.out.println("LOG: Một chú Sói con bầy đàn đã chào đời tại tọa độ " + babyPosition);
        return new Wolf(babyPosition);
    }
}