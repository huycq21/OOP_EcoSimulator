package model.carnivore;

import model.Animal;
import model.Entity;
import model.Vector2D;
import model.Reproducible;
import model.strategy.*;
import model.herbivore.Rabbit;
import model.herbivore.BlackGrouse;
import model.domestic.Chicken;
import model.domestic.Pig;

public class Fox extends Carnivore implements Reproducible {
    
    public Fox(Vector2D position) {
        // --- THÔNG SỐ CƠ BẢN (Kết hợp tối ưu từ 2 bản) ---
        // Kích thước: 3.5
        // Máu (Max HP): 60 | Năng lượng (Max Energy): 120
        // Tốc độ di chuyển: 6.5 (Đủ nhanh để rượt đuổi con mồi)
        // Tầm nhìn cốt lõi (Vision Radius): 160.0 (Phát hiện kẻ thù lớn sớm để chạy)
        // Khí chất đe dọa (Strength Weight): 40.0
        // Sát thương đòn cắn (Attack Damage): 30.0 (Cần 2 phát để tiễn Thỏ 50 HP)
        // Hồi chiêu tấn công (Attack Cooldown): 40 tick (~1.3 giây ở 30 FPS)
        super(position, 3.5, 60, 120, 6.5, 160.0, 40.0, 30.0, 40);
        
        // Cấu hình riêng bán kính kích hoạt trạng thái đi săn (Từ bản mới)
        this.setPreyDetectionRadius(110.0);
        this.setStrikeRadius(55.0); // Áp sát bằng 50% radar săn mồi sẽ bộc phát tốc độ

        // --- THỰC ĐƠN CỦA CÁO ---
        this.addPreyType(Rabbit.class);       // Thỏ
        this.addPreyType(BlackGrouse.class); // Gà rừng
        this.addPreyType(Chicken.class);     // Gà nhà (Món khoái khẩu)
        
        // --- LẮP RÁP BỘ NÃO AI 4 TẦNG (Decorator Pattern từ bản cũ) ---
        // Thứ tự bọc chiến thuật: Thấp nhất (Passive) -> Ưu tiên cao nhất (Scared)
        SurvivalStrategy passiveState   = new PassiveStrategy();               // Tầng 4: Không có gì thì đi dạo tung tăng
        SurvivalStrategy hunterState    = new HunterStrategy(passiveState);    // Tầng 3: Đói bụng thì chủ động đi săn mồi
        SurvivalStrategy scavengerState = new ScavengerStrategy(hunterState);   // Tầng 2: Thấy xác chết thì lại ăn ké tiết kiệm sức
        SurvivalStrategy scaredState    = new ScaredStrategy(scavengerState);  // Tầng 1: Gặp thú lớn (Gấu, Sói, Người) là vắt chân lên cổ chạy trước!
        
        // Nạp bộ não hoàn chỉnh vào thực thể
        this.setBrain(scaredState);
    }

    // --- CƠ CHẾ SINH TRƯỞNG & SINH SẢN (Từ Bản Mới) ---

    @Override
    protected Entity createBaby(Vector2D position) {
        return new Fox(position); 
    }
}