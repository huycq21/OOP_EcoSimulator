package model.carnivore;

import model.Animal;
import model.Vector2D;
import model.*;


public abstract class Carnivore extends Animal {
    protected double strengthWeight;    // Mức độ đe dọa
    protected double attackDamage;      // Lực sát thương mỗi lần cắn
    protected int attackCooldown;       // Thời gian chờ giữa các lần tấn công (số tick)
    protected int currentCooldownTimer; // Bộ đếm thời gian chờ

    public Carnivore(Vector2D position, double size, double maxHp, double maxEnergy, 
                     double speed, double visionRadius, double strengthWeight, 
                     double attackDamage, int attackCooldown) {
        
        super(position, size, maxHp, maxEnergy, speed, visionRadius);
        this.strengthWeight = strengthWeight;
        
        this.attackDamage = attackDamage;
        this.attackCooldown = attackCooldown;
        this.currentCooldownTimer = 0; // Sẵn sàng cắn ngay lần đầu chạm mặt
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
            // Trừ máu con mồi
            prey.setHp(prey.getHp() - this.attackDamage);
            
            // Reset lại thời gian chờ (Ví dụ: 30 tick = nửa giây)
            this.currentCooldownTimer = this.attackCooldown;
            
            // Có thể in ra Console để debug xem chúng nó cắn nhau thế nào
            // System.out.println(this.getClass().getSimpleName() + " cắn " + prey.getClass().getSimpleName() + " gây " + this.attackDamage + " sát thương!");
        }
    }

    public double getStrengthWeight() { return strengthWeight; }
    public double getAttackDamage() { return attackDamage; }
}