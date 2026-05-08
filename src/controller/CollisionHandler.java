package controller;

import model.environment.*;
import model.*;
import model.apex.ApexEntity;
import model.carnivore.*;
import model.herbivore.*;
import model.plant.*;
import java.util.List;
import java.util.ArrayList;

public class CollisionHandler {

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
                        // CHÚ Ý 1: Truyền thêm 'env' vào đây
                        resolveCollision(e1, e2, newEntities, env);
                    }
                }
            }
        }
        for (Entity newEntity : newEntities) {
            env.addEntity(newEntity);
        }
    }

    // --- PHÂN LOẠI VÀ XỬ LÝ TỪNG TRƯỜNG HỢP ---
    // CHÚ Ý 2: Thêm tham số Environment env
    private static void resolveCollision(Entity e1, Entity e2, List<Entity> newEntities, Environment env) {
        
        // 1. TRƯỜNG HỢP: ApexEntity đụng độ Thú ăn thịt thường (Ví dụ: Hổ vs Sói/Cáo)
        if (e1 instanceof ApexEntity && e2 instanceof Carnivore && !(e2 instanceof ApexEntity)) {
            handleCombat((Carnivore) e1, (Animal) e2, newEntities, env);
        } else if (e2 instanceof ApexEntity && e1 instanceof Carnivore && !(e1 instanceof ApexEntity)) {
            handleCombat((Carnivore) e2, (Animal) e1, newEntities, env);
        }

        // 2. TRƯỜNG HỢP: Động vật ăn thịt (bao gồm cả Apex) đụng Động vật ăn cỏ
        else if (e1 instanceof Carnivore && e2 instanceof Herbivore) {
            handleCombat((Carnivore) e1, (Animal) e2, newEntities, env);
        } else if (e2 instanceof Carnivore && e1 instanceof Herbivore) {
            handleCombat((Carnivore) e2, (Animal) e1, newEntities, env);
        }

        // 3. TRƯỜNG HỢP: Động vật ăn cỏ đụng Eatable
        else if (e1 instanceof Herbivore && e2 instanceof Eatable && !(e2 instanceof Carcass)) {
            handleEating((Herbivore) e1, (Eatable) e2);
        } else if (e2 instanceof Herbivore && e1 instanceof Eatable && !(e1 instanceof Carcass)) {
            handleEating((Herbivore) e2, (Eatable) e1);
        }

        // 4. TRƯỜNG HỢP: Động vật đụng Bụi rậm / Chướng ngại vật
        else if (e1 instanceof Animal && e2 instanceof Obstacle) {
            handleObstacleCollision((Animal) e1, (Obstacle) e2);
        } else if (e2 instanceof Animal && e1 instanceof Obstacle) {
            handleObstacleCollision((Animal) e2, (Obstacle) e1);
        }

        // 5. TRƯỜNG HỢP: Thú ăn thịt đụng Xác chết (Ăn xác)
        else if (e1 instanceof Carnivore && e2 instanceof Carcass) {
            handleEatingCarcass((Carnivore) e1, (Carcass) e2);
        } else if (e2 instanceof Carnivore && e1 instanceof Carcass) {
            handleEatingCarcass((Carnivore) e2, (Carcass) e1);
        }
        // --- LUẬT MỚI: 2 THÚ ĂN THỊT ĐỤNG NHAU (TRANH GIÀNH) ---
        if (e1 instanceof Carnivore && e2 instanceof Carnivore) {
            handleTurfWar((Carnivore) e1, (Carnivore) e2, newEntities, env);
        }
    }

    // --- CÁC HÀM XỬ LÝ CHI TIẾT ---

    // Đã đổi Herbivore thành Animal để Predator có thể thịt cả Carnivore nhỏ hơn
    private static void handleCombat(Carnivore predator, Animal prey, List<Entity> newEntities, Environment env) {
        // Xử lý cơ chế nấp lùm
        if (prey.getCurrentState() == AnimalState.HIDING) {
            if (predator instanceof Fox) {
                // Cáo vẫn tấn công chay bình thường
                predator.attack(prey);
            } else {
                return; // Kẻ khác (kể cả Hổ/Gấu) không thấy mồi trong lùm
            }
        } else {
            // NẾU KHÔNG NẤP TRONG LÙM: Phân biệt Apex và Thường
            if (predator instanceof ApexEntity) {
                ApexEntity apex = (ApexEntity) predator;
                
                // Nếu chiêu đặc biệt đã hồi xong
                if (apex.getCurrentCooldownTimer() <= 0) {
                    apex.performSpecialAbility(env);
                } else {
                    // Đang hồi chiêu thì phải cắn chay
                    apex.attack(prey);
                }
            } else {
                // Thú ăn thịt thông thường cắn chay
                predator.attack(prey);
            }
        }

        // KIỂM TRA MÁU VÀ RỚT XÁC (Áp dụng chung)
        if (prey.getHp() <= 0 && prey.isAlive()) { // Thêm check isAlive để tránh rơi xác 2 lần
            prey.destroy(); // Chuyển cờ isAlive = false
            
            Carcass meat = new Carcass(prey.getPosition(), prey.getSize(), prey.getMaxEnergy(), prey.getClass());
            newEntities.add(meat);
            
            System.out.println("CẢNH BÁO TỬ VONG: " + prey.getClass().getSimpleName() 
                               + " đã bị hạ gục bởi " + predator.getClass().getSimpleName() + "! Rơi ra lượng thịt: " + meat.getEnergyValue());
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

    // Xử lý ăn Xác chết (Áp dụng cho cả Carnivore thường và Apex)
    private static void handleEatingCarcass(Carnivore carnivore, Carcass carcass) {
        // Mỗi lần chạm, cắn một miếng năng lượng bằng sát thương của Carnivore
        double bite = carcass.takeBite(carnivore.getAttackDamage());
        carnivore.setEnergy(carnivore.getEnergy() + bite);
        carnivore.setCurrentState(AnimalState.EATING);
    }

    // Xử lý đụng tường / chui bụi rậm
    private static void handleObstacleCollision(Animal animal, Obstacle obstacle) {
        // Nếu chướng ngại vật là một chỗ trốn (Hideable) như Bush
        if (animal instanceof Elephant) return; // Voi thì không bị chướng ngại vật nào cản được, bỏ qua hết
        if (obstacle instanceof Hideable) {
            Hideable hideable = (Hideable) obstacle;
            
            // Nếu con vật chui vừa (ví dụ Cáo/Thỏ size <= 4.0 chui vừa Bush)
            if (animal.getSize() <= hideable.getMaxAllowedSize()) {
                hideable.hideEntity(animal);
                animal.setCurrentState(AnimalState.HIDING);
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

    // --- HÀM MỚI: XỬ LÝ TRANH GIÀNH GIỮA CÁC LOÀI ĂN THỊT ---
    private static void handleTurfWar(Carnivore c1, Carnivore c2, List<Entity> newEntities, Environment env) {
        // Lấy sức mạnh thực tế (đã bao gồm bầy đàn)
        double str1 = c1.getEffectiveStrength(env);
        double str2 = c2.getEffectiveStrength(env);

        // Phân loại Kẻ mạnh (Dominant) và Kẻ yếu (Submissive)
        Carnivore dominant = str1 >= str2 ? c1 : c2;
        Carnivore submissive = str1 >= str2 ? c2 : c1;

        // Nếu kẻ mạnh áp đảo hoàn toàn kẻ yếu (ví dụ: Sức mạnh chênh lệch hơn 1.3 lần)
        if (dominant.getEffectiveStrength(env) > submissive.getEffectiveStrength(env) * 1.3) {
            
            // KẺ YẾU KIỂM TRA ĐỘ ĐÓI
            if (submissive.isStarving()) {
                // Chó cùng dứt giậu! Đói quá rồi, liều mạng cắn trả!
                System.out.println(submissive.getClass().getSimpleName() + " đang đói khát! Nó liều mạng tấn công " + dominant.getClass().getSimpleName() + "!");
                
                // Cả 2 cùng tấn công nhau (Dùng lại logic đánh nhau của bạn)
                executeAttack(submissive, dominant, env);
                executeAttack(dominant, submissive, env);
                
            } else {
                // Chưa đến mức chết đói -> Sợ hãi, nhường đồ ăn và BỎ CHẠY
                submissive.setCurrentState(AnimalState.FLEEING); // Chuyển state để né
                
                // Toán học Vector: Kẻ yếu nảy ngược/chạy trốn ra xa khỏi kẻ mạnh
                double dx = submissive.getPosition().getX() - dominant.getPosition().getX();
                double dy = submissive.getPosition().getY() - dominant.getPosition().getY();
                Vector2D fleeVector = new Vector2D(dx, dy);
                fleeVector.normalize();
                
                submissive.getVelocity().setX(fleeVector.getX() * submissive.getSpeed());
                submissive.getVelocity().setY(fleeVector.getY() * submissive.getSpeed());
                
                // Kẻ mạnh có thể cắn với 1 cái làm cảnh cáo (tùy bạn)
                executeAttack(dominant, submissive, env); 
            }
        } 
        else {
            // Sức mạnh ngang ngửa nhau (ví dụ: Bầy Linh cẩu đụng độ Hổ đơn độc) -> TỬ CHIẾN!
            executeAttack(c1, c2, env);
            executeAttack(c2, c1, env);
        }

        // Kiểm tra xem có đứa nào chết sau pha va chạm không để rơi thịt
        checkDeathAndDropCarcass(c1, c2, newEntities);
        checkDeathAndDropCarcass(c2, c1, newEntities);
    }
    // --- HÀM HỖ TRỢ ĐỂ TÁI SỬ DỤNG CODE TẤN CÔNG (DÙNG CHO CẢ APEX VÀ THƯỜNG) ---
    private static void executeAttack(Carnivore attacker, Animal victim, Environment env) {
        if (attacker instanceof ApexEntity) {
            // ApexEntity giờ đã tự biết khi nào quăng chiêu, khi nào cắn chay!
            ((ApexEntity) attacker).attack(victim, env);
        } else {
            // Carnivore thường cắn chay
            attacker.attack(victim);
        }
    }

    // --- HÀM HỖ TRỢ KIỂM TRA CHẾT VÀ RỚT XÁC ---
    private static void checkDeathAndDropCarcass(Carnivore killer, Animal victim, List<Entity> newEntities) {
        if (victim.getHp() <= 0 && victim.isAlive()) {
            victim.destroy();
            Carcass meat = new Carcass(victim.getPosition(), victim.getSize(), victim.getMaxEnergy(), victim.getClass());
            newEntities.add(meat);
            System.out.println("TỬ CHIẾN THÚ ĂN THỊT: " + victim.getClass().getSimpleName() 
                               + " đã bị xé xác bởi " + killer.getClass().getSimpleName() + "!");
        }
    }
}