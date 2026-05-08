package model.carnivore;

import model.Entity;
import model.Vector2D;
import model.strategy.HunterStrategy;
import model.environment.Environment;

public class Cheetah extends Carnivore {

    private double baseSpeed = 8.5;
    private double sprintSpeed = 16.0; // Tốc độ hủy diệt khi bứt tốc
    private boolean isSprinting = false;
    HunterStrategy hunter = new HunterStrategy();

    public Cheetah(Vector2D position) {
        // base stats như bạn đã thiết kế
        super(position, 4.5, 80, 80, 8.5, 90.0, 60.0, 45.0, 30);
        this.setBrain(hunter);
    }

    // Giả sử các Entity của bạn có hàm update() được gọi mỗi frame
    @Override
    public void update() {
        super.update(); // Gọi update của lớp cha để thực hiện logic di chuyển/săn mồi cơ bản

        if (!isAlive()) return;

        Entity target = hunter.findNearestPrey(this);
        // Logic bứt tốc: Nếu đang nhắm mục tiêu (đang đi săn) và năng lượng còn trên 30%
        if (target != null && this.getEnergy() > 25.0) {
            if (!isSprinting) {
                isSprinting = true;
                this.setSpeed(sprintSpeed);
                System.out.println("Báo săn bắt đầu bứt tốc! Speed: " + sprintSpeed);
            }
            
            // Trừ năng lượng cực mạnh khi đang bứt tốc (mỗi frame trừ nhiều hơn bình thường)
            this.setEnergy(this.getEnergy() - 1.5); 
            
        } else {
            // Khi mất mục tiêu HOẶC cạn kiệt năng lượng -> Rơi vào trạng thái kiệt sức
            if (isSprinting) {
                isSprinting = false;
                System.out.println("Báo săn kiệt sức, giảm tốc độ!");
            }
            
            // Tốc độ tụt thảm hại nếu năng lượng quá thấp (đặc trưng của báo săn sau khi chạy)
            if (this.getEnergy() < 10.0) {
                this.setSpeed(baseSpeed * 0.4); // Chỉ còn lết đi (tốc độ ~ 3.4)
            } else {
                this.setSpeed(baseSpeed); // Hồi phục lại tốc độ đi bộ thông thường
            }
        }
    }
}