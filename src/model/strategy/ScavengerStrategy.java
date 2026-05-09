package model.strategy;

import model.Animal;
import model.AnimalState;
import model.Entity;
import model.Vector2D;
import model.Carcass;
import model.environment.Environment;
import model.environment.Rectangle;
import java.util.List;

public class ScavengerStrategy implements SurvivalStrategy {
    private SurvivalStrategy nextStrategy; // Logic dự phòng (Ví dụ: Flocking hoặc Hunter)

    public ScavengerStrategy(SurvivalStrategy nextStrategy) {
        this.nextStrategy = nextStrategy;
    }

    @Override
    public void execute(Animal scavenger) {
        // 1. ƯU TIÊN CAO NHẤT: Tìm xác chết xung quanh
        Entity carcass = findNearestCorpse(scavenger);

        if (carcass != null) {
            // Đã thấy xác! Chuyển trạng thái sang ĐANG ĂN và di chuyển tới đó
            scavenger.setCurrentState(AnimalState.EATING);
            
            Vector2D targetPos = carcass.getPosition();
            Vector2D myPos = scavenger.getPosition();

            double dx = targetPos.getX() - myPos.getX();
            double dy = targetPos.getY() - myPos.getY();

            Vector2D moveVector = new Vector2D(dx, dy);
            moveVector.normalize();
            
            // Di chuyển tới xác với tốc độ bình thường
            moveVector.setX(moveVector.getX() * scavenger.getSpeed());
            moveVector.setY(moveVector.getY() * scavenger.getSpeed());

            scavenger.getVelocity().setX(moveVector.getX());
            scavenger.getVelocity().setY(moveVector.getY());
            
        } else {
            // 2. Nếu không thấy xác chết, thực hiện logic tiếp theo (Bầy đàn / Săn mồi)
            if (nextStrategy != null) {
                nextStrategy.execute(scavenger);
            }
        }
    }

    private Entity findNearestCorpse(Animal scavenger) {
        Entity nearest = null;
        double vision = scavenger.getVisionRadius();
        double minDistance = vision;

        Rectangle searchRange = new Rectangle(
            scavenger.getPosition().getX(),
            scavenger.getPosition().getY(),
            vision * 2, vision * 2
        );

        List<Entity> nearby = Environment.getInstance().getQuadTree().query(searchRange, null);

        for (Entity e : nearby) {
            // Chỉ ăn xác (Carcass) VÀ KHÔNG PHẢI XÁC ĐỒNG LOẠI
            if (e instanceof Carcass) {
                Carcass meat = (Carcass) e;
                if (meat.getOriginalSpecies() != scavenger.getClass()) {
                    double dist = scavenger.getPosition().distanceTo(meat.getPosition());
                    if (dist <= vision && dist < minDistance) {
                        minDistance = dist;
                        nearest = meat;
                    }
                }
            }
        }
        return nearest;
    }
}