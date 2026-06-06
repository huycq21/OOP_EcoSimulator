package model.carnivore;

import java.util.List;
import model.*;
import model.environment.Environment;
import model.environment.Rectangle;
import model.herbivore.Herbivore;
import java.util.ArrayList;

public abstract class Carnivore extends Animal {
    protected double strengthWeight;    // Mức độ đe dọa
    protected double attackDamage;      // Lực sát thương mỗi lần cắn

    protected int attackCooldown;       // Thời gian chờ giữa các lần tấn công (số tick)
    protected int currentCooldownTimer; // Bộ đếm thời gian chờ
    protected double preyDetectionRadius;
    protected List<Class<? extends Animal>> preyTypes;
    protected double strikeRadius;      // khoảng cách bắt đầu bộc phát tốc độ sau khi rón rén tiếp cận con mồi
    protected double packMultiplier = 1.0; // Số lượng trong đàn, mặc định là một( khi đi cùng đàn thì mới cộng dồn)

    public Carnivore(Vector2D position, double size, double maxHp, double maxEnergy, 
                     double speed, double visionRadius, double strengthWeight, 
                     double attackDamage, int attackCooldown) {
                    
        super(position, size, maxHp, maxEnergy, speed, visionRadius);
        this.strengthWeight = strengthWeight;
        
        this.attackDamage = attackDamage;
        this.attackCooldown = attackCooldown;
        this.currentCooldownTimer = 0; // Sẵn sàng cắn ngay lần đầu chạm mặt
        this.preyDetectionRadius = visionRadius;
        this.strikeRadius = this.preyDetectionRadius * 0.5;
        this.preyTypes = new ArrayList<>();
    }

    public boolean isStarving() {
        return this.getEnergy() < (this.getMaxEnergy() * 0.25);
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
        if (preyTypes.isEmpty()) return true;

        for (Class<? extends Animal> preyType : preyTypes) {
            if (preyType.isInstance(prey)) {
                return true;
            }
        }
        return false;
    }

    protected void addPreyType(Class<? extends Animal> preyType) {
        preyTypes.add(preyType);
    }
    
    public List<Class<? extends Animal>> getPreyType() {
        return preyTypes;
    }

    public void setAttackDamage(double attackDamage) {
        this.attackDamage = attackDamage;
    }

    public double getPreyDetectionRadius() {
        return preyDetectionRadius;
    }

    public void setPreyDetectionRadius(double preyDetectionRadius) {
        this.preyDetectionRadius = preyDetectionRadius;
    }

    public double getAttackDamage() { 
        return attackDamage; 
    }

    public void setPackMultiplier(double multiplier) {
        this.packMultiplier = multiplier;
    }

    // Trong class Carnivore.java
    public double getStrengthWeight() {
        // Mức độ đe dọa thực tế = Khí chất gốc x Hào quang bầy đàn
        return this.strengthWeight * this.packMultiplier; 
    }

    public void setStrikeRadius(double strikeRadius) {
        this.strikeRadius = strikeRadius;
    }
    
    public double getStrikeRadius() {
        return strikeRadius;
    }
}
