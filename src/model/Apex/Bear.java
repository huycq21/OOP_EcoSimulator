package model.apex;

import model.Vector2D;
import model.environment.Environment;
import model.environment.Rectangle;
import model.strategy.HunterStrategy;
import model.Entity;

import java.util.List;

import model.Animal;

public class Bear extends ApexEntity {

    private double aoeRadius;

    public Bear(Vector2D position) {
        // Cân bằng dựa trên Sói (Wolf): HP 100, Speed 5.5, Dmg 60, Threat 80
        super(
            position,
            12.0,        // size: To hơn gấp đôi Sói
            400.0,       // maxHp: Cần tới 7 nhát cắn của Sói mới gục ngã
            500.0,       // maxEnergy: Dự trữ năng lượng khổng lồ
            3.0,         // speed: Chậm hơn Sói (5.5). Sói có thể Hit & Run!
            80.0,        // visionRadius: Tầm nhìn rộng
            150.0,       // strengthWeight: Mức độ đe dọa áp đảo hoàn toàn Sói
            100.0,       // attackDamage: Vả đúng 1 phát là Sói (100 HP) "đăng xuất"!
            90,          // attackCooldown: Đánh chậm hơn Sói (90 tick vs 60 tick)
            300          // spAttackCooldown: THÊM VÀO ĐÂY! (Hồi chiêu AOE mất 300 tick)
        );
        
        // Tầm vả AOE vươn ra gấp 1.5 lần cơ thể
        this.aoeRadius = this.getSize() * 1.5; 
        // Hổ / Gấu constructor:
        this.setBrain(new HunterStrategy()); // Dùng constructor mặc định
    }

    @Override
    public void performSpecialAbility(Environment env) {
        // Nếu đã chết hoặc đang chờ hồi chiêu thì không làm gì cả
        if (!isAlive() || currentCooldownTimer > 0) return;

        boolean hitSomeone = false;

        // 1. TẠO VÙNG TÌM KIẾM CHO QUAD TREE
        // Tâm là vị trí con Gấu, chiều rộng/dài chính là bán kính AOE
        Rectangle aoeRange = new Rectangle(
            this.getPosition().getX(), 
            this.getPosition().getY(), 
            this.aoeRadius, 
            this.aoeRadius
        );

        // 2. GỌI QUAD TREE LỌC THỰC THỂ (SIÊU NHANH)
        // Lưu ý: Class Environment của bạn cần có hàm getQuadTree() để lấy cây của frame hiện tại
        List<Entity> nearbyEntities = env.getQuadTree().query(aoeRange, null);

        // 3. XÉT VA CHẠM TRONG SỐ NHỮNG KẺ ĐEN ĐỦI ĐỨNG GẦN GẤU
        for (Entity entity : nearbyEntities) {
            // Không tự vả mặt mình, và chỉ vả động vật
            if (entity != this && entity.isAlive() && entity instanceof Animal && entity.getClass() != this.getClass()) {
                
                Animal prey = (Animal) entity;
                double distance = this.getPosition().distanceTo(prey.getPosition());

                // QuadTree trả về hình chữ nhật, đòn đánh là hình tròn nên ta chốt lại bằng distance
                if (distance <= this.aoeRadius) {
                    prey.setHp(prey.getHp() - this.attackDamage);
                    hitSomeone = true;
                    System.out.println("Gấu vả AOE trúng " + prey.getClass().getSimpleName() + " (-" + this.attackDamage + " HP)!");
                }
            }
        }

        // Chỉ tính là đã xài chiêu nếu thực sự vả trúng ai đó
        if (hitSomeone) {
            this.currentCooldownTimer = this.attackCooldown;
        }
    }
}
