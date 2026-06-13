package model.strategy;

import model.Animal;
import model.AnimalState;
import model.Carcass;
import model.Entity;
import model.Vector2D;
import model.carnivore.Carnivore;
import model.environment.Environment;
import model.environment.Rectangle;
import java.util.List;

public class ScavengerStrategy implements SurvivalStrategy {
    private final SurvivalStrategy nextLogic; // Chuỗi AI tầng dưới (Ví dụ: HunterStrategy)

    public ScavengerStrategy(SurvivalStrategy nextLogic) {
        this.nextLogic = nextLogic;
    }

    @Override
    public void execute(Animal scavenger) {
        // --- TẦNG 1: CAM KẾT ĐI SĂN (Tunnel Vision - Bản mới) ---
        // Nếu đang trong trạng thái rượt đuổi gắt gao hoặc đang tung đòn vồ mồi sống, tuyệt đối không phân tâm vì xác chết!
        if (scavenger.getCurrentState() == AnimalState.CHASING || 
            scavenger.getCurrentState() == AnimalState.ATTACKING) {
            nextLogic.execute(scavenger);
            return;
        }

        // --- TẦNG 2: QUÉT RADAR PHÒNG VỆ CHỦ ĐỘNG (Kế thừa bản cũ, tối ưu hóa QuadTree) ---
        // Nếu bất ngờ phát hiện có một kẻ săn mồi nguy hiểm lảng vảng ngay gần mình, lập tức cắm đầu chạy thoát thân
        Entity predator = findImmediatePredator(scavenger);
        if (predator != null) {
            scavenger.setCurrentState(AnimalState.FLEEING);
            moveAwayFrom(scavenger, predator, scavenger.getSpeed());
            return;
        }

        // --- TẦNG 3: KIỂM TRA ĐIỀU KIỆN ĐÓI ---
        double energyPercentage = scavenger.getEnergy() / scavenger.getMaxEnergy();
        if (energyPercentage > 0.85) { // Thực thể đã no, không cần ăn xác, nhường quyền cho tầng dưới
            nextLogic.execute(scavenger);
            return;
        }

        // Nếu năng lượng tụt xuống mức nguy kịch (<25%), khứu giác thính hơn, tăng tầm ngửi mùi xác lên x1.5
        double searchRadius = scavenger.getVisionRadius() * ((energyPercentage < 0.25) ? 1.5 : 1.0);
        Entity carcass = findNearestCorpse(scavenger, searchRadius);

        // --- TẦNG 4: KẾT QUẢ TÌM XÁC CHẾT ---
        if (carcass == null) {
            nextLogic.execute(scavenger); // Không ngửi thấy mùi xác nào, chuyển sang đi săn mồi sống
            return;
        }

        // --- TẦNG 5: CÓ XÁC -> XỬ LÝ TRANH CHẤP NGAY TẠI MIẾNG ĂN (Bản mới) ---
        Entity competitor = findCompetitorNearCarcass(scavenger, carcass);
        
        if (competitor != null) {
            boolean isDesperate = false;
            if (scavenger instanceof Carnivore) {
                isDesperate = ((Carnivore) scavenger).isStarving(); 
            }

            if (isDesperate) {
                // ĐƯỜNG CÙNG: Chấp nhận lao vào tử chiến cướp đồ ăn
                double distanceToThreat = scavenger.getPosition().distanceTo(competitor.getPosition());
                double attackRange = (scavenger.getSize() + competitor.getSize()) / 2.0 + 1.0;

                if (distanceToThreat <= attackRange) {
                    // Áp sát mục tiêu cạnh tranh -> Khóa phanh giáp lá cà!
                    scavenger.getVelocity().setX(0);
                    scavenger.getVelocity().setY(0);
                    scavenger.setCurrentState(AnimalState.ATTACKING);
                } else {
                    // Còn ở xa -> Bật trạng thái truy đuổi lao thẳng vào đối thủ cạnh tranh
                    scavenger.setCurrentState(AnimalState.CHASING);
                    moveToward(scavenger, competitor, scavenger.getSpeed() * 1.2); 
                }
            } else {
                // CÒN LÝ TRÍ: Kẻ địch bảo vệ xác quá mạnh mà mình chưa đói lả -> Cúp đuôi bỏ chạy
                scavenger.setCurrentState(AnimalState.FLEEING);
                moveAwayFrom(scavenger, competitor, scavenger.getSpeed()); 
            }
            return;
        }

        // --- TẦNG 6: AN TOÀN -> TIẾN VÀO THƯỞNG THỨC THỨC ĂN ---
        double distanceToCarcass = scavenger.getPosition().distanceTo(carcass.getPosition());
        double eatRange = (scavenger.getSize() + carcass.getSize()) / 2.0 + 1.0; // Khoảng cách chạm vỏ vật lý

        if (distanceToCarcass <= eatRange) {
            // Đã đến nơi khít tọa độ -> Đứng im thưởng thức miếng mồi
            scavenger.setCurrentState(AnimalState.EATING);
            scavenger.getVelocity().setX(0);
            scavenger.getVelocity().setY(0);
        } else {
            // Còn khoảng cách -> Chuyển sang FORAGING (Tìm kiếm thức ăn) và lững thững đi tới (70% tốc độ)
            scavenger.setCurrentState(AnimalState.FORAGING); 
            moveToward(scavenger, carcass, scavenger.getSpeed() * 0.7); 
        }
    }

    /**
     * Dò tìm xem xung quanh thực thể có kẻ săn mồi nguy hiểm nào không (Lớp phòng vệ tầm gần)
     */
    private Entity findImmediatePredator(Animal animal) {
        Entity nearest = null;
        double minDistance = animal.getVisionRadius() * 0.8; // Phạm vi cảnh giác an toàn

        Rectangle searchRange = new Rectangle(
            animal.getPosition().getX(), animal.getPosition().getY(), 
            minDistance * 2, minDistance * 2
        );

        List<Entity> nearby = Environment.getInstance().getQuadTree().query(searchRange, null);

        for (Entity entity : nearby) {
            if (entity instanceof Carnivore && entity.isAlive() && entity != animal) {
                // Đảm bảo kẻ đó có khả năng tấn công mình (Tránh việc Sói sợ Cáo)
                if (((Carnivore) entity).canAttack(animal)) {
                    double distance = animal.getPosition().distanceTo(entity.getPosition());
                    if (distance < minDistance) {
                        minDistance = distance;
                        nearest = entity;
                    }
                }
            }
        }
        return nearest;
    }

    /**
     * Quét không gian xung quanh cái xác xem có đối thủ nào có chỉ số Sức mạnh (Strength) to hơn đang đứng chiếm giữ không
     */
    private Entity findCompetitorNearCarcass(Animal me, Entity carcass) {
        double disputeRadius = 40.0; // Bán kính tranh chấp quanh cái xác
        Rectangle searchRange = new Rectangle(
            carcass.getPosition().getX(), carcass.getPosition().getY(), 
            disputeRadius * 2, disputeRadius * 2
        );

        List<Entity> nearby = Environment.getInstance().getQuadTree().query(searchRange, null);
        double myStrength = (me instanceof Carnivore) ? ((Carnivore) me).getStrengthWeight() : 0;

        for (Entity e : nearby) {
            if (e instanceof Carnivore && e.isAlive() && e != me) {
                Carnivore enemy = (Carnivore) e;
                // Nếu đối thủ đứng gần xác có lực chiến mạnh hơn hẳn bản thân
                if (enemy.getStrengthWeight() > myStrength) {
                    return enemy; 
                }
            }
        }
        return null;
    }

    /**
     * Quét QuadTree tìm kiếm xác chết hợp lệ gần nhất thuộc thực đơn cho phép
     */
    private Entity findNearestCorpse(Animal scavenger, double radius) {
        Entity nearestCorpse = null;
        double minDistance = radius;
        
        Rectangle searchRange = new Rectangle(
            scavenger.getPosition().getX(), scavenger.getPosition().getY(), 
            radius * 2, radius * 2
        );

        List<Entity> nearbyEntities = Environment.getInstance().getQuadTree().query(searchRange, null);
        
        for (Entity e : nearbyEntities) {
            if (e instanceof Carcass) {
                Carcass carcass = (Carcass) e;
                // Kiểm tra hệ thống thực đơn đa hình xem loài này có ăn được loại thịt này không
                if (isCarcassInMenu(scavenger, carcass)) {
                    double dist = scavenger.getPosition().distanceTo(carcass.getPosition());
                    
                    if (dist <= radius && dist < minDistance) {
                        minDistance = dist;
                        nearestCorpse = carcass;
                    }
                }
            }
        }
        return nearestCorpse;
    }
    
    /**
     * Kiểm tra tính hợp lệ của món ăn dựa trên thuộc tính cấu hình PreyType của loài
     */
    private boolean isCarcassInMenu(Animal scavenger, Carcass carcass) {
        if (!(scavenger instanceof Carnivore)) return false;
        
        Carnivore carnivore = (Carnivore) scavenger;
        Class<?> meatType = carcass.getOriginalSpecies(); // Lấy nguồn gốc loài của xác chết

        return carnivore.getPreyType() != null && carnivore.getPreyType().contains(meatType);
    }

    /**
     * Toán học toán tử Vector di chuyển tiếp cận mục tiêu
     */
    private void moveToward(Animal animal, Entity target, double speed) {
        Vector2D dir = new Vector2D(
            target.getPosition().getX() - animal.getPosition().getX(), 
            target.getPosition().getY() - animal.getPosition().getY()
        );
        dir.normalize();
        animal.getVelocity().setX(dir.getX() * speed);
        animal.getVelocity().setY(dir.getY() * speed);
    }

    /**
     * Toán học toán tử Vector di chuyển trốn chạy khỏi mục tiêu nguy hiểm
     */
    private void moveAwayFrom(Animal animal, Entity threat, double speed) {
        Vector2D dir = new Vector2D(
            animal.getPosition().getX() - threat.getPosition().getX(), 
            animal.getPosition().getY() - threat.getPosition().getY()
        );
        dir.normalize();
        animal.getVelocity().setX(dir.getX() * speed);
        animal.getVelocity().setY(dir.getY() * speed);
    }
}