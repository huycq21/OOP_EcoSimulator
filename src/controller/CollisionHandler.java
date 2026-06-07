package controller;

import model.environment.*;
import model.*;
import model.carnivore.*;
import model.herbivore.*;
import model.plant.*;
import java.util.List;
import java.util.ArrayList;
import util.SoundManager;

public class CollisionHandler {

    private static long lastBushSound = 0;

    public static void processCollisions(Environment env) {
        List<Entity> entities = env.getEntities();
        List<Entity> newEntities = new ArrayList<>();

        QuadTree qTree = env.getQuadTree();

        for (Entity e1 : entities) {
            if (!e1.isAlive()) continue;

            double searchRadius = e1.getSize() * 2; 
            
            Rectangle searchRange = new Rectangle(
                    e1.getPosition().getX(), 
                    e1.getPosition().getY(), 
                    searchRadius, searchRadius
            );


            List<Entity> nearbyEntities = qTree.query(searchRange, null);

            for (Entity e2 : nearbyEntities) {
                if (e1 != e2 && e2.isAlive()) {
                    double distance = e1.getPosition().distanceTo(e2.getPosition());
                    double collisionRadius = (e1.getSize() + e2.getSize()) / 2.0;

                    if (distance < collisionRadius) {
                        resolveCollision(e1, e2, newEntities);
                    }
                }
            }
        }
        for (Entity newEntity : newEntities) {
            env.addEntity(newEntity);
        }
    }

    // --- PHÂN LOẠI VÀ XỬ LÝ TỪNG TRƯỜNG HỢP ---
    private static void resolveCollision(Entity e1, Entity e2, List<Entity> newEntities) {
        
        // 1. TRƯỜNG HỢP: Động vật ăn thịt đụng Động vật ăn cỏ
        if (e1 instanceof Carnivore && e2 instanceof Herbivore) {
            handleCombat((Carnivore) e1, (Herbivore) e2, newEntities);
        } else if (e2 instanceof Carnivore && e1 instanceof Herbivore) {
            handleCombat((Carnivore) e2, (Herbivore) e1, newEntities);
        }

        // 2. TRƯỜNG HỢP: Động vật ăn cỏ đụng Eatable (có thể là cây cỏ để ăn hoặc bụi rậm để trốn)
        else if (e1 instanceof Herbivore && e2 instanceof Eatable && !(e2 instanceof Carcass)) {
            handleEating((Herbivore) e1, (Eatable) e2);
        } else if (e2 instanceof Herbivore && e1 instanceof Eatable && !(e1 instanceof Carcass)) {
            handleEating((Herbivore) e2, (Eatable) e1);
        }

        // 3. TRƯỜNG HỢP: Động vật đụng Bụi rậm / Chướng ngại vật
        else if (e1 instanceof Animal && e2 instanceof Obstacle) {
            handleObstacleCollision((Animal) e1, (Obstacle) e2);
        } else if (e2 instanceof Animal && e1 instanceof Obstacle) {
            handleObstacleCollision((Animal) e2, (Obstacle) e1);
        }

        // 4. TRƯỜNG HỢP: Thú ăn thịt đụng Xác chết (Ăn xác)
        else if (e1 instanceof Carnivore && e2 instanceof Carcass) {
            handleEatingCarcass((Carnivore) e1, (Carcass) e2);
        } else if (e2 instanceof Carnivore && e1 instanceof Carcass) {
            handleEatingCarcass((Carnivore) e2, (Carcass) e1);
        }
    }

    // --- CÁC HÀM XỬ LÝ CHI TIẾT ---

        // Xử lý săn bắt và rơi ra Xác chết
    private static void handleCombat(Carnivore predator, Herbivore prey, List<Entity> newEntities) {
        // Nếu thỏ đang nấp trong bụi rậm thì Sói không cắn được
        if (prey.getCurrentState() == AnimalState.HIDING) return;
        if (prey.getCurrentState() == AnimalState.DEAD) return;
        if (!predator.canAttack(prey)) return;

        // Thú ăn thịt gọi hàm cắn (có tính toán Cooldown ở bên trong class Carnivore)
        predator.attack(prey);

        // Nếu con mồi cạn máu
        if (prey.getCurrentState() == AnimalState.DEAD && prey.shouldSpawnCarcass()) {
            // TẠO RA XÁC CHẾT (CARCASS) TẠI ĐÚNG VỊ TRÍ ĐÓ
            // Lượng thịt bằng chính năng lượng tối đa của con mồi
            Carcass meat = new Carcass(prey.getPosition(), prey.getSize(), prey.getMaxEnergy());
            newEntities.add(meat);
            
            System.out.println("Một con " + prey.getClass().getSimpleName() + " đã bị hạ gục! Rơi ra cục thịt.");
        }
    }

    // Xử lý ăn cỏ / nấm độc
    private static void handleEating(Herbivore herbivore, Eatable food) {

        if (food instanceof OldTree) {
            // Giả sử các loài có size >= 5.0 (như Voi, Hươu cao cổ) mới với tới lá cây
            if (herbivore.getSize() < 5.0) {
                return; 
                // Lùn quá với không tới, từ chối cho ăn, ép con vật đi tìm cỏ!
            }
            //Voi có thể ăn được lá cây lớn
            double leafGot = food.getEnergyValue();
            herbivore.setHp(herbivore.getHp() + leafGot * 0.5);
            herbivore.setEnergy(herbivore.getEnergy() + leafGot);
            food.getEaten();
            return;

        } else if (food instanceof SmallTree) { //Nếu là cây non thì ăn luôn
            if (herbivore instanceof Elephant) {
                // Voi có thể ăn được cây nhỏ
                double energyGot = food.getEnergyValue();
                herbivore.setHp(herbivore.getHp() + energyGot * 0.5); // Cỏ có thể hồi máu, nấm độc thì trừ máu (x0.5 để cân bằng)
                herbivore.setEnergy(herbivore.getEnergy() + energyGot);
                food.getEaten();
            }
            return;
        }

        double energyGot = food.getEnergyValue();

        // Nếu năng lượng bằng 0 (ví dụ bụi Berry vừa bị vặt hết quả), thì không làm gì cả
        if (energyGot == 0) return;

        // Cộng (hoặc trừ) năng lượng cho con vật
        herbivore.setHp(herbivore.getHp() + energyGot * 0.5); // Cỏ có thể hồi máu, nấm độc thì trừ máu (x0.5 để cân bằng)
        herbivore.setEnergy(herbivore.getEnergy() + energyGot);

        // Nấm độc (Mushroom) có energyValue bị âm. Vừa bị trừ năng lượng, vừa trừ máu luôn cho chân thực!
        if (food instanceof Mushroom) {
            herbivore.setHp(herbivore.getHp() - 20); // Trừ thẳng 20 máu
            System.out.println(herbivore.getClass().getSimpleName() + " ăn trúng nấm độc! Bị trừ máu.");
        }

        // Gọi hàm để cây biết nó vừa bị cắn (Cỏ thì chết, Berry thì mất trạng thái có quả)
        food.getEaten();
    }

    // Xử lý ăn Xác chết (Dành cho Sói hoặc Linh cẩu)
    private static void handleEatingCarcass(Carnivore carnivore, Carcass carcass) {
        // Mỗi lần chạm, cắn một miếng 15.0 năng lượng
        double bite = carcass.takeBite(15.0);
        carnivore.setEnergy(carnivore.getEnergy() + bite);
        carnivore.setCurrentState(AnimalState.EATING);
    }

    // Xử lý đụng tường / chui bụi rậm
    private static void handleObstacleCollision(Animal animal, Obstacle obstacle) {
        // Nếu chướng ngại vật là một chỗ trốn (Hideable) như Bush
        if (animal instanceof Elephant) return; // Voi thì không bị chướng ngại vật nào cản được, bỏ qua hết
        if (obstacle instanceof Hideable) {
            if (animal.getCurrentState() == AnimalState.HIDING) {
                return;
            }
            Hideable hideable = (Hideable) obstacle;

            if (animal.hasJustLeftBush()) {
                animal.setJustLeftBush(false);
                return;
            }
            
            // Nếu con vật chui vừa (ví dụ Cáo/Thỏ size <= 4.0 chui vừa Bush)
            // Tạm thời chỉ dể thỏ chui
            if (animal instanceof Rabbit && animal.getSize() <= hideable.getMaxAllowedSize()) {
                hideable.hideEntity(animal);
                System.out.println("RABBIT HIDING");
                long now = System.currentTimeMillis();

                if (now - lastBushSound > 500) {
                    SoundManager.playSound("bush_rustle.wav");
                    lastBushSound = now;
                }
                animal.setCurrentState(AnimalState.HIDING);
                animal.setHidingTicks(SimulationConstant.RABBIT_HIDE_DURATION); //may change later for universal
                EventManager.animalHide(animal.getClass().getSimpleName());
                // Gắn chặt vị trí con vật vào giữa bụi rậm và ép dừng lại
                animal.getPosition().setX(obstacle.getPosition().getX());
                animal.getPosition().setY(obstacle.getPosition().getY());
                animal.getVelocity().setX(0);
                animal.getVelocity().setY(0);
                return; // Xong, không xét đụng tường nữa
            }
        }

        // NẾU KHÔNG THỂ CHUI VÀO (Ví dụ: Sói quá to, hoặc đụng trúng Cây cổ thụ Tree)
        // Hiệu ứng dội ngược: Đảo ngược vận tốc để nảy ra
        animal.getVelocity().setX(animal.getVelocity().getX() * -1);
        animal.getVelocity().setY(animal.getVelocity().getY() * -1);
    }
}
