package model.strategy;

import model.Animal;
import model.AnimalState;
import model.Entity;
import model.Vector2D;
import model.carnivore.Carnivore;
import model.environment.Environment;
import model.environment.Rectangle;
import model.plant.Grass;

import java.util.List;

public class AggressiveStrategy implements SurvivalStrategy {
    private final SurvivalStrategy nextLogic; // Lớp xử lý tầng dưới (Ví dụ: ScaredStrategy hoặc ForagingStrategy)
    private final double courageThreshold;     // Ngưỡng dũng cảm (Sức mạnh kẻ địch dưới tầm này mới dám húc)

    public AggressiveStrategy(SurvivalStrategy nextLogic, double courageThreshold) {
        this.nextLogic = nextLogic;
        this.courageThreshold = courageThreshold;
    }

    @Override
    public void execute(Animal animal) {
        // --- ƯU TIÊN 1: PHÒNG THỦ CHỦ ĐỘNG (HÚC KẺ THÙ TỪ BẢN MỚI) ---
        Entity threat = findNearestThreat(animal);

        if (threat != null) {
            Carnivore predator = (Carnivore) threat;

            // Nếu kẻ thù quá mạnh (Vượt ngưỡng can đảm, ví dụ: Gấu, Sư tử)
            if (predator.getStrengthWeight() > this.courageThreshold) {
                // Không dám húc, nhường quyền cho bộ não tầng dưới (ví dụ: ScaredStrategy) để chạy trốn!
                nextLogic.execute(animal);
                return;
            }

            // Kẻ thù vừa tầm (Ví dụ: Cáo, Sói cô độc) -> Bứt tốc lao vào húc trả đũa!
            animal.setCurrentState(AnimalState.CHASING);
            
            // Tăng tốc húc mục tiêu (Gấp 1.2 lần tốc độ gốc)
            double chargeSpeed = animal.getSpeed() * 1.2;
            moveToward(animal, predator, chargeSpeed);
            return;
        }

        // --- ƯU TIÊN 2: TẤN CÔNG THỨC ĂN BẤT CHẤP (TỪ BẢN CŨ) ---
        // Nếu không có mối đe dọa, con vật sẽ chuyển sang trạng thái đói khát và tìm cỏ hung hãn
        Entity food = findNearestFood(animal);
        if (food != null) {
            animal.setCurrentState(AnimalState.CHASING);
            moveToward(animal, food, animal.getSpeed());
            return;
        }

        // --- TẦNG CUỐI: CHUYỂN TIẾP NÃO BỘ DỰ PHÒNG ---
        // Không có kẻ thù, không có đồ ăn trong tầm nhìn -> Thực hiện logic đi dạo / bầy đàn tiếp theo
        nextLogic.execute(animal);
    }

    private Entity findNearestThreat(Animal animal) {
        Entity nearest = null;
        double vision = animal.getVisionRadius();
        double minDistance = vision;

        // Tạo vùng không gian quét dựa trên tầm nhìn
        Rectangle searchRange = new Rectangle(
            animal.getPosition().getX(), animal.getPosition().getY(), 
            vision * 2, vision * 2
        );
        
        List<Entity> nearby = Environment.getInstance().getQuadTree().query(searchRange, null);

        for (Entity e : nearby) {
            if (e instanceof Carnivore && e.isAlive()) {
                double dist = animal.getPosition().distanceTo(e.getPosition());
                if (dist <= vision && dist < minDistance) {
                    minDistance = dist;
                    nearest = e;
                }
            }
        }
        return nearest;
    }

    private Entity findNearestFood(Animal animal) {
        Entity nearest = null;
        // Bản cũ cho phép ngửi/nhìn thấy thức ăn xa hơn một chút (x1.5 tầm nhìn)
        double foodVision = animal.getVisionRadius() * 1.5; 
        double minDistance = foodVision;

        Rectangle searchRange = new Rectangle(
            animal.getPosition().getX(), animal.getPosition().getY(), 
            foodVision * 2, foodVision * 2
        );

        List<Entity> nearby = Environment.getInstance().getQuadTree().query(searchRange, null);

        for (Entity e : nearby) {
            if (e instanceof Grass && e.isAlive()) {
                double dist = animal.getPosition().distanceTo(e.getPosition());
                if (dist <= foodVision && dist < minDistance) {
                    minDistance = dist;
                    nearest = e;
                }
            }
        }
        return nearest;
    }

    private void moveToward(Animal animal, Entity target, double speed) {
        Vector2D direction = new Vector2D(
            target.getPosition().getX() - animal.getPosition().getX(),
            target.getPosition().getY() - animal.getPosition().getY()
        );

        direction.normalize(); // Chuẩn hóa Vector về độ dài bằng 1

        // Áp đặt vận tốc di chuyển mới
        animal.getVelocity().setX(direction.getX() * speed);
        animal.getVelocity().setY(direction.getY() * speed);
    }
}