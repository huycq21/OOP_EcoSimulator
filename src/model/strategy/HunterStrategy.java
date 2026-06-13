package model.strategy;

import model.Animal;
import model.AnimalState;
import model.Entity;
import model.Vector2D;
import model.herbivore.Herbivore;
import model.carnivore.Carnivore;
import model.carnivore.Fox;
import model.environment.Environment;
import model.environment.Rectangle;
import java.util.List;

import controller.SimulationConstant;

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
        // --- XỬ LÝ DI CHUYỂN SĂN MỒI ---
        if (target != null) {
            Animal prey = (Animal) target;
            Vector2D hunterPos = hunter.getPosition();
            Vector2D targetPos = prey.getPosition();
            double distance = hunterPos.distanceTo(targetPos);
            
            // Tính khoảng cách vừa chạm da nhau
            double attackRange = (hunter.getSize() + prey.getSize()) / 2.0 + 1.0; 

            // ==========================================
            // BƯỚC 1: XÁC ĐỊNH TỐC ĐỘ CỦA FRAME NÀY TRƯỚC
            // ==========================================
            double currentMoveSpeed = hunter.getSpeed();
            double strikeDistance = (hunter instanceof Carnivore) ? 
                                    ((Carnivore) hunter).getStrikeRadius() : 
                                    (hunter.getVisionRadius() * 0.3);

            if (prey.getCurrentState() == AnimalState.FLEEING || distance <= strikeDistance) {
                hunter.setCurrentState(AnimalState.CHASING);
                currentMoveSpeed = hunter.getSpeed(); 
            } else {
                hunter.setCurrentState(AnimalState.SNEAKING);
                currentMoveSpeed = hunter.getSpeed() * 0.1; 
            }

            // ==========================================
            // BƯỚC 2: CƠ CHẾ MAGNET SNAP (Ý TƯỞNG CỦA BẠN)
            // ==========================================
            // Nếu khoảng cách hiện tại <= Tầm đánh + Quãng đường sẽ chạy
            // Nghĩa là: BƯỚC NHẢY TIẾP THEO CHẮC CHẮN CHẠM HOẶC VƯỢT QUA CON MỒI!
            if (distance <= attackRange + currentMoveSpeed) {
                // TELEPORT: Dịch chuyển thẳng vào vị trí con mồi (Tạo hiệu ứng vồ mồi)
                hunter.getPosition().setX(targetPos.getX());
                hunter.getPosition().setY(targetPos.getY());
                
                // Khóa phanh, chuyển state để hàm CollisionHandler lo phần sát thương
                hunter.getVelocity().setX(0);
                hunter.getVelocity().setY(0);
                hunter.setCurrentState(AnimalState.ATTACKING);
                return; // KẾT THÚC HÀM TẠI ĐÂY
            }

            // ==========================================
            // BƯỚC 3: CÒN XA -> ÁP DỤNG VECTOR ĐỂ CHẠY TỚI
            // ==========================================
            Vector2D moveVector = new Vector2D(
                targetPos.getX() - hunterPos.getX(), 
                targetPos.getY() - hunterPos.getY()
            );
            moveVector.normalize();
            
            hunter.getVelocity().setX(moveVector.getX() * currentMoveSpeed);
            hunter.getVelocity().setY(moveVector.getY() * currentMoveSpeed);
            
        } else {
            // Đói nhưng không thấy mồi
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
            // Kiểm tra các điều kiện lọc cơ bản (Sự tương khắc chuỗi thức ăn, trạng thái ẩn nấp)
            if (!isValidPrey(hunter, e)) continue;
            
            if (e instanceof Animal && e.isAlive() && e.getClass() != hunter.getClass()) {
                Animal preyCandidate = (Animal) e;
                
                boolean isHiding = (preyCandidate.getCurrentState() == AnimalState.HIDING);
                // Đặc quyền của Cáo (Fox): Dáng bé, chui được vào bụi
                boolean canSeePrey = !isHiding || (hunter.getSize() < controller.SimulationConstant.CATCH_SIZE);

                if (canSeePrey) {
                    boolean isPreyValid = false;

                    if (preyCandidate instanceof Herbivore) {
                        isPreyValid = true;
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
        
        // Nếu con mồi đang trốn và kẻ đi săn quá to -> Bỏ qua
        if (prey.getCurrentState() == AnimalState.HIDING && !(hunter.getSize() < SimulationConstant.CATCH_SIZE)) return false;

        // Kiểm tra xem thú săn mồi có "tư cách" cắn con này không (Ví dụ Sói không cắn được Voi)
        if (hunter instanceof Carnivore) {
            return ((Carnivore) hunter).canAttack(prey);
        }
        return true;
    }

    /**
     * Lấy bán kính quét mồi đặc trưng của thực thể
     */
    private double getPreyDetectionRadius(Animal hunter) {
        if (hunter instanceof Carnivore) {
            return ((Carnivore) hunter).getPreyDetectionRadius();
        }
        return hunter.getVisionRadius();
    }
}