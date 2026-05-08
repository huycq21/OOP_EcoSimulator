package model.apex;

import model.Vector2D;
import model.environment.Environment;
import model.environment.Rectangle;
import model.Entity;
import model.Animal;
import model.carnivore.Carnivore;
import java.util.List;

public class Human extends ApexEntity {

    private double gunRange;

    public Human(Vector2D position) {
        super(
            position,
            5.0,         // size: Bằng con người bình thường
            100.0,       // maxHp: Thể chất yếu, máu bằng con Sói
            250.0,       // maxEnergy: Thể lực kém
            4.0,         // speed: Đi bộ chậm chạp
            300.0,       // visionRadius: Ống nhòm giúp nhìn rất xa
            2000.0,      // strengthWeight: SIÊU ĐE DỌA. Mọi con vật gặp người đều bỏ chạy!
            30.0,        // attackDamage: Đánh tay bo/dao găm cùi bắp
            60,          // attackCooldown: Hồi đánh tay
            180          // spAttackCooldown: Hồi đạn nhanh (180 tick)
        );
        // Tầm bắn của súng
        this.gunRange = 250.0;
        
    }

    @Override
    public void performSpecialAbility(Environment env) {
        Rectangle aimRange = new Rectangle(
            this.getPosition().getX(), 
            this.getPosition().getY(), 
            this.gunRange, 
            this.gunRange
        );

        List<Entity> visibleEntities = env.getQuadTree().query(aimRange, null);
        Animal bestTarget = null;
        double maxThreat = -1; // Biến để tìm con vật nguy hiểm/đắt tiền nhất

        for (Entity entity : visibleEntities) {
            if (entity != this && entity.isAlive() && entity instanceof Animal && entity.getClass() != this.getClass()) {
                Animal target = (Animal) entity;
                double distance = this.getPosition().distanceTo(target.getPosition());

                if (distance <= this.gunRange) {
                    double currentThreat = 0;
                    // Thợ săn ưu tiên bắn thú ăn thịt (Carnivore/Apex) vì da chúng có giá trị cao
                    if (target instanceof Carnivore) {
                        currentThreat = ((Carnivore) target).getStrengthWeight();
                    } else {
                        currentThreat = target.getSize(); // Ăn cỏ thì ưu tiên bắn con to (như Voi)
                    }

                    if (currentThreat > maxThreat) {
                        maxThreat = currentThreat;
                        bestTarget = target;
                    }
                }
            }
        }

        if (bestTarget != null) {
            // Súng bắn tầm xa -> KHÔNG CẦN DI CHUYỂN, gây sát thương cực mạnh từ xa
            double gunDamage = 500.0; // Sức mạnh của súng (Một phát chết Gấu)
            bestTarget.setHp(bestTarget.getHp() - gunDamage);
            
            System.out.println("ĐOÀNG! Thợ săn dùng súng bắn tỉa trúng " + bestTarget.getClass().getSimpleName() 
                               + " (-" + gunDamage + " HP)! Phá vỡ cân bằng sinh thái!");
            
            this.currentSpAttack = this.spAttackCooldown;
        }
    }
}