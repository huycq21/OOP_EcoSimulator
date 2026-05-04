package model.apex;

import model.carnivore.Carnivore;
import model.Vector2D;
import model.environment.Environment;

public abstract class ApexEntity extends Carnivore {

    public ApexEntity(Vector2D position, double size, double maxHp, double maxEnergy, 
                      double speed, double visionRadius, double strengthWeight, 
                      double attackDamage, int attackCooldown) {
                      
        super(position, size, maxHp, maxEnergy, speed, visionRadius, strengthWeight, attackDamage, attackCooldown);
    }

    // Bắt buộc mọi loài Apex phải có kỹ năng đặc biệt
    // Truyền Environment vào để con vật có thể tương tác với cả bản đồ (ví dụ gọi QuadTree)
    public abstract void performSpecialAbility(Environment env);
}