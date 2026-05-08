package model.strategy;

import model.Animal;
import model.AnimalState;
import model.Entity;
import model.Vector2D;
import model.environment.Environment;
import model.environment.Rectangle;
import java.util.List;

public class FlockingStrategy implements SurvivalStrategy {
    private SurvivalStrategy idleLogic; // Logic dự phòng khi đi 1 mình

    // Constructor mặc định (rảnh thì đi dạo)
    public FlockingStrategy() {
        this.idleLogic = new PassiveStrategy();
    }

    // Constructor linh hoạt (rảnh thì làm gì đó khác)
    public FlockingStrategy(SurvivalStrategy customIdleLogic) {
        this.idleLogic = customIdleLogic;
    }

    @Override
    public void execute(Animal animal) {
        // KHÔNG CÒN fleeLogic Ở ĐÂY NỮA. CHỈ TẬP TRUNG TÌM ĐỒNG BỌN.
        Entity ally = findNearestAlly(animal);

        if (ally != null) {
            animal.setCurrentState(AnimalState.WANDERING);
            
            double dist = animal.getPosition().distanceTo(ally.getPosition());
            
            // Cohesion: Đi lại gần nhau nếu cách xa
            if (dist > 20.0) {
                double dx = ally.getPosition().getX() - animal.getPosition().getX();
                double dy = ally.getPosition().getY() - animal.getPosition().getY();

                Vector2D flockVector = new Vector2D(dx, dy);
                flockVector.normalize();
                
                flockVector.setX(flockVector.getX() * animal.getSpeed() * 0.6);
                flockVector.setY(flockVector.getY() * animal.getSpeed() * 0.6);

                animal.getVelocity().setX(flockVector.getX());
                animal.getVelocity().setY(flockVector.getY());
            } else {
                // Sát nhau rồi thì cùng tản bộ
                idleLogic.execute(animal);
            }
        } else {
            // Không thấy bầy -> Làm việc khác
            idleLogic.execute(animal);
        }
    }

    private Entity findNearestAlly(Animal animal) {
        Entity nearest = null;
        double vision = animal.getVisionRadius();
        double minDistance = vision;

        Rectangle searchRange = new Rectangle(
            animal.getPosition().getX(), animal.getPosition().getY(), vision * 2, vision * 2
        );

        List<Entity> nearby = Environment.getInstance().getQuadTree().query(searchRange, null);

        for (Entity e : nearby) {
            // Tìm CÙNG LOÀI, đang sống và không phải bản thân mình
            if (e.getClass() == animal.getClass() && e.isAlive() && e != animal) {
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