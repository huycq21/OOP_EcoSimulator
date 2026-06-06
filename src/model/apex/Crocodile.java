package model.apex;

import model.Vector2D;
import model.carnivore.Carnivore;
import model.environment.Environment;
import model.environment.Rectangle;
import model.strategy.HunterStrategy;
import model.Entity;
import model.Animal;
import model.AnimalState;

import java.util.List;

public class Crocodile extends ApexEntity {

    private double ambushRadius; // Tầm kích hoạt đòn lao tới cắn
    private boolean isSubmerged; // Trạng thái đang lặn

    public Crocodile(Vector2D position) {
        // Thêm tham số cuối cùng (400) làm spAttackCooldown (thời gian hồi chiêu cuối)
        super(position, 6.0, 250.0, 150.0, 1.5, 50.0, 120.0, 150.0, 120, 400);
        this.setBrain(new HunterStrategy());
        
        // Tầm cắn bất ngờ xa gấp 3 lần kích thước cơ thể
        this.ambushRadius = this.getSize() * 3.0;
        this.isSubmerged = false;
        this.setBrain(new HunterStrategy()); // Dùng constructor mặc định
    }

    // --- BƯỚC 1: XỬ LÝ TRẠNG THÁI ẨN MÌNH ---
    @Override
    public void update() {
        super.update(); // Giảm cooldown và thể lực như bình thường
        
        if (!isAlive()) return;

        // Nếu chiêu cuối đã sẵn sàng (Cooldown <= 0) VÀ chưa lặn -> Bắt đầu lặn
        if (this.currentSpAttack <= 0 && !isSubmerged) {
            isSubmerged = true;
            this.setCurrentState(AnimalState.HIDING); // Tàng hình! Các thú khác sẽ đi ngang qua mà không thấy
            System.out.println("Cá sấu đã lặn xuống nước, chuẩn bị phục kích...");
        } 
        // Nếu đang hồi chiêu -> Hiện hình trở lại (không được HIDING nữa)
        else if (this.currentSpAttack > 0 && isSubmerged) {
            isSubmerged = false;
        }
    }

    // --- BƯỚC 2: XỬ LÝ ĐÒN CẮN TẤT SÁT (ONE-SHOT) ---
    @Override
    public void performSpecialAbility(Environment env) {
        // Chỉ kích hoạt khi đang ở trạng thái lặn (chuẩn bị phục kích)
        if (!isSubmerged) return;

        Rectangle ambushRange = new Rectangle(
            this.getPosition().getX(), 
            this.getPosition().getY(), 
            this.ambushRadius, 
            this.ambushRadius
        );

        List<Entity> nearbyEntities = env.getQuadTree().query(ambushRange, null);
        Animal target = null;
        double minDistance = Double.MAX_VALUE;

        // Tìm con mồi xấu số vô tình bước vào tầm ngắm
        for (Entity entity : nearbyEntities) {
            // Cá sấu cạp mọi thứ là Động vật (kể cả Hổ, Gấu hay Sư tử) trừ chính nó
            if (entity != this && entity.isAlive() && entity instanceof Animal && entity.getClass() != this.getClass()) {
                Animal prey = (Animal) entity;
                double distance = this.getPosition().distanceTo(prey.getPosition());

                if (distance <= this.ambushRadius && distance < minDistance) {
                    minDistance = distance;
                    target = prey;
                }
            }
        }

        // KHI CON MỒI ĐÃ VÀO TẦM CẮN
        if (target != null) {
            // 1. Phá vỡ trạng thái lặn/tàng hình
            this.isSubmerged = false;
            this.setCurrentState(AnimalState.CHASING);

            // 2. Lao thật nhanh đến mục tiêu (áp sát tức thời)
            this.getPosition().setX(target.getPosition().getX());
            this.getPosition().setY(target.getPosition().getY());

            // 3. Cạp một phát chết luôn (Gây sát thương bằng đúng maxHp hoặc 9999)
            double fatalDamage = 300;
            target.setHp(target.getHp() - fatalDamage);

            System.out.println("RÀÀO!! Cá sấu lao lên khỏi mặt nước cạp nát " + target.getClass().getSimpleName() + "!");

            // 4. Bắt đầu thời gian chờ hồi chiêu lặn tiếp theo
            this.currentSpAttack = this.spAttackCooldown;
        }
    }
}