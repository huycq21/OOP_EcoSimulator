package model.apex;

import model.Vector2D;
import model.environment.Environment;
import model.environment.Rectangle;
import model.strategy.FlockingStrategy;
import model.strategy.HunterStrategy;
import model.Entity;
import model.Animal;
import java.util.List;

public class Lion extends ApexEntity {

    private double roarRadius;

    public Lion(Vector2D position) {
        super(
            position,
            11.0,        // size: Nhỏ hơn Gấu một chút, to hơn Hổ
            350.0,       // maxHp: Khá trâu bò
            450.0,       // maxEnergy: Năng lượng dồi dào
            5.8,         // speed: Nhanh, nhưng chậm hơn Hổ (6.0)
            110.0,       // visionRadius: Tầm nhìn xa trên thảo nguyên
            130.0,       // strengthWeight: Uy lực cao, đi theo bầy sẽ cộng dồn lên cực lớn
            90.0,        // attackDamage: Lực cắn mạnh
            75,          // attackCooldown: Hồi đòn khá nhanh
            350          // spAttackCooldown: Hồi chiêu tiếng gầm 350 tick
        );
        // Tiếng gầm vang xa gấp 3 lần tầm nhìn bình thường
        this.roarRadius = this.getVisionRadius() * 3.0; 
        // Sư tử / Linh cẩu constructor:
        this.setBrain(new HunterStrategy(new FlockingStrategy()));
    }

    @Override
    public void performSpecialAbility(Environment env) {
        Rectangle roarRange = new Rectangle(
            this.getPosition().getX(), 
            this.getPosition().getY(), 
            this.roarRadius, 
            this.roarRadius
        );

        List<Entity> nearbyEntities = env.getQuadTree().query(roarRange, null);
        boolean roared = false;

        for (Entity entity : nearbyEntities) {
            // Làm khiếp sợ tất cả động vật yếu hơn xung quanh (không áp dụng với Apex khác để tránh lỗi logic)
            if (entity != this && entity.isAlive() && entity instanceof Animal && !(entity instanceof ApexEntity)) {
                Animal prey = (Animal) entity;
                double distance = this.getPosition().distanceTo(prey.getPosition());

                if (distance <= this.roarRadius) {
                    // Tụt sạch năng lượng của con mồi về 0 -> Bọn chúng sẽ không thể bứt tốc bỏ chạy
                    prey.setEnergy(0);
                    roared = true;
                    System.out.println("Sư tử gầm lên! " + prey.getClass().getSimpleName() + " sợ hãi rụng rời, mất sạch năng lượng!");
                }
            }
        }

        if (roared) {
            this.currentSpAttack = this.spAttackCooldown;
        }
    }
}