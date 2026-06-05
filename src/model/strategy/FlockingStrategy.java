package model.strategy;

import model.Animal;
import model.AnimalState;
import model.Entity;
import model.Vector2D;
import model.environment.Environment;
import model.environment.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class FlockingStrategy implements SurvivalStrategy {
    private SurvivalStrategy nextLogic; 

    public FlockingStrategy() {
        this.nextLogic = new PassiveStrategy();
    }

    public FlockingStrategy(SurvivalStrategy customLogic) {
        this.nextLogic = customLogic;
    }

    @Override
    public void execute(Animal animal) {
        List<Animal> allies = findNearbyAllies(animal);

        // NẾU CÓ ĐỒNG BỌN -> KÍCH HOẠT TẬP TÍNH BẦY ĐÀN
        if (!allies.isEmpty()) {
            animal.setCurrentState(AnimalState.WANDERING);

            Vector2D cohesion = new Vector2D(0, 0);   // Điểm đến chung (Tụ tập)
            Vector2D alignment = new Vector2D(0, 0);  // Hướng đi chung (Đồng hướng)
            Vector2D separation = new Vector2D(0, 0); // Lực đẩy (Giãn cách)

            for (Animal ally : allies) {
                double dist = animal.getPosition().distanceTo(ally.getPosition());

                // 1. Cộng dồn tọa độ để tìm tâm điểm của bầy
                cohesion.add(ally.getPosition());

                // 2. Cộng dồn vector vận tốc để xem bầy đang trôi về đâu
                alignment.add(ally.getVelocity());

                // 3. Nếu đứng quá gần nhau (ví dụ < 60.0), tạo lực đẩy né nhau ra
                if (dist < 60.0 && dist > 0) {
                    double pushX = animal.getPosition().getX() - ally.getPosition().getX();
                    double pushY = animal.getPosition().getY() - ally.getPosition().getY();
                    // Càng gần lực đẩy càng mạnh (chia cho dist^2)
                    separation.add(new Vector2D(pushX / dist / dist * 10, pushY / dist / dist * 10));
                }
            }

            int count = allies.size();

            // Tính trung bình
            cohesion.setX(cohesion.getX() / count);
            cohesion.setY(cohesion.getY() / count);
            
            alignment.setX(alignment.getX() / count);
            alignment.setY(alignment.getY() / count);

            // Tính hướng đi từ bản thân tới Tâm của bầy (Cohesion Vector)
            Vector2D steerToCenter = new Vector2D(
                cohesion.getX() - animal.getPosition().getX(),
                cohesion.getY() - animal.getPosition().getY()
            );
            // steerToCenter.normalize();
            // alignment.normalize();
            // separation.normalize();

            // TRỘN 3 LỰC LẠI VỚI NHAU (Bạn có thể tinh chỉnh các hệ số này)
            // Ví dụ: Ưu tiên giãn cách (1.5) > Đồng hướng (1.0) = Tụ tập (1.0)
            Vector2D finalVector = new Vector2D(
                (steerToCenter.getX() * 1.0) + (alignment.getX() * 1.0) + (separation.getX() * 1.5),
                (steerToCenter.getY() * 1.0) + (alignment.getY() * 1.0) + (separation.getY() * 1.5)
            );
            finalVector.normalize();

            // Áp dụng vector mới
            double pace = animal.getSpeed() * 0.2;
            animal.getVelocity().setX(finalVector.getX() * pace);
            animal.getVelocity().setY(finalVector.getY() * pace);
            
        } else {
            // NẾU ĐỨNG MỘT MÌNH KO THẤY AI -> TRỞ VỀ LÀM KẺ LANG THANG (nextLogic)
            nextLogic.execute(animal);
        }
    }

    // Đổi kiểu trả về thành List<Animal> để lấy nguyên bầy
    protected List<Animal> findNearbyAllies(Animal animal) {
        List<Animal> allies = new ArrayList<>();
        double vision = animal.getVisionRadius();

        Rectangle searchRange = new Rectangle(
            animal.getPosition().getX(), animal.getPosition().getY(), vision * 2, vision * 2
        );

        List<Entity> nearby = Environment.getInstance().getQuadTree().query(searchRange, null);

        for (Entity e : nearby) {
            if (e.getClass() == animal.getClass() && e.isAlive() && e != animal) {
                double dist = animal.getPosition().distanceTo(e.getPosition());
                if (dist <= vision) {
                    allies.add((Animal) e);
                }
            }
        }
        return allies;
    }
}