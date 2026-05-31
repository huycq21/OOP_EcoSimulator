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
    private SurvivalStrategy nextLogic; // Thường sẽ là HunterStrategy

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
            // Kiểm tra xem con vật có đang bị dồn vào đường cùng không
            boolean isDesperate = false;
            if (scavenger instanceof Carnivore) {
                isDesperate = ((Carnivore) scavenger).isStarving(); 
            } else {
                // Fallback an toàn nếu không phải Carnivore
                isDesperate = (energyPercentage < 0.25); 
            }

            if (isDesperate) {
                // BƯỚC ĐƯỜNG CÙNG: Mặc kệ kẻ thù mạnh cỡ nào, lao vào cắn xé!
                scavenger.setCurrentState(AnimalState.ATTACKING);
                
                // Bơm adrenaline, tốc độ lao vào đánh nhau cực nhanh (x1.2)
                moveToward(scavenger, threat, scavenger.getSpeed() * 1.2); 
            } else {
                // CÒN LÝ TRÍ: Vẫn đói nhưng chưa đến mức liều mạng, cúp đuôi bỏ chạy
                scavenger.setCurrentState(AnimalState.FLEEING);
                moveAwayFrom(scavenger, threat, scavenger.getSpeed() ); 
            }
            return;
        }

        // --- 5. AN TOÀN -> TIẾN VÀO ĂN ---
        double distanceToCarcass = scavenger.getPosition().distanceTo(carcass.getPosition());
        if (distanceToCarcass <= scavenger.getSize() + carcass.getSize()) {
            scavenger.setCurrentState(AnimalState.EATING);
            scavenger.getVelocity().setX(0);
            scavenger.getVelocity().setY(0);
            // Logic trừ thịt của xác và cộng năng lượng sẽ nằm ở hàm update() của con vật
        } else {
            scavenger.setCurrentState(AnimalState.WANDERING); // Hoặc tạo state APPROACHING
            moveToward(scavenger, carcass, scavenger.getSpeed() * 0.8); // Lững thững đi tới
        }
    }

    // Hàm quét tìm đối thủ cạnh tranh xung quanh cái xác
    private Entity findCompetitorNearCarcass(Animal me, Entity carcass) {
        double disputeRadius = 40.0; // Vùng tranh chấp quanh cái xác
        Rectangle searchRange = new Rectangle(
            carcass.getPosition().getX(), carcass.getPosition().getY(), 
            disputeRadius * 2, disputeRadius * 2
        );

        List<Entity> nearby = Environment.getInstance().getQuadTree().query(searchRange, null);
        double myStrength = (me instanceof Carnivore) ? ((Carnivore) me).getStrengthWeight() : 0;

        for (Entity e : nearby) {
            if (e instanceof Carnivore && e.isAlive() && e != me) {
                Carnivore enemy = (Carnivore) e;
                
                // Nếu khí chất của kẻ thù lớn hơn mình -> Nó là mối đe dọa
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
                
                // Kiểm tra xem cái xác này có nằm trong thực đơn không
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
        // 1. Chỉ Thú ăn thịt mới ăn xác
        if (!(scavenger instanceof Carnivore)) {
            return false;
        }
        
        Carnivore carnivore = (Carnivore) scavenger;
        
        // để trả về Class của con vật đã chết (được truyền vào lúc new Carcass)
        Class<?> meatType = carcass.getOriginalSpecies();

        // 3. Quét danh sách thực đơn (preyTypes)
        // carnivore.getPreyType() sẽ trả về List<Class<? extends Herbivore>>
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