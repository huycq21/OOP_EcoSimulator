package model.strategy;

import model.Animal;
import model.AnimalState;
import model.Entity;
import model.Vector2D;
import model.carnivore.Carnivore;
import model.environment.Environment;
import model.environment.Rectangle; // Chú ý import thêm Rectangle
import java.util.List;

public class ScaredStrategy implements SurvivalStrategy {
    private PassiveStrategy wanderLogic;

    public ScaredStrategy() {
        this.wanderLogic = new PassiveStrategy();
    }

    @Override
    public void execute(Animal prey) {
        // ĐIỂM BỔ SUNG QUAN TRỌNG: 
        // Nếu đang nấp trong bụi rậm thì tuyệt đối nằm im, nín thở!
        if (prey.getCurrentState() == AnimalState.HIDING) {
            prey.getVelocity().setX(0);
            prey.getVelocity().setY(0);
            return; // Dừng não lại ngay tại đây, không suy nghĩ, không quét radar gì nữa!
        }

        // 1. Quét radar xem có thú ăn thịt nào lảng vảng gần đây không
        Entity predator = findNearestPredator(prey);

        if (predator != null) {
            prey.setCurrentState(AnimalState.FLEEING);

            // 2. Toán học Vector (Chạy trốn): Nguồn (Thỏ) - Đích (Sói)
            Vector2D preyPos = prey.getPosition();
            Vector2D predPos = predator.getPosition();

            double dx = preyPos.getX() - predPos.getX();
            double dy = preyPos.getY() - predPos.getY();

            Vector2D fleeVector = new Vector2D(dx, dy);
            fleeVector.normalize();

            // Chạy thục mạng!
            fleeVector.setX(fleeVector.getX() * prey.getSpeed());
            fleeVector.setY(fleeVector.getY() * prey.getSpeed());

            // 3. Gán vận tốc mới
            prey.getVelocity().setX(fleeVector.getX());
            prey.getVelocity().setY(fleeVector.getY());

        } else {
            // 4. Nếu không có thú săn mồi, quay lại đi dạo ăn cỏ bình thường
            wanderLogic.execute(prey);
        }
    }

    // --- ĐÃ ĐƯỢC NÂNG CẤP BẰNG QUADTREE ---
    private Entity findNearestPredator(Animal prey) {
        Entity nearest = null;
        double vision = prey.getVisionRadius(); 
        double minDistance = vision;

        // 1. Tạo vùng radar (hình chữ nhật) bao quanh tầm nhìn
        Rectangle searchRange = new Rectangle(
            prey.getPosition().getX(),
            prey.getPosition().getY(),
            vision * 2, 
            vision * 2
        );

        // 2. Lấy danh sách từ QuadTree siêu nhanh
        List<Entity> nearbyEntities = Environment.getInstance().getQuadTree().query(searchRange, null);

        for (Entity e : nearbyEntities) {
            // Chỉ sợ những kẻ là thú ăn thịt (Carnivore) và đang còn sống
            if (e instanceof Carnivore && e.isAlive()&& prey.getSize() < e.getSize() * 1.5) {
                double dist = prey.getPosition().distanceTo(e.getPosition());
                
                // QuadTree trả về hình chữ nhật, nên ta phải check lại xem 
                // con sói có thực sự nằm trong "vòng tròn" tầm nhìn hay không (dist <= vision)
                if (dist <= vision && dist < minDistance) {
                    minDistance = dist;
                    nearest = e;
                }
            }
        }
        return nearest;
    }
}