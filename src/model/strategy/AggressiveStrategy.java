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
    private ScaredStrategy fleeLogic; // Bản năng bỏ chạy
    private PassiveStrategy wanderLogic; // Đi dạo
    private double courageThreshold; // Giới hạn chịu đựng đe dọa

    // Truyền độ can đảm vào lúc nạp não cho con vật
    // Ví dụ: Lợn rừng có courage = 90. Cáo/Sói (Threat 60-80) thì húc. Gấu (150) thì chạy!
    public AggressiveStrategy(double courageThreshold) {
        this.fleeLogic = new ScaredStrategy();
        this.wanderLogic = new PassiveStrategy();
        this.courageThreshold = courageThreshold;
    }

    @Override
    public void execute(Animal animal) {
        Entity threat = findNearestThreat(animal);

        if (threat != null) {
            Carnivore predator = (Carnivore) threat;

            // KIỂM TRA ĐE DỌA: Kẻ thù có vượt quá độ can đảm không?
            if (predator.getStrengthWeight() > this.courageThreshold) {
                // Quá mạnh! Quay xe bỏ chạy!
                fleeLogic.execute(animal);
                return;
            }

            // Nếu trong tầm kiểm soát -> Lao vào húc!
            animal.setCurrentState(AnimalState.CHASING);

            Vector2D myPos = animal.getPosition();
            Vector2D threatPos = threat.getPosition();
            double dx = threatPos.getX() - myPos.getX();
            double dy = threatPos.getY() - myPos.getY();

            Vector2D attackVector = new Vector2D(dx, dy);
            attackVector.normalize();

            attackVector.setX(attackVector.getX() * animal.getSpeed());
            attackVector.setY(attackVector.getY() * animal.getSpeed());

            animal.getVelocity().setX(attackVector.getX());
            animal.getVelocity().setY(attackVector.getY());

        } else {
            wanderLogic.execute(animal);
        }
    }

    private Entity findNearestThreat(Animal animal) {
        // (Giữ nguyên logic quét QuadTree tìm thú ăn thịt gần nhất như cũ)
        // ...
        Entity nearest = null;
        double vision = animal.getVisionRadius();
        double minDistance = vision;

        Rectangle searchRange = new Rectangle(animal.getPosition().getX(), animal.getPosition().getY(), vision * 2, vision * 2);
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
}