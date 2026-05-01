package model.strategy;

import model.Animal;
import model.AnimalState;
import model.Vector2D;
import java.util.Random;

public class PassiveStrategy implements SurvivalStrategy {
    private Random random;

    public PassiveStrategy() {
        this.random = new Random();
    }

    @Override
    public void execute(Animal animal) {
        // 1. Đặt nhãn trạng thái là đi lang thang
        animal.setCurrentState(AnimalState.WANDERING);

        // 2. Logic đi dạo ngẫu nhiên (chỉ đổi hướng với xác suất 5% mỗi khung hình)
        if (random.nextDouble() < 0.05) {
            double angle = random.nextDouble() * 2 * Math.PI; // Random góc 360 độ
            
            // Lấy vận tốc hiện tại của con vật ra để chỉnh sửa
            Vector2D velocity = animal.getVelocity();
            
            // Đi dạo nên chỉ đi với 50% tốc độ tối đa (speed * 0.5)
            velocity.setX(Math.cos(angle) * animal.getSpeed() * 0.5);
            velocity.setY(Math.sin(angle) * animal.getSpeed() * 0.5);
        }
    }
}