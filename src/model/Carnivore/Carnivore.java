package model.carnivore;

import java.util.List;

import model.*;
import model.environment.Environment;
import model.environment.Rectangle;


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
    public boolean isStarving() {
        return this.getEnergy() < (this.getMaxEnergy() * 0.25);
    }

    // --- TÍNH NĂNG MỚI: TÍNH SỨC MẠNH ÁP ĐẢO (CỘNG DỒN BẦY ĐÀN) ---
    public double getEffectiveStrength(Environment env) {
        double totalStrength = this.strengthWeight;

        // Chỉ những loài săn mồi bầy đàn mới được cộng dồn đe dọa (ví dụ Sói, Linh Cẩu)
        // Lưu ý: Hổ hay Gấu là động vật độc lập, không cộng dồn dù có đứng cạnh nhau
        if (this instanceof Wolf || this instanceof Hyena) {
            
            // Quét các đồng loại xung quanh trong bán kính tầm nhìn
            Rectangle searchRange = new Rectangle(
                this.getPosition().getX(), 
                this.getPosition().getY(), 
                this.getVisionRadius(), 
                this.getVisionRadius()
            );

            List<Entity> nearby = env.getQuadTree().query(searchRange, null);

            for (Entity e : nearby) {
                // Nếu là đồng loại (cùng Class), đang sống, và không phải chính mình
                if (e != this && e.getClass() == this.getClass() && e.isAlive()) {
                    // Cộng hưởng sức mạnh! (Giả sử mỗi đồng minh đóng góp 80% sức mạnh đe dọa)
                    totalStrength += ((Carnivore) e).getStrengthWeight() * 0.8;
                }
            }
        }
        return totalStrength;
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

    public double getStrengthWeight() {
        return strengthWeight;
    }
    public double getAttackDamage() {
        return attackDamage;
    }
    public int getAttackCooldown() {
        return attackCooldown;
    }
    public void setAttackCooldown(int attackCooldown) {
        this.attackCooldown = attackCooldown;
    }
        public int getCurrentCooldownTimer() {
        return currentCooldownTimer;
    }
    public void setCurrentCooldownTimer(int currentCooldownTimer) {
        this.currentCooldownTimer = currentCooldownTimer;
    }
}