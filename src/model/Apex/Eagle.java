package model.apex;

import model.Vector2D;
import model.environment.Environment;
import model.environment.Rectangle;
import model.strategy.HunterStrategy;
import model.Entity;
import model.Animal;
import java.util.List;

public class Eagle extends ApexEntity {

    public Eagle(Vector2D position) {
        super(
            position,
            3.0,         // size: Nhỏ gọn
            60.0,        // maxHp: Máu cực thấp, bị cắn là chết
            200.0,       // maxEnergy: Không cần nhiều năng lượng vì bay
            12.0,        // speed: Tốc độ bay xé gió
            600.0,       // visionRadius: Mắt đại bàng (quét toàn bản đồ)
            70.0,        // strengthWeight: Đe dọa thấp dưới mặt đất
            80.0,        // attackDamage: Móng vuốt sắc bén
            50,          // attackCooldown: Tấn công liên tục
            200          // spAttackCooldown: Cú bổ nhào hồi nhanh
        );
        this.setBrain(new HunterStrategy()); // Dùng constructor mặc định
    }

    @Override
    public void performSpecialAbility(Environment env) {
        Rectangle visionRange = new Rectangle(
            this.getPosition().getX(), 
            this.getPosition().getY(), 
            this.getVisionRadius(), 
            this.getVisionRadius()
        );

        List<Entity> visibleEntities = env.getQuadTree().query(visionRange, null);
        Animal target = null;
        double minDistance = Double.MAX_VALUE;

        for (Entity entity : visibleEntities) {
            if (entity != this && entity.isAlive() && entity instanceof Animal && entity.getClass() != this.getClass()) {
                Animal prey = (Animal) entity;
                
                // Đại bàng chỉ bắt các con vật nhỏ (size <= 5.0) như Thỏ, Cáo, chuột...
                if (prey.getSize() <= 5.0) {
                    double distance = this.getPosition().distanceTo(prey.getPosition());
                    if (distance < minDistance) {
                        minDistance = distance;
                        target = prey;
                    }
                }
            }
        }

        if (target != null) {
            // Đại bàng bổ nhào (dịch chuyển tức thời đến con mồi nhỏ)
            this.getPosition().setX(target.getPosition().getX());
            this.getPosition().setY(target.getPosition().getY());

            // Móng vuốt xuyên thấu: Gây sát thương cực lớn (đủ sức 1-shot thú nhỏ)
            target.setHp(target.getHp() - 200.0);
            System.out.println("Đại bàng bổ nhào từ trên không, quắp trúng " + target.getClass().getSimpleName() + "!");
            
            this.currentSpAttack = this.spAttackCooldown;
        }
    }
}
