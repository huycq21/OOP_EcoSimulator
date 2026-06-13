package model.strategy;

import model.Animal;
import model.AnimalState;
import model.Vector2D;
import model.environment.Environment;
import java.util.Random;
import controller.SimulationConstant;

public class PassiveStrategy implements SurvivalStrategy {
    private final Random random;

    public PassiveStrategy() {
        this.random = new Random();
    }

    @Override
    public void execute(Animal animal) {
        // Đặt trạng thái hiển thị của thực thể thành ĐANG ĐI DẠO
        animal.setCurrentState(AnimalState.WANDERING);

        // --- 1. XỬ LÝ TRẠNG THÁI ĐỨNG NGHỈ NGƠI ---
        int restLeft = animal.getPassiveRestTimer();
        if (restLeft > 0) {
            animal.setPassiveRestTimer(restLeft - 1);
            // Khóa phanh đứng im tại chỗ
            animal.getVelocity().setX(0);
            animal.getVelocity().setY(0);
            return;
        }

        // --- 2. KIỂM TRA CHU KỲ ĐỔI HƯỚNG DI CHUYỂN MỚI ---
        int timer = animal.getWanderTimer();
        if (timer <= 0 || animal.getWanderDirection() == null) {
            chooseNewWanderState(animal);
            
            // Nếu bốc thăm ngẫu nhiên trúng vào ô nghỉ ngơi -> Lập tức đứng im
            if (animal.getPassiveRestTimer() > 0) {
                animal.getVelocity().setX(0);
                animal.getVelocity().setY(0);
                return;
            }
        }

        // --- 3. THỰC THI DI CHUYỂN LANG THANG (THONG THẢ) ---
        Vector2D direction = animal.getWanderDirection();
        
        // Đi chậm bằng 20% tốc độ tối đa để tạo hiệu ứng thong dong dạo chơi
        double pace = animal.getSpeed() * 0.2; 
        
        animal.getVelocity().setX(direction.getX() * pace);
        animal.getVelocity().setY(direction.getY() * pace);
        
        // Ép quay đầu nếu thực thể tiến sát đường biên bản đồ
        steerAwayFromMapEdges(animal);
        
        // Giảm thời gian hiệu lực của hướng đi hiện tại xuống 1 Tick
        animal.setWanderTimer(animal.getWanderTimer() - 1);
    }

    /**
     * Bốc thăm ngẫu nhiên hành vi tiếp theo cho từng cá thể động vật độc lập
     */
    private void chooseNewWanderState(Animal animal) {
        if (random.nextDouble() < 0.25) {
            // 25% cơ hội quyết định dừng chân đứng nghỉ ngơi (từ 30 đến 119 ticks)
            animal.setPassiveRestTimer(30 + random.nextInt(90));
            animal.setWanderTimer(0);
        } else {
            // 75% cơ hội chọn một góc ngẫu nhiên trong không gian đường tròn Toán học (2 * PI)
            double angle = random.nextDouble() * 2 * Math.PI;
            Vector2D newDirection = new Vector2D(Math.cos(angle), Math.sin(angle));
            
            animal.setWanderDirection(newDirection);
            // Thời gian duy trì hướng đi dạo này (từ 45 đến 194 ticks)
            animal.setWanderTimer(45 + random.nextInt(150));
        }
    }

    /**
     * Ngăn chặn động vật đi lọt ra ngoài rìa bản đồ mô phỏng
     */
    private void steerAwayFromMapEdges(Animal animal) {
        Environment env = Environment.getInstance();
        if (env == null) return;

        // Kế thừa hằng số biên an toàn tập trung từ hệ thống của bản cũ
        double margin = SimulationConstant.EDGE_MARGIN; 
        double x = animal.getPosition().getX();
        double y = animal.getPosition().getY();
        Vector2D velocity = animal.getVelocity();

        // Kiểm tra và đảo hướng vận tốc X nếu chạm biên trái / phải
        if (x < margin) {
            velocity.setX(Math.abs(velocity.getX())); // Chắc chắn hướng sang phải
        } else if (x > env.getWidth() - margin) {
            velocity.setX(-Math.abs(velocity.getX())); // Chắc chắn hướng sang trái
        }

        // Kiểm tra và đảo hướng vận tốc Y nếu chạm biên trên / dưới
        if (y < margin) {
            velocity.setY(Math.abs(velocity.getY())); // Chắc chắn hướng xuống dưới
        } else if (y > env.getHeight() - margin) {
            velocity.setY(-Math.abs(velocity.getY())); // Chắc chắn hướng lên trên
        }
    }
}