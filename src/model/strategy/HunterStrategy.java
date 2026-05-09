package model.strategy;

import model.*;
import model.herbivore.Herbivore;
import model.carnivore.*;
import model.apex.Eagle;
import model.environment.Environment;
import model.environment.Rectangle; // Import thêm Rectangle để dùng QuadTree
import java.util.List;

public class HunterStrategy implements SurvivalStrategy {
    // Đổi PassiveStrategy cứng nhắc thành một SurvivalStrategy linh hoạt
    private SurvivalStrategy idleLogic; 

    // Constructor 1: Mặc định (như cũ) - Đi dạo khi rảnh
    public HunterStrategy() {
        this.idleLogic = new PassiveStrategy();
    }

    // Constructor 2: MỚI - Cho phép nạp tập tính bầy đàn (hoặc bất cứ gì) khi rảnh
    public HunterStrategy(SurvivalStrategy customIdleLogic) {
        this.idleLogic = customIdleLogic;
    }

    @Override
    public void execute(Animal hunter) {
        Entity target = findNearestCorpse(hunter);
        boolean isScavenging = true;

        if (target == null) {
            target = findNearestPrey(hunter);
            isScavenging = false;
        }

        if (target != null) {
            hunter.setCurrentState(isScavenging ? AnimalState.EATING : AnimalState.CHASING);
            Vector2D hunterPos = hunter.getPosition();
            Vector2D targetPos = target.getPosition();
            double dx = targetPos.getX() - hunterPos.getX();
            double dy = targetPos.getY() - hunterPos.getY();
            
            Vector2D moveVector = new Vector2D(dx, dy);
            moveVector.normalize();
            moveVector.setX(moveVector.getX() * hunter.getSpeed());
            moveVector.setY(moveVector.getY() * hunter.getSpeed());
            
            hunter.getVelocity().setX(moveVector.getX());
            hunter.getVelocity().setY(moveVector.getY());
        } else {
            // BÍ QUYẾT LÀ Ở ĐÂY: Khi không đi săn, gọi não bộ dự phòng!
            idleLogic.execute(hunter); 
        }
    }

    // --- HÀM TÌM XÁC CHẾT (ĐÃ DÙNG QUADTREE & CẤM ĂN ĐỒNG LOẠI) ---
    public Entity findNearestCorpse(Animal hunter) {
        Entity nearestCorpse = null;
        double vision = hunter.getVisionRadius();
        double minDistance = vision;

        // 1. Tạo vùng quét bằng tầm nhìn của con vật
        Rectangle searchRange = new Rectangle(
            hunter.getPosition().getX(), 
            hunter.getPosition().getY(), 
            vision * 2, vision * 2
        );

        // 2. Lấy danh sách thực thể xung quanh từ QuadTree (Siêu nhanh)
        List<Entity> nearbyEntities = Environment.getInstance().getQuadTree().query(searchRange, null);

        for (Entity e : nearbyEntities) {
            // Xác chết = Động vật VÀ Đã chết VÀ KHÔNG CÙNG LOÀI
            if (e instanceof Carcass && e.getClass() != hunter.getClass()) {
                double dist = hunter.getPosition().distanceTo(e.getPosition());
                
                // Vẫn phải check khoảng cách vì QuadTree trả về hình chữ nhật, tầm nhìn là hình tròn
                if (dist <= vision && dist < minDistance) {
                    minDistance = dist;
                    nearestCorpse = e;
                }
            }
        }
        return nearestCorpse;
    }

    // --- HÀM TÌM MỒI SỐNG (ĐÃ DÙNG QUADTREE & CẤM SĂN ĐỒNG LOẠI) ---
    public Entity findNearestPrey(Animal hunter) {
        Entity nearestPrey = null;
        double vision = hunter.getVisionRadius();
        double minDistance = vision;

        // 1. Tạo vùng quét bằng QuadTree
        Rectangle searchRange = new Rectangle(
            hunter.getPosition().getX(), 
            hunter.getPosition().getY(), 
            vision * 2, vision * 2
        );

        List<Entity> nearbyEntities = Environment.getInstance().getQuadTree().query(searchRange, null);

        for (Entity e : nearbyEntities) {
            
            if (e instanceof Animal && e.isAlive() && e.getClass() != hunter.getClass()) {
                
                Animal preyCandidate = (Animal) e;
                
                // KIỂM TRA TẦM NHÌN CỦA KẺ ĐI SĂN
                // Con mồi đang nấp lùm?
                boolean isHiding = (preyCandidate.getCurrentState() == AnimalState.HIDING);
                // Kẻ đi săn có thể nhìn thấy nếu: Con mồi KHÔNG nấp, HOẶC kẻ đi săn là Cáo (Fox)
                boolean canSeePrey = !isHiding || (hunter instanceof Fox);

                // Nếu thỏa mãn điều kiện nhìn thấy
                if (canSeePrey) {
                    boolean isValidPrey = false;

                    // 1. NẾU LÀ THÚ ĂN CỎ
                    if (preyCandidate instanceof Herbivore) {
                        if (hunter instanceof Eagle) {
                            if (preyCandidate.getSize() <= 5.0) isValidPrey = true;
                        } else {
                            isValidPrey = true;
                        }
                    } 
                    // 2. NẾU LÀ THÚ ĂN THỊT KHÁC LOÀI -> Cá lớn nuốt cá bé
                    else if (preyCandidate instanceof Carnivore && hunter instanceof Carnivore) {
                        Carnivore predator = (Carnivore) hunter;
                        Carnivore prey = (Carnivore) preyCandidate;

                        if (predator.getStrengthWeight() > prey.getStrengthWeight() * 1.3) {
                            if (hunter instanceof Eagle) {
                                if (prey.getSize() <= 5.0) isValidPrey = true;
                            } else {
                                isValidPrey = true;
                            }
                        }
                    }

                    // Nếu xác nhận là mồi ngon, tính khoảng cách
                    if (isValidPrey) {
                        double dist = hunter.getPosition().distanceTo(preyCandidate.getPosition());
                        if (dist <= vision && dist < minDistance) {
                            minDistance = dist;
                            nearestPrey = preyCandidate;
                        }
                    }
                }
            }
        }
        return nearestPrey;
    }
}