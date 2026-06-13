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
        // --- 1. CAM KẾT ĐI SĂN (Tunnel Vision) ---
        // Nếu đang rượt mồi sống hoặc đang cắn nhau, tuyệt đối không phân tâm tìm xác!
        if (scavenger.getCurrentState() == AnimalState.CHASING || 
            scavenger.getCurrentState() == AnimalState.ATTACKING) {
            nextLogic.execute(scavenger);
            return;
        }

        // --- 2. TÌM XÁC CHẾT ---
        double energyPercentage = scavenger.getEnergy() / scavenger.getMaxEnergy();
        if (energyPercentage > 0.85) { // No rồi thì thôi
            nextLogic.execute(scavenger);
            return;
        }

        // Đói thì ngửi mùi xác xa hơn
        double searchRadius = scavenger.getVisionRadius() * ((energyPercentage < 0.25) ? 1.5 : 1.0);
        Entity carcass = findNearestCorpse(scavenger, searchRadius);

        // --- 3. KHÔNG CÓ XÁC -> CHUYỂN QUA ĐI SĂN ---
        if (carcass == null) {
            nextLogic.execute(scavenger);
            return;
        }

        // --- 4. CÓ XÁC -> XỬ LÝ TRANH CHẤP TRƯỚC KHI ĂN ---
        Entity threat = findCompetitorNearCarcass(scavenger, carcass);
        
        if (threat != null) {
            boolean isDesperate = false;
            if (scavenger instanceof Carnivore) {
                isDesperate = ((Carnivore) scavenger).isStarving(); 
            }

            if (isDesperate) {
                // ==========================================
                // BƯỚC ĐƯỜNG CÙNG: LAO VÀO CẮN XÉ (CÓ PHANH)
                // ==========================================
                double distanceToThreat = scavenger.getPosition().distanceTo(threat.getPosition());
                double attackRange = (scavenger.getSize() + threat.getSize()) / 2.0 + 1.0;

                if (distanceToThreat <= attackRange) {
                    // Áp sát -> Phanh gấp cắn kẻ thù!
                    scavenger.getVelocity().setX(0);
                    scavenger.getVelocity().setY(0);
                    scavenger.setCurrentState(AnimalState.ATTACKING);
                } else {
                    // Chưa tới -> Bơm adrenaline lao tới
                    scavenger.setCurrentState(AnimalState.CHASING); // Hoặc giữ ATTACKING tùy logic của bạn
                    moveToward(scavenger, threat, scavenger.getSpeed() * 1.2); 
                }
            } else {
                // CÒN LÝ TRÍ: Cúp đuôi bỏ chạy
                scavenger.setCurrentState(AnimalState.FLEEING);
                moveAwayFrom(scavenger, threat, scavenger.getSpeed() ); 
            }
            return;
        }

        // --- 5. AN TOÀN -> TIẾN VÀO ĂN (CÓ PHANH VÀ CHUẨN KÍCH THƯỚC) ---
        double distanceToCarcass = scavenger.getPosition().distanceTo(carcass.getPosition());
        // Sửa lại công thức chia đôi cho đúng chuẩn vật lý của game
        double eatRange = (scavenger.getSize() + carcass.getSize()) / 2.0 + 1.0;

        if (distanceToCarcass <= eatRange) {
            // Tới nơi -> Phanh lại ăn!
            scavenger.setCurrentState(AnimalState.EATING);
            scavenger.getVelocity().setX(0);
            scavenger.getVelocity().setY(0);
        } else {
            // Còn xa -> Lững thững đi tới
            scavenger.setCurrentState(AnimalState.WANDERING); 
            moveToward(scavenger, carcass, scavenger.getSpeed() * 0.8); 
        }
    }

    // --- CÁC HÀM HỖ TRỢ GIỮ NGUYÊN BÊN DƯỚI ---
    private Entity findCompetitorNearCarcass(Animal me, Entity carcass) {
        double disputeRadius = 40.0; 
        Rectangle searchRange = new Rectangle(
            carcass.getPosition().getX(), carcass.getPosition().getY(), 
            disputeRadius * 2, disputeRadius * 2
        );

        List<Entity> nearby = Environment.getInstance().getQuadTree().query(searchRange, null);
        double myStrength = (me instanceof Carnivore) ? ((Carnivore) me).getStrengthWeight() : 0;

        for (Entity e : nearby) {
            if (e instanceof Carnivore && e.isAlive() && e != me) {
                Carnivore enemy = (Carnivore) e;
                if (enemy.getStrengthWeight() > myStrength) {
                    return enemy; 
                }
            }
        }
        return null;
    }

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
    
    private boolean isCarcassInMenu(Animal scavenger, Carcass carcass) {
        if (!(scavenger instanceof Carnivore)) return false;
        
        Carnivore carnivore = (Carnivore) scavenger;
        Class<?> meatType = carcass.getOriginalSpecies();

        if (carnivore.getPreyType() != null && carnivore.getPreyType().contains(meatType)) {
            return true;
        }
        return false;
    }

    private void moveToward(Animal animal, Entity target, double speed) {
        Vector2D dir = new Vector2D(target.getPosition().getX() - animal.getPosition().getX(), target.getPosition().getY() - animal.getPosition().getY());
        dir.normalize();
        animal.getVelocity().setX(dir.getX() * speed);
        animal.getVelocity().setY(dir.getY() * speed);
    }

    private void moveAwayFrom(Animal animal, Entity threat, double speed) {
        Vector2D dir = new Vector2D(animal.getPosition().getX() - threat.getPosition().getX(), animal.getPosition().getY() - threat.getPosition().getY());
        dir.normalize();
        animal.getVelocity().setX(dir.getX() * speed);
        animal.getVelocity().setY(dir.getY() * speed);
    }
}