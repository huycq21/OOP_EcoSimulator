package model.strategy;

import model.Animal;
import model.AnimalState;
import model.Entity;
import model.Vector2D;
import model.herbivore.Herbivore;
import model.environment.Environment; // Cần import cái này để lấy danh sách sinh vật
import java.util.List;

public class HunterStrategy implements SurvivalStrategy {
    // Tái sử dụng lại logic đi dạo nếu không có con mồi nào ở gần
    private PassiveStrategy wanderLogic;

    public HunterStrategy() {
        this.wanderLogic = new PassiveStrategy();
    }

    @Override
    public void execute(Animal hunter) {
        // 1. Quét tìm con mồi gần nhất
        Entity target = findNearestPrey(hunter);

        if (target != null) {
            // Đã thấy mồi! Đổi nhãn trạng thái thành ĐANG SĂN MỒI
            hunter.setCurrentState(AnimalState.CHASING);

            // 2. Toán học Vector: Tính toán hướng lao tới con mồi
            Vector2D hunterPos = hunter.getPosition();
            Vector2D targetPos = target.getPosition();

            // Lấy tọa độ Đích trừ tọa độ Nguồn sẽ ra Vector hướng
            double dx = targetPos.getX() - hunterPos.getX();
            double dy = targetPos.getY() - hunterPos.getY();

            Vector2D chaseVector = new Vector2D(dx, dy);

            // Chuẩn hóa vector (rút ngắn độ dài vector về 1) 
            // để đảm bảo con vật không dịch chuyển tức thời đến chỗ con mồi
            chaseVector.normalize();

            // Nhân vector hướng (độ dài 1) với tốc độ của kẻ đi săn
            // Đi săn thì chạy bằng 100% tốc độ tối đa (speed)
            chaseVector.setX(chaseVector.getX() * hunter.getSpeed());
            chaseVector.setY(chaseVector.getY() * hunter.getSpeed());

            // 3. Cập nhật vận tốc mới để lớp Animal mang đi di chuyển
            hunter.getVelocity().setX(chaseVector.getX());
            hunter.getVelocity().setY(chaseVector.getY());

        } else {
            // 4. Nếu không thấy ai, gọi bộ não đi dạo ra chạy hộ!
            wanderLogic.execute(hunter);
        }
    }

    // --- HÀM HỖ TRỢ: DÒ RADAR TÌM CON MỒI ---
    private Entity findNearestPrey(Animal hunter) {
        Entity nearest = null;
        double minDistance = hunter.getVisionRadius(); // Chỉ quét trong bán kính tầm nhìn

        // Lấy danh sách tất cả sinh vật trên bản đồ. 
        // LƯU Ý: Chỗ này yêu cầu class Environment của bạn phải có pattern Singleton (getInstance())
        // Hoặc sau này bạn sẽ thay bằng cấu trúc QuadTree mà ta đã bàn!
        List<Entity> allEntities = Environment.getInstance().getEntities();

        for (Entity e : allEntities) {
            // Nếu thực thể đó là Thú ăn cỏ VÀ đang sống VÀ không phải đang trốn
            if (e instanceof Herbivore && e.isAlive() && ((Herbivore) e).getCurrentState() != AnimalState.HIDING) {
                
                // Tính khoảng cách
                double dist = hunter.getPosition().distanceTo(e.getPosition());
                
                // Nếu nằm trong tầm nhìn và gần hơn mục tiêu trước đó
                if (dist < minDistance) {
                    minDistance = dist;
                    nearest = e;
                }
            }
        }
        return nearest;
    }
}