package model.carnivore;

import model.Animal;
import model.Vector2D;
import model.*;
import model.herbivore.Herbivore;

import java.util.ArrayList;
import java.util.List;


public abstract class Carnivore extends Animal {
    protected double strengthWeight;    // Mức độ đe dọa
    protected double attackDamage;      // Lực sát thương mỗi lần cắn
    protected int attackCooldown;       // Thời gian chờ giữa các lần tấn công (số tick)
    protected int currentCooldownTimer; // Bộ đếm thời gian chờ
    protected double preyDetectionRadius;
    protected List<Class<? extends Herbivore>> preyTypes;

    public Carnivore(Vector2D position, double size, double maxHp, double maxEnergy, 
                     double speed, double visionRadius, double strengthWeight, 
                     double attackDamage, int attackCooldown) {
        
        super(position, size, maxHp, maxEnergy, speed, visionRadius);
        this.strengthWeight = strengthWeight;
        
        this.attackDamage = attackDamage;
        this.attackCooldown = attackCooldown;
        this.currentCooldownTimer = 0; // Sẵn sàng cắn ngay lần đầu chạm mặt
        this.preyDetectionRadius = visionRadius;
        this.preyTypes = new ArrayList<>();
    }
    
    @Override
    public void update() {
        super.update(); // Gọi Animal update để di chuyển và giảm thể lực
        
        if (!isAlive) return;

        // Giảm thời gian chờ tấn công theo mỗi khung hình
        if (currentCooldownTimer > 0) {
            currentCooldownTimer--;
        }
    }

    // Hàm thực hiện việc cắn con mồi
    public void attack(Animal prey) {
        if (currentCooldownTimer == 0) {
            this.startAttackState();

            // Trừ máu con mồi
            prey.receiveDamage(this.attackDamage);
            
            // Reset lại thời gian chờ (Ví dụ: 30 tick = nửa giây)
            this.currentCooldownTimer = this.attackCooldown;
            
            // Có thể in ra Console để debug xem chúng nó cắn nhau thế nào
            // System.out.println(this.getClass().getSimpleName() + " cắn " + prey.getClass().getSimpleName() + " gây " + this.attackDamage + " sát thương!");
        }
    }

    public boolean canAttack(Animal prey) {
        if (!(prey instanceof Herbivore)) return false;
        if (preyTypes.isEmpty()) return true;

        for (Class<? extends Herbivore> preyType : preyTypes) {
            if (preyType.isInstance(prey)) {
                return true;
            }
        }
        return false;
    }

    protected void addPreyType(Class<? extends Herbivore> preyType) {
        preyTypes.add(preyType);
    }

    public double getPreyDetectionRadius() {
        return preyDetectionRadius;
    }

    public void setPreyDetectionRadius(double preyDetectionRadius) {
        this.preyDetectionRadius = preyDetectionRadius;
    }

    public double getStrengthWeight() { return strengthWeight; }
    public double getAttackDamage() { return attackDamage; }
}
