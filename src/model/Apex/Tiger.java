package model.apex;

import model.Vector2D;
import model.environment.Environment;
import model.environment.Rectangle;
import model.strategy.HunterStrategy;
import model.Entity;
import model.Animal;

import java.util.List;

public class Tiger extends ApexEntity {

    private double pounceRadius; // Tầm vồ mồi (xa hơn đòn đánh thường)
    private double critMultiplier; // Hệ số sát thương chí mạng khi vồ

    public Tiger(Vector2D position) {
        // Cân bằng so với Sói (HP 100, Speed 5.5, Dmg 60) và Gấu (HP 400, Speed 3.0, Dmg 100)
        super(
            position,
            10.0,        // size: To hơn Sói nhưng gọn gàng hơn Gấu (12.0)
            250.0,       // maxHp: Sinh tồn tốt, nhưng không "trâu" bằng Gấu
            400.0,       // maxEnergy: Năng lượng cao để rình rập và bứt tốc
            6.0,         // speed: Nhanh hơn Sói (5.5)
            120.0,       // visionRadius: Tầm nhìn cực xa
            140.0,       // strengthWeight: Mức độ đe dọa cực cao, chỉ kém Gấu
            120.0,       // attackDamage: Sát thương tay đôi CAO NHẤT
            70,          // attackCooldown: Tốc độ ra đòn nhanh hơn Gấu (90)
            240          // spAttackCooldown: THÊM VÀO ĐÂY! (Hồi chiêu vồ mồi 240 tick)
        );
        
        // Tầm vồ mồi xa gấp 4 lần kích thước cơ thể, cho phép lao đến bất ngờ
        this.pounceRadius = this.getSize() * 4.0; 
        
        // Đòn vồ mồi sẽ gây 1.5x sát thương thông thường (120 * 1.5 = 180 dmg)
        this.critMultiplier = 1.5;
        this.setBrain(new HunterStrategy()); // Dùng constructor mặc định
    }

    @Override
    public void performSpecialAbility(Environment env) {
        // Nếu đã chết hoặc đang chờ hồi chiêu thì không làm gì cả
        if (!isAlive() || currentCooldownTimer > 0) return;

        // 1. TẠO VÙNG TÌM KIẾM CHO KỸ NĂNG VỒ MỒI
        Rectangle pounceRange = new Rectangle(
            this.getPosition().getX(), 
            this.getPosition().getY(), 
            this.pounceRadius, 
            this.pounceRadius
        );

        // 2. LỌC THỰC THỂ XUNG QUANH TỪ QUAD TREE
        List<Entity> nearbyEntities = env.getQuadTree().query(pounceRange, null);

        Animal closestPrey = null;
        double minDistance = Double.MAX_VALUE;

        // 3. TÌM CON MỒI GẦN NHẤT TRONG TẦM VỒ (Đặc trưng săn mồi đơn độc, nhắm vào 1 mục tiêu)
        for (Entity entity : nearbyEntities) {
            if (entity != this && entity.isAlive() && entity instanceof Animal && entity.getClass() != this.getClass()) {
                
                Animal prey = (Animal) entity;
                double distance = this.getPosition().distanceTo(prey.getPosition());

                if (distance <= this.pounceRadius && distance < minDistance) {
                    minDistance = distance;
                    closestPrey = prey;
                }
            }
        }

        // 4. THỰC HIỆN KỸ NĂNG: ÁP SÁT VÀ KẾT LIỄU
        if (closestPrey != null) {
            // Hổ lập tức lao đến (áp sát) vị trí của con mồi
            this.getPosition().setX(closestPrey.getPosition().getX());
            this.getPosition().setY(closestPrey.getPosition().getY());

            // Gây sát thương chí mạng
            double finalDamage = this.attackDamage * this.critMultiplier;
            closestPrey.setHp(closestPrey.getHp() - finalDamage);
            
            System.out.println("Hổ lao đến vồ chí mạng " + closestPrey.getClass().getSimpleName() 
                               + " (-" + finalDamage + " HP)! Kẻ săn mồi áp đảo!");

            // Đưa kỹ năng vào thời gian hồi
            this.currentCooldownTimer = this.attackCooldown;
        }
    }
}