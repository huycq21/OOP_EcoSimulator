package model.apex;

import model.Vector2D;
import model.strategy.*;
import model.AnimalState;
import model.herbivore.*;
import model.carnivore.*;
import model.domestic.*;

public class Eagle extends ApexEntity {

    private boolean isDiving;
    private int diveTimer;
    private double baseSpeed;
    private double baseDamage;

    public Eagle(Vector2D position) {
        super(position, 3.0, 60.0, 200.0, 8.0, 600.0, 70.0, 80.0, 50, 200);
        
        this.baseSpeed = this.getSpeed();
        this.baseDamage = this.getAttackDamage();
        this.isDiving = false;
        this.diveTimer = 0;

        // --- Ý TƯỞNG THIÊN TÀI CỦA BẠN: KHÔNG CẦN RÓN RÉN ---
        // Thấy mồi là lao vào rượt luôn!
        this.setStrikeRadius(this.getPreyDetectionRadius());

        this.addPreyType(Rabbit.class);
        this.addPreyType(BlackGrouse.class);
        this.addPreyType(Chicken.class);
        this.addPreyType(Fox.class);

        SurvivalStrategy passive = new PassiveStrategy();
        SurvivalStrategy hunter = new HunterStrategy(passive);
        SurvivalStrategy scared = new ScaredStrategy(hunter); 
        SurvivalStrategy scavenger = new ScavengerStrategy(scared);
        
        this.setBrain(scavenger); 
    }

    @Override
    public void update() {
        super.update(); // Xử lý giảm cooldown cơ bản

        if (!isAlive()) return;

        // 1. NẾU ĐANG TRONG TRẠNG THÁI BỔ NHÀO (Đã được buff)
        if (isDiving) {
            diveTimer--;
            
            // Hết đà bổ nhào hoặc lỡ chuyển sang state khác (ví dụ: bị dọa sợ FLEEING)
            if (diveTimer <= 0 || this.getCurrentState() != AnimalState.CHASING) {
                isDiving = false;
                this.setSpeed(baseSpeed);
                this.setAttackDamage(baseDamage);
                System.out.println("Đại bàng kết thúc bổ nhào, trở lại chỉ số bình thường.");
            }
        } 
        // 2. NẾU CHƯA BỔ NHÀO & CHIÊU ĐÃ HỒI & NÃO ĐANG BẬT MODE RƯỢT ĐUỔI
        else {
            if (this.getCurrentState() == AnimalState.CHASING && this.currentSpAttack <= 0) {
                
                // Kích hoạt bứt tốc!
                this.isDiving = true;
                this.diveTimer = 90; // Kéo dài 1.5 giây

                this.setSpeed(baseSpeed * 1.5); 
                this.setAttackDamage(baseDamage * 2.0);
                
                // Reset cooldown chiêu cuối
                this.currentSpAttack = this.spAttackCooldown;
                
                System.out.println("ÉÉÉ!! Đại bàng khóa mục tiêu! Tốc độ xé gió kích hoạt!");
            }
        }
    }

    // Vì chúng ta xài nội tại buff theo State, hàm này có thể để trống 
    // (hoặc nếu Interface bắt buộc đè thì cứ để nó rỗng)
    @Override
    public void performSpecialAbility(model.environment.Environment env) {
        // Kỹ năng của Đại bàng là Nội tại (Passive Trigger) dựa trên State CHASING.
        // Không cần làm gì ở đây cả!
    }
}