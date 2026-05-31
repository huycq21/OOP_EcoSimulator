package model.carnivore;

import model.Vector2D;
import model.AnimalState;
import model.strategy.*;
import model.herbivore.*;

public class Cheetah extends Carnivore {

    private double baseSpeed = 8.5;
    private double sprintSpeed = 16.0; 
    private boolean isSprinting = false;

    public Cheetah(Vector2D position) {
        super(position, 4.5, 80, 100, 8.5, 90.0, 60.0, 45.0, 30);
        
        this.addPreyType(Rabbit.class);
        this.addPreyType(Deer.class);
        this.addPreyType(Goat.class);        // Dê
        this.addPreyType(BlackGrouse.class);

        // --- LẮP NÃO CHUẨN SINH THÁI ---
        SurvivalStrategy passive = new PassiveStrategy(); 
        SurvivalStrategy hunter = new HunterStrategy(passive); 
        
        // Nhét bản năng ăn xác lên trên Đi săn. 
        // Báo sẽ ưu tiên ăn xác (nếu an toàn). Không có xác mới đi săn mồi sống.
        SurvivalStrategy scavenger = new ScavengerStrategy(hunter);
        
        // Vỏ bọc ngoài cùng: Sợ hãi. 
        // Báo sẽ bỏ chạy nếu thấy KẺ CÓ THỂ ĂN THỊT NÓ (Hổ, Sư tử). Cáo thì nó bơ luôn!
        SurvivalStrategy scared = new ScaredStrategy(scavenger); 
        
        this.setBrain(scared);
    }

    @Override
    public void update() {
        super.update(); 

        if (!isAlive()) return;

        boolean isChasing = (this.getCurrentState() == AnimalState.CHASING);

        if (isChasing && this.getEnergy() > 25.0) {
            
            if (!isSprinting) {
                isSprinting = true;
                this.setSpeed(sprintSpeed); 
            }
            
            // CÂN BẰNG LẠI THỂ LỰC:
            // Game chạy 60 FPS. Trừ 0.25 mỗi frame = 1 giây mất 15 năng lượng.
            // Báo có 100 năng lượng -> Sẽ bứt tốc được khoảng 6.5 giây trước khi kiệt sức!
            this.setEnergy(this.getEnergy() - 0.25); 
            
        } else {
            if (isSprinting) {
                isSprinting = false;
            }
            
            if (this.getEnergy() < 10.0) {
                this.setSpeed(baseSpeed * 0.4); 
            } else {
                this.setSpeed(baseSpeed); 
            }
        }
    }
}