package model.strategy;

import model.Animal;
import model.AnimalState;
import model.Entity;
import model.Vector2D;
import model.herbivore.Herbivore;
import model.carnivore.Carnivore;
import model.carnivore.Fox;
import model.apex.Eagle;
import model.environment.Environment;
import model.environment.Rectangle;
import java.util.List;

public class HunterStrategy implements SurvivalStrategy {
    private SurvivalStrategy nextLogic;

    public HunterStrategy() {
        this.nextLogic = new PassiveStrategy();
    }

    public HunterStrategy(SurvivalStrategy customLogic) {
        this.nextLogic = customLogic;
    }

    @Override
    public void execute(Animal hunter) {
        // --- TÍNH TOÁN MỨC ĐỘ ĐÓI ---
        double energyPercentage = hunter.getEnergy() / hunter.getMaxEnergy();

        // 1. TRẠNG THÁI NO: Năng lượng > 80%, không cần sát sinh
        if (energyPercentage > 0.80) {
            nextLogic.execute(hunter);
            return;
        }

        // 2. TRẠNG THÁI KHÁT MÁU: Năng lượng < 30%, tăng 50% tầm phát hiện mồi (x1.5)
        double radiusMultiplier = (energyPercentage < 0.30) ? 1.5 : 1.0;

        // Chỉ tìm mồi sống
        Entity target = findNearestPrey(hunter, radiusMultiplier);

        // --- XỬ LÝ DI CHUYỂN SĂN MỒI ---
        if (target != null) {
            Animal prey = (Animal) target;
            Vector2D hunterPos = hunter.getPosition();
            Vector2D targetPos = prey.getPosition();
            double distance = hunterPos.distanceTo(targetPos);
            
            double currentMoveSpeed = hunter.getSpeed();
            
            // Lấy cự ly phát động tấn công (bung sức) của loài đi săn
            double strikeDistance = (hunter instanceof Carnivore) ? 
                                    ((Carnivore) hunter).getStrikeRadius() : 
                                    (hunter.getVisionRadius() * 0.3);

            // NẾU MỒI BỎ CHẠY HOẶC ĐÃ VÀO TẦM BUNG SỨC -> RƯỢT ĐUỔI!
            if (prey.getCurrentState() == AnimalState.FLEEING || distance <= strikeDistance) {
                hunter.setCurrentState(AnimalState.CHASING);
                currentMoveSpeed = hunter.getSpeed() ; 
            } 
            // NẾU MỒI CHƯA BIẾT GÌ VÀ ĐANG Ở NGOÀI TẦM BUNG SỨC -> RÓN RÉN
            else {
                hunter.setCurrentState(AnimalState.SNEAKING);
                currentMoveSpeed = hunter.getSpeed() * 0.45; 
            }

            // Áp dụng vector vận tốc di chuyển tới con mồi
            Vector2D moveVector = new Vector2D(
                targetPos.getX() - hunterPos.getX(), 
                targetPos.getY() - hunterPos.getY()
            );
            moveVector.normalize();
            
            hunter.getVelocity().setX(moveVector.getX() * currentMoveSpeed);
            hunter.getVelocity().setY(moveVector.getY() * currentMoveSpeed);
            
        } else {
            // Đói nhưng không thấy mồi -> tiếp tục đi dạo tìm kiếm hoặc nhường quyền cho não khác
            nextLogic.execute(hunter); 
        }
    }

    // --- HÀM TÌM MỒI SỐNG ---
    private Entity findNearestPrey(Animal hunter, double radiusMultiplier) {
        Entity nearestPrey = null;
        
        double baseRadius = getPreyDetectionRadius(hunter);
        double activeVision = baseRadius * radiusMultiplier; // Nhân hệ số khát máu
        double minDistance = activeVision;

        Rectangle searchRange = new Rectangle(
            hunter.getPosition().getX(), 
            hunter.getPosition().getY(), 
            activeVision * 2, activeVision * 2
        );

        List<Entity> nearbyEntities = Environment.getInstance().getQuadTree().query(searchRange, null);

        for (Entity e : nearbyEntities) {
            if (!isValidPrey(hunter, e)) continue;
            
            if (e instanceof Animal && e.isAlive() && e.getClass() != hunter.getClass()) {
                Animal preyCandidate = (Animal) e;
                
                boolean isHiding = (preyCandidate.getCurrentState() == AnimalState.HIDING);
                // Đặc quyền của Cáo (Fox): Mũi thính, đánh hơi được cả mồi đang nấp trong bụi
                boolean canSeePrey = !isHiding || (hunter instanceof Fox);

                if (canSeePrey) {
                    boolean isPreyValid = false;

                    if (preyCandidate instanceof Herbivore) {
                        if (hunter instanceof Eagle) {
                            // Đại bàng chỉ cắp được mồi nhỏ (size <= 5.0)
                            if (preyCandidate.getSize() <= 5.0) isPreyValid = true;
                        } else {
                            isPreyValid = true;
                        }
                    } 
                    
                    if (isPreyValid) {
                        double dist = hunter.getPosition().distanceTo(preyCandidate.getPosition());
                        if (dist <= activeVision && dist < minDistance) {
                            minDistance = dist;
                            nearestPrey = preyCandidate;
                        }
                    }
                }
            }
        }
        return nearestPrey;
    }
    
    // --- CÁC HÀM LỌC ĐIỀU KIỆN ---
    private boolean isValidPrey(Animal hunter, Entity entity) {
        // Chỉ nhắm vào con vật còn sống và là thú ăn cỏ
        if (!entity.isAlive() || !(entity instanceof Herbivore)) return false;

        Herbivore prey = (Herbivore) entity; 
        
        // Nếu con mồi đang trốn và kẻ đi săn không phải Cáo -> Bỏ qua
        if (prey.getCurrentState() == AnimalState.HIDING && !(hunter instanceof Fox)) return false;

        // Kiểm tra xem thú săn mồi có "tư cách" cắn con này không (Ví dụ Sói không cắn được Voi)
        if (hunter instanceof Carnivore) {
            return ((Carnivore) hunter).canAttack(prey);
        }
        return true;
    }

    private double getPreyDetectionRadius(Animal hunter) {
        if (hunter instanceof Carnivore) {
            return ((Carnivore) hunter).getPreyDetectionRadius();
        }
        return hunter.getVisionRadius();
    }
}