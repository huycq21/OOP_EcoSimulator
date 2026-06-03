package model.strategy;

import model.Animal;
import model.AnimalState;
import model.Entity;
import model.Vector2D;
import model.carnivore.Carnivore;
import model.environment.Environment;
import model.environment.Rectangle;
import java.util.List;

public class AggressiveStrategy implements SurvivalStrategy {
    private SurvivalStrategy nextLogic; // Lớp não dự phòng (Scared -> Foraging...)
    private double courageThreshold;

    public AggressiveStrategy(SurvivalStrategy nextLogic, double courageThreshold) {
        this.nextLogic = nextLogic;
        this.courageThreshold = courageThreshold;
    }

    @Override
    public void execute(Animal animal) {
        Entity threat = findNearestThreat(animal);

        if (threat != null) {
            Carnivore predator = (Carnivore) threat;

            // KIỂM TRA ĐE DỌA: Kẻ thù có vượt quá độ can đảm không?
            if (predator.getStrengthWeight() > this.courageThreshold) {
                // Kẻ thù quá mạnh (Ví dụ: Gấu)! 
                // Không dám húc, nhường quyền lại cho não dưới (ScaredStrategy) để nó tính đường chạy trốn!
                nextLogic.execute(animal);
                return;
            }

            // Kẻ thù vớ vẩn (Ví dụ: Cáo, Sói) -> LAO VÀO HÚC MÀ KHÔNG CẦN CHẠY TRỐN!
            animal.setCurrentState(AnimalState.CHASING);

            Vector2D myPos = animal.getPosition();
            Vector2D threatPos = threat.getPosition();
            
            Vector2D attackVector = new Vector2D(
                threatPos.getX() - myPos.getX(),
                threatPos.getY() - myPos.getY()
            );
            attackVector.normalize();

            // Lợn rừng húc thì phải bứt tốc độ! (ví dụ x1.2)
            double chargeSpeed = animal.getSpeed() * 1.2;
            animal.getVelocity().setX(attackVector.getX() * chargeSpeed);
            animal.getVelocity().setY(attackVector.getY() * chargeSpeed);

        } else {
            // Không có kẻ thù -> Đi dạo, ăn cỏ, đi theo bầy...
            nextLogic.execute(animal);
        }
    }

    private Entity findNearestThreat(Animal animal) {
        Entity nearest = null;
        double vision = animal.getVisionRadius();
        double minDistance = vision;

        Rectangle searchRange = new Rectangle(
            animal.getPosition().getX(), animal.getPosition().getY(), 
            vision * 2, vision * 2
        );
        
        List<Entity> nearby = Environment.getInstance().getQuadTree().query(searchRange, null);

        for (Entity e : nearby) {
            // Chỉ "gắt" với thú ăn thịt thôi
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
}