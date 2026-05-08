package model.strategy;

import model.Animal;
import model.AnimalState;
import model.Carcass;
import model.Entity;
import model.Vector2D;
import model.carnivore.Carnivore;
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
        boolean hungry = hunter.getEnergy() < hunter.getMaxEnergy() * 0.65;
        Entity target = hungry ? findNearestPrey(hunter) : findVeryClosePrey(hunter);
        if (target == null && hungry) {
            target = findNearestCarcass(hunter);
        }

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
        double minDistance = getPreyDetectionRadius(hunter);

        // Lấy danh sách tất cả sinh vật trên bản đồ. 
        // LƯU Ý: Chỗ này yêu cầu class Environment của bạn phải có pattern Singleton (getInstance())
        // Hoặc sau này bạn sẽ thay bằng cấu trúc QuadTree mà ta đã bàn!
        List<Entity> allEntities = Environment.getInstance().getEntities();

        for (Entity e : allEntities) {
            // Nếu thực thể đó là Thú ăn cỏ VÀ đang sống VÀ không phải đang trốn
            if (isValidPrey(hunter, e)) {
                
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

    private Entity findVeryClosePrey(Animal hunter) {
        Entity nearest = null;
        double minDistance = getPreyDetectionRadius(hunter) * 0.35;
        List<Entity> allEntities = Environment.getInstance().getEntities();

        for (Entity e : allEntities) {
            if (isValidPrey(hunter, e)) {
                double dist = hunter.getPosition().distanceTo(e.getPosition());
                if (dist < minDistance) {
                    minDistance = dist;
                    nearest = e;
                }
            }
        }
        return nearest;
    }

    private boolean isValidPrey(Animal hunter, Entity entity) {
        if (!(entity instanceof Herbivore) || !entity.isAlive()) return false;

        Herbivore prey = (Herbivore) entity;
        if (prey.getCurrentState() == AnimalState.HIDING) return false;

        if (hunter instanceof Carnivore) {
            return ((Carnivore) hunter).canAttack(prey);
        }

        return true;
    }

    private Entity findNearestCarcass(Animal hunter) {
        Entity nearest = null;
        double minDistance = getPreyDetectionRadius(hunter) * 1.5;
        List<Entity> allEntities = Environment.getInstance().getEntities();

        for (Entity e : allEntities) {
            if (e instanceof Carcass && e.isAlive()) {
                double dist = hunter.getPosition().distanceTo(e.getPosition());
                if (dist < minDistance) {
                    minDistance = dist;
                    nearest = e;
                }
            }
        }
        return nearest;
    }

    private double getPreyDetectionRadius(Animal hunter) {
        if (hunter instanceof Carnivore) {
            return ((Carnivore) hunter).getPreyDetectionRadius();
        }
        return hunter.getVisionRadius();
    }
}
