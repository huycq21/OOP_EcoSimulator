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

public class HunterStrategy implements SurvivalStrategy {
    private final SurvivalStrategy nextLogic;

    public HunterStrategy() {
        this.nextLogic = new PassiveStrategy();
    }

    public HunterStrategy(SurvivalStrategy customLogic) {
        this.nextLogic = customLogic;
    }

    @Override
    public void execute(Animal hunter) {
        // --- 1. TÍNH TOÁN MỨC ĐỘ ĐÓI ĐỂ KÍCH HOẠT BẢN NĂNG ---
        double energyPercentage = hunter.getEnergy() / hunter.getMaxEnergy();

        // TRẠNG THÁI NO: Năng lượng > 80%, không cần sát sinh, nhường quyền cho AI tầng dưới đi dạo/đi đàn
        if (energyPercentage > 0.80) {
            nextLogic.execute(hunter);
            return;
        }

        // TRẠNG THÁI KHÁT MÁU (BẢN MỚI): Năng lượng nguy kịch < 30%, tăng 50% tầm phát hiện mồi (x1.5)
        double radiusMultiplier = (energyPercentage < 0.30) ? 1.5 : 1.0;

        // Tiến hành dò tìm con mồi sống gần nhất thông qua hệ thống QuadTree
        Entity target = findNearestPrey(hunter, radiusMultiplier);

        // --- 2. XỬ LÝ DI CHUYỂN TIẾP CẬN VÀ VỒ MỒI ---
        if (target != null) {
            Animal prey = (Animal) target;
            Vector2D hunterPos = hunter.getPosition();
            Vector2D targetPos = prey.getPosition();
            double distance = hunterPos.distanceTo(targetPos);
            
            // Tính khoảng cách vừa chạm bề mặt vật lý của nhau (Dựa trên kích thước thực thể)
            double attackRange = (hunter.getSize() + prey.getSize()) / 2.0 + 1.0; 

            // BƯỚC A: XÁC ĐỊNH TRẠNG THÁI VÀ TỐC ĐỘ DI CHUYỂN TRONG TICK NÀY
            double currentMoveSpeed = hunter.getSpeed();
            double strikeDistance = (hunter instanceof Carnivore) ? 
                                    ((Carnivore) hunter).getStrikeRadius() : 
                                    (hunter.getVisionRadius() * 0.3);

            // Nếu con mồi đã phát hiện và bỏ chạy, hoặc kẻ săn mồi đã lọt vào tầm vồ (strikeDistance)
            if (prey.getCurrentState() == AnimalState.FLEEING || distance <= strikeDistance) {
                hunter.setCurrentState(AnimalState.CHASING);
                currentMoveSpeed = hunter.getSpeed(); // Bung 100% tốc độ để rượt đuổi
            } else {
                hunter.setCurrentState(AnimalState.SNEAKING);
                currentMoveSpeed = hunter.getSpeed() * 0.1; // Đi rình rập, ẩn mình bằng 10% tốc độ
            }

            // BƯỚC B: CƠ CHẾ VỒ MỒI CHÍNH XÁC (MAGNET SNAP)
            // Nếu bước nhảy tiếp theo chắc chắn chạm hoặc vượt quá vị trí con mồi
            if (distance <= attackRange + currentMoveSpeed) {
                // Teleport nhẹ: Đưa kẻ săn mồi áp sát khít vào tọa độ mục tiêu
                hunter.getPosition().setX(targetPos.getX());
                hunter.getPosition().setY(targetPos.getY());
                
                // Khóa phanh vận tốc, chuyển trạng thái để CollisionHandler xử lý trừ máu/sát thương
                hunter.getVelocity().setX(0);
                hunter.getVelocity().setY(0);
                hunter.setCurrentState(AnimalState.ATTACKING);
                return; 
            }

            // BƯỚC C: TOÁN HỌC VECTOR - DI CHUYỂN TIẾP CẬN KHI Ở XA (TỪ BẢN CŨ)
            Vector2D moveVector = new Vector2D(
                targetPos.getX() - hunterPos.getX(), 
                targetPos.getY() - hunterPos.getY()
            );
            moveVector.normalize(); // Chuẩn hóa Vector về độ dài đơn vị = 1
            
            // Áp đặt vận tốc di chuyển thực tế theo hướng mục tiêu
            hunter.getVelocity().setX(moveVector.getX() * currentMoveSpeed);
            hunter.getVelocity().setY(moveVector.getY() * currentMoveSpeed);
            
        } else {
            // Đái/Đói nhưng hoàn toàn không quét được con mồi nào trong tầm radar
            nextLogic.execute(hunter); 
        }
    }

    private Entity findNearestPrey(Animal hunter, double radiusMultiplier) {
        Entity nearestPrey = null;
        
        double baseRadius = getPreyDetectionRadius(hunter);
        double activeVision = baseRadius * radiusMultiplier; // Nhân hệ số khát máu nếu có
        double minDistance = activeVision;

        // Thiết lập hộp không gian tìm kiếm
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
                // Đặc quyền của Cáo (Fox): Mũi cực thính, đánh hơi được cả mồi đang ẩn nấp trong bụi rậm
                boolean canSeePrey = !isHiding || (hunter instanceof Fox);

                if (canSeePrey) {
                    double dist = hunter.getPosition().distanceTo(preyCandidate.getPosition());
                    
                    // Cập nhật mục tiêu sống tối ưu nhất ở gần radar nhất
                    if (dist <= activeVision && dist < minDistance) {
                        minDistance = dist;
                        nearestPrey = preyCandidate;
                    }
                }
            }
        }
        return nearestPrey;
    }
    
    private boolean isValidPrey(Animal hunter, Entity entity) {
        // Mục tiêu phải là thú ăn cỏ (Herbivore) và phải còn sống
        if (!entity.isAlive() || !(entity instanceof Herbivore)) return false;

        Herbivore prey = (Herbivore) entity; 
        
        // Cơ chế bụi rậm: Nếu mồi đang ẩn nấp mà kẻ đi săn có kích thước quá lớn (Size >= 5 như Sói, Gấu) -> Mắt mờ không nhìn thấy
        if (prey.getCurrentState() == AnimalState.HIDING && !(hunter.getSize() < 5)) return false;

        // Kiểm tra đa hình thông qua danh sách thực đơn canAttack() cấu hình riêng ở từng loài Carnivore
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