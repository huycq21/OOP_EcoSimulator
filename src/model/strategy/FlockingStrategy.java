package model.strategy;

import model.Animal;
import model.AnimalState;
import model.Entity;
import model.Vector2D;
import model.environment.Environment;
import model.environment.Rectangle;
import java.util.ArrayList;
import java.util.List;
import controller.SimulationConstant;

public class FlockingStrategy implements SurvivalStrategy {
    private final SurvivalStrategy nextLogic; 

    // Các hằng số khoảng cách kế thừa từ bản cũ để căn chỉnh bầy đàn mượt mà
    private static final double SEPARATION_DISTANCE = 35.0;

    public FlockingStrategy() {
        this.nextLogic = new PassiveStrategy();
    }

    public FlockingStrategy(SurvivalStrategy customLogic) {
        this.nextLogic = customLogic;
    }

    @Override
    public void execute(Animal animal) {
        // --- 1. QUẢN LÝ CHU KỲ THỜI GIAN ĐI ĐÀN / RÃ BẦY (TỪ BẢN MỚI) ---
        if (animal.isRestingFromFlock()) {
            animal.setRestingTimer(animal.getRestingTimer() + 1);
            
            if (animal.getRestingTimer() >= SimulationConstant.REST_TICKS) {
                // Đã nghỉ ngơi đủ thời gian -> Dỡ bỏ lệnh cấm và reset đồng hồ bầy đàn
                animal.setRestingFromFlock(false);
                animal.setRestingTimer(0);
                animal.setFlockingTimer(0);
            } else {
                // Vẫn đang trong thời gian xả bầy -> Ép chạy AI cấp dưới (đi dạo tự do)
                nextLogic.execute(animal);
                return;
            }
        }

        // Tìm kiếm đồng bọn xung quanh bằng QuadTree tối ưu
        List<Animal> allies = findNearbyAllies(animal);

        // --- 2. XỬ LÝ ĐIỀU HƯỚNG DI CHUYỂN THEO BẦY (BOIDS ALGORITHM) ---
        if (!allies.isEmpty()) {
            // Tăng thời gian đã đi cùng bầy
            animal.setFlockingTimer(animal.getFlockingTimer() + 1);

            // Nếu đi chung quá lâu -> Kích hoạt trạng thái mệt mỏi, rã đàn sang chu kỳ nghỉ
            if (animal.getFlockingTimer() >= SimulationConstant.MAX_FLOCK_TICKS) {
                animal.setRestingFromFlock(true);
                animal.setRestingTimer(0);
                nextLogic.execute(animal);
                return;
            }

            // Đổi trạng thái hiển thị sang đi lang thang (theo bầy)
            animal.setCurrentState(AnimalState.WANDERING);

            // Tính toán 3 lực cốt lõi theo cấu trúc hàm sạch sẽ của bản cũ
            Vector2D separation = calculateSeparation(animal, allies);
            Vector2D alignment = calculateAlignment(allies);
            Vector2D cohesion = calculateCohesion(animal, allies);

            // Trộn các lực lại với nhau theo trọng số (Weights) tối ưu
            Vector2D finalVector = new Vector2D(
                (separation.getX() * 1.8) + (alignment.getX() * 1.2) + (cohesion.getX() * 1.0),
                (separation.getY() * 1.8) + (alignment.getY() * 1.2) + (cohesion.getY() * 1.0)
            );
            
            finalVector.normalize();

            // Áp dụng vận tốc di chuyển mới cho động vật (Tốc độ đi đàn vừa phải)
            double flockSpeed = animal.getSpeed() * 0.6;
            animal.getVelocity().setX(finalVector.getX() * flockSpeed);
            animal.getVelocity().setY(finalVector.getY() * flockSpeed);
            
        } else {
            // Đứng một mình không thấy ai -> Trở về làm kẻ lang thang tự do
            nextLogic.execute(animal);
        }
    }

    /**
     * LỰC 1: SEPARATION - Tránh va chạm, giữ khoảng cách an toàn với các cá thể quá gần
     */
    private Vector2D calculateSeparation(Animal animal, List<Animal> allies) {
        Vector2D steer = new Vector2D(0, 0);
        int count = 0;

        for (Animal other : allies) {
            double dist = animal.getPosition().distanceTo(other.getPosition());

            if (dist < SEPARATION_DISTANCE && dist > 0) {
                Vector2D diff = new Vector2D(
                    animal.getPosition().getX() - other.getPosition().getX(),
                    animal.getPosition().getY() - other.getPosition().getY()
                );
                
                diff.normalize();
                // Lực đẩy tỷ lệ nghịch với khoảng cách: Càng sát nhau đẩy càng mạnh
                steer.add(new Vector2D(diff.getX() / dist, diff.getY() / dist));
                count++;
            }
        }
        
        if (count > 0) {
            steer.setX(steer.getX() / count);
            steer.setY(steer.getY() / count);
        }
        return steer;
    }

    /**
     * LỰC 2: ALIGNMENT - Đồng hướng, hướng vận tốc theo vận tốc trung bình của cả bầy
     */
    private Vector2D calculateAlignment(List<Animal> allies) {
        Vector2D avgVelocity = new Vector2D(0, 0);

        for (Animal other : allies) {
            avgVelocity.add(other.getVelocity());
        }

        avgVelocity.setX(avgVelocity.getX() / allies.size());
        avgVelocity.setY(avgVelocity.getY() / allies.size());
        avgVelocity.normalize();
        
        return avgVelocity;
    }

    /**
     * LỰC 3: COHESION - Tụ tập, tạo lực hướng tâm di chuyển về vị trí trung tâm của bầy
     */
    private Vector2D calculateCohesion(Animal animal, List<Animal> allies) {
        double centerX = 0;
        double centerY = 0;

        for (Animal other : allies) {
            centerX += other.getPosition().getX();
            centerY += other.getPosition().getY();
        }

        centerX /= allies.size();
        centerY /= allies.size();

        Vector2D direction = new Vector2D(
            centerX - animal.getPosition().getX(),
            centerY - animal.getPosition().getY()
        );
        
        direction.normalize();
        return direction;
    }

    /**
     * Quét tìm đồng loại trong tầm nhìn bằng cấu trúc QuadTree (Tối ưu từ bản mới)
     */
    protected List<Animal> findNearbyAllies(Animal animal) {
        List<Animal> allies = new ArrayList<>();
        double vision = animal.getVisionRadius();

        Rectangle searchRange = new Rectangle(
            animal.getPosition().getX(), animal.getPosition().getY(), vision * 2, vision * 2
        );

        List<Entity> nearby = Environment.getInstance().getQuadTree().query(searchRange, null);

        for (Entity e : nearby) {
            // Điều kiện lọc: Phải cùng chính xác lớp (Cáo theo Cáo, Thỏ theo Thỏ), còn sống, và không phải chính nó
            if (e.getClass() == animal.getClass() && e.isAlive() && e != animal) {
                Animal potentialAlly = (Animal) e;
                
                // Đồng bọn phải đang KHÔNG trong trạng thái nghỉ xả bầy thì mới tụ tập chung
                if (!potentialAlly.isRestingFromFlock()) {
                    double dist = animal.getPosition().distanceTo(potentialAlly.getPosition());
                    if (dist <= vision) {
                        allies.add(potentialAlly);
                    }
                }
            }
        }
        return allies;
    }
}