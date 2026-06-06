package model.apex;

import model.carnivore.Carnivore;
import model.Vector2D;
import model.environment.Environment;
import model.Animal; // Bổ sung import Animal

public abstract class ApexEntity extends Carnivore {
    // Đổi tên biến: chữ cái đầu viết thường (spAttackCooldown) theo chuẩn Java Naming Convention
    protected int spAttackCooldown;  
    protected int currentSpAttack;   

    public ApexEntity(Vector2D position, double size, double maxHp, double maxEnergy, 
                      double speed, double visionRadius, double strengthWeight, 
                      double attackDamage, int attackCooldown, int spAttackCooldown) { // Bổ sung tham số cấu hình chiêu cuối
                      
        super(position, size, maxHp, maxEnergy, speed, visionRadius, strengthWeight, attackDamage, attackCooldown);
        
        // Khởi tạo chỉ số cho chiêu đặc biệt
        this.spAttackCooldown = spAttackCooldown;
        this.currentSpAttack = 0; // Vừa sinh ra là chiêu cuối sẵn sàng
    }

    // --- SỬA LỖI 2: PHẢI CÓ HÀM UPDATE ĐỂ GIẢM COOLDOWN MỖI FRAME ---
    @Override
    public void update() {
        super.update(); // Gọi update của lớp cha (để giảm cooldown đánh thường và năng lượng)
        
        if (!isAlive()) return;

        if (currentSpAttack > 0) {
            currentSpAttack--;
        }
    }

    // --- SỬA LỖI 1: NẠP CHỒNG (OVERLOAD) HÀM ATTACK ĐỂ NHẬN ENVIRONMENT ---
    // Vì chiêu cuối cần Environment (để quét QuadTree), ta bắt buộc phải truyền env vào đây.
    public void attack(Animal prey, Environment env) {
        // Nếu chiêu đặc biệt đã sẵn sàng
        if (this.currentSpAttack <= 0) {
            this.performSpecialAbility(env);
            
            // Lưu ý: Việc reset cooldown (this.currentSpAttack = this.spAttackCooldown) 
            // nên được đặt ở cuối hàm performSpecialAbility() của Gấu/Hổ,
            // vì lỡ Gấu vồ hụt thì không bị mất chiêu!
        } else {
            // Nếu đang chờ hồi chiêu cuối -> Sử dụng đòn cắn chay của Carnivore
            super.attack(prey);
        }
    }
    
    public abstract void performSpecialAbility(Environment env);
    
    // --- GETTER & SETTER ---
    public int getSpAttackCooldown() {
        return spAttackCooldown;
    }

    public void setSpAttackCooldown(int spAttackCooldown) {
        this.spAttackCooldown = spAttackCooldown;
    }

    public int getCurrentSpAttack() {
        return currentSpAttack;
    }

    public void setCurrentSpAttack(int currentSpAttack) {
        this.currentSpAttack = currentSpAttack;
    }
}