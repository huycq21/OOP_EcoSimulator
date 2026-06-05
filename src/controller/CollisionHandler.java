package controller;

import model.environment.*;
import model.environment.obstacle.Hideable;
import model.environment.obstacle.Obstacle;
import model.environment.obstacle.OldTree;
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

        // Fix Bán kính tìm kiếm: Phải đủ rộng để bắt được con to nhất game (Ví dụ Voi size 15)
        double maxEntitySize = 15.0; 

        for (Entity e1 : entities) {
            if (!e1.isAlive()) continue;

            double searchRadius = e1.getSize() / 2.0 + maxEntitySize; 
            
            Rectangle searchRange = new Rectangle(
                    e1.getPosition().getX(), 
                    e1.getPosition().getY(), 
                    searchRadius * 2, searchRadius * 2
            );

            List<Entity> nearbyEntities = qTree.query(searchRange, null);

            for (Entity e2 : nearbyEntities) {
                // BUG FIX 1: Thêm e1.getId() < e2.getId() để chống xử lý va chạm 2 lần!
                if (e1 != e2 && e2.isAlive() && e1.getId() < e2.getId()) {
                    
                    double distance = e1.getPosition().distanceTo(e2.getPosition());
                    double collisionRadius = (e1.getSize() + e2.getSize()) / 2.0;

                    if (distance < collisionRadius) {
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
        
        // --- CHỐNG ĐÈ LÊN NHAU TẤT CẢ CÁC LOÀI ĐỘNG VẬT ---
        if (e1 instanceof Animal && e2 instanceof Animal) {
            resolveOverlap(e1, e2);
        }
        
        // ==========================================
        // LAYER 1: VẬT LÝ (CHƯỚNG NGẠI VẬT) 
        // ==========================================

        if (e1 instanceof Animal && e2 instanceof Obstacle) {
            handleObstacleCollision((Animal) e1, (Obstacle) e2);
        } else if (e2 instanceof Animal && e1 instanceof Obstacle) {
            handleObstacleCollision((Animal) e2, (Obstacle) e1);
        }

        // ==========================================
        // LAYER 2: TƯƠNG TÁC SINH HỌC 
        // ==========================================
        
        // 1. Thú ăn thịt đụng Xác chết (Ưu tiên kiểm tra trước)
        if (e1 instanceof Carnivore && e2 instanceof Carcass) {
            handleEatingCarcass((Carnivore) e1, (Carcass) e2);
        } else if (e2 instanceof Carnivore && e1 instanceof Carcass) {
            handleEatingCarcass((Carnivore) e2, (Carcass) e1);
        }
        
        // 2. Động vật ăn cỏ đụng Eatable
        else if (e1 instanceof Herbivore && e2 instanceof Eatable && !(e2 instanceof Carcass)) {
            handleEating((Herbivore) e1, (Eatable) e2);
        } else if (e2 instanceof Herbivore && e1 instanceof Eatable && !(e1 instanceof Carcass)) {
            handleEating((Herbivore) e2, (Eatable) e1);
        }

        // 3. Hai thú ăn thịt đụng nhau (Turf War)
        else if (e1 instanceof Carnivore && e2 instanceof Carnivore) {
            handleTurfWar((Carnivore) e1, (Carnivore) e2, newEntities, env);
        }

        // 5. Thú ăn thịt (Apex/Thường) đụng Thú ăn cỏ
        else if (e1 instanceof Carnivore && e2 instanceof Herbivore) {
            handleCombat((Carnivore) e1, (Animal) e2, newEntities, env);
        } else if (e2 instanceof Carnivore && e1 instanceof Herbivore) {
            handleCombat((Carnivore) e2, (Animal) e1, newEntities, env);
        }
    }

    // --- CÁC HÀM XỬ LÝ CHI TIẾT ---
    private static void handleCombat(Carnivore predator, Animal prey, List<Entity> newEntities, Environment env) {
        // Xử lý cơ chế nấp lùm
        if (prey.getCurrentState() == AnimalState.HIDING) {
            if (predator.getSize() < 5) {
                // Cáo vẫn tấn công chay bình thường
                predator.attack(prey);
            } else {
                return; // Kẻ khác (kể cả Hổ/Gấu) không thấy mồi trong lùm
            }
        } else {
            // NẾU KHÔNG NẤP TRONG LÙM: Phân biệt Apex và Thường
            if (predator instanceof ApexEntity) {
                ApexEntity apex = (ApexEntity) predator;
                apex.attack(prey,env);
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
        if (animal instanceof Elephant) return; // Voi càn quét mọi thứ

        if (obstacle instanceof Hideable) {
            Hideable hideable = (Hideable) obstacle;
            
            // ĐIỀU KIỆN ĐỂ ĐƯỢC CHUI VÀO BỤI:
            // 1. Phải chui lọt (size)
            // 2. Bụi chưa đầy (chống kẹt)
            // 3. Quan trọng nhất: Con vật phải đang BỎ CHẠY (FLEEING) hoặc ĐÃ NẤP (HIDING)
            boolean isScared = (animal.getCurrentState() == AnimalState.FLEEING || animal.getCurrentState() == AnimalState.HIDING);
            
            if (animal.getSize() <= hideable.getMaxAllowedSize() && !hideable.isFull() && isScared) {
                
                // Tránh lỗi 1 con chiếm 2 slot nếu logic bị lặp
                // (Giả sử bạn đã thêm hàm getHiddenEntities() vào Hideable/Bush)
                if (!hideable.getHiddenEntities().contains(animal)) {
                    hideable.hideEntity(animal);
                }
                
                // Khóa chặt trạng thái và vị trí
                animal.setCurrentState(AnimalState.HIDING);
                animal.getPosition().setX(obstacle.getPosition().getX());
                animal.getPosition().setY(obstacle.getPosition().getY());
                animal.getVelocity().setX(0);
                animal.getVelocity().setY(0);
                return; // Đã trốn xong, kết thúc vật lý va chạm
            }
        }

        // NẾU KHÔNG THỂ CHUI VÀO (Do quá to, bụi đầy, hoặc CHỈ ĐANG ĐI DẠO NGANG QUA)
        // -> Chuyển sang logic đẩy dạt ra ngoài (trượt quanh mép cây)
        double dist = animal.getPosition().distanceTo(obstacle.getPosition());
        double minCollisionDist = (animal.getSize() + obstacle.getSize()) / 2.0;

        if (dist < minCollisionDist && dist > 0) {
            double overlap = minCollisionDist - dist;
            
            double dx = animal.getPosition().getX() - obstacle.getPosition().getX();
            double dy = animal.getPosition().getY() - obstacle.getPosition().getY();
            
            dx /= dist;
            dy /= dist;

            // Đẩy dạt ra ngoài
            animal.getPosition().setX(animal.getPosition().getX() + dx * overlap);
            animal.getPosition().setY(animal.getPosition().getY() + dy * overlap);
        }
    }

    // --- HÀM MỚI: XỬ LÝ TRANH GIÀNH GIỮA CÁC LOÀI ĂN THỊT ---
private static void handleTurfWar(Carnivore c1, Carnivore c2, List<Entity> newEntities, Environment env) {
        if(c1.getClass() == c2.getClass()) {
            // Nếu cùng loài, có thể bỏ qua va chạm hoặc chỉ đẩy nhau ra vật
            resolveOverlap(c1, c2);
            return;
        }

        boolean c1Attacking = (c1.getCurrentState() == AnimalState.ATTACKING);
        boolean c2Attacking = (c2.getCurrentState() == AnimalState.ATTACKING);

        // 1. NẾU CẢ 2 KHÔNG AI CÓ Ý ĐỊNH ĐÁNH NHAU
        // (Ví dụ: Cả 2 cùng đang đi dạo ngang qua, hoặc cùng đang chạy trốn kẻ khác)
        if (!c1Attacking && !c2Attacking) {
            // Không gây sát thương, chỉ đẩy nhau ra vật lý để khỏi xuyên thấu
            resolveOverlap(c1, c2);
            return;
        }

        // 2. CƠ CHẾ PHẢN XẠ TỰ VỆ (AGGRO)
        // Nếu 1 con cắn, con kia dù đang ăn xác hay đi dạo cũng phải bừng tỉnh và cắn lại!
        if (c1Attacking && !c2Attacking) {
            // Con c2 nếu đang bỏ chạy (FLEEING) thì chỉ lo chạy, không cắn lại.
            // Nhưng nếu nó đang làm việc khác (WANDERING, EATING...), nó bị chọc điên và tự vệ!
            if (c2.getCurrentState() != AnimalState.FLEEING) {
                c2.setCurrentState(AnimalState.ATTACKING);
                c2Attacking = true;
            }
        } else if (c2Attacking && !c1Attacking) {
            if (c1.getCurrentState() != AnimalState.FLEEING) {
                c1.setCurrentState(AnimalState.ATTACKING);
                c1Attacking = true;
            }
        }

        // 3. THỰC THI SÁT THƯƠNG KHI STATE LÀ ATTACKING
        // Ai đang bật Mode đánh nhau thì người đó mới vung tay cắn
        if (c1Attacking) {
            executeAttack(c1, c2, env);
        }
        if (c2Attacking) {
            executeAttack(c2, c1, env);
        }

        // 4. KIỂM TRA CHẾT VÀ TÁCH VẬT LÝ
        checkDeathAndDropCarcass(c1, c2, newEntities);
        checkDeathAndDropCarcass(c2, c1, newEntities);
        
        resolveOverlap(c1, c2); // Xô nhau văng ra một chút, không cho đè lên nhau
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

    // Chống đè lên nhau
    private static void resolveOverlap(Entity e1, Entity e2) {
        double dist = e1.getPosition().distanceTo(e2.getPosition());
        double minCollisionDist = (e1.getSize() + e2.getSize()) / 2.0;

        // XỬ LÝ LỖI TRÙNG KHÍT (Khoảng cách = 0)
        if (dist == 0) {
            // Đẩy nhẹ e1 một khoảng siêu nhỏ (vd: 0.3) sang phải hoặc ngẫu nhiên
            // Mục đích chỉ là để tạo ra dist > 0, sau đó vòng lặp vật lý sẽ tự lo phần còn lại
            e1.getPosition().setX(e1.getPosition().getX() + 0.2);
            e1.getPosition().setY(e1.getPosition().getY() + 0.2);
            
            // Cập nhật lại khoảng cách sau khi đã "lệch" ra
            dist = e1.getPosition().distanceTo(e2.getPosition()); 
        }

        // Xử lý đẩy lùi (Lúc này dist chắc chắn > 0)
        if (dist < minCollisionDist) {
            double overlap = minCollisionDist - dist; // Độ lún (pixel)

            // Vector hướng từ e2 sang e1
            double dx = e1.getPosition().getX() - e2.getPosition().getX();
            double dy = e1.getPosition().getY() - e2.getPosition().getY();
            
            // Chuẩn hóa vector
            dx /= dist;
            dy /= dist;

            // Đẩy mỗi con lùi lại một nửa độ lún (để chúng vừa khít chạm nhau)
            e1.getPosition().setX(e1.getPosition().getX() + dx * (overlap / 2.0));
            e1.getPosition().setY(e1.getPosition().getY() + dy * (overlap / 2.0));

            e2.getPosition().setX(e2.getPosition().getX() - dx * (overlap / 2.0));
            e2.getPosition().setY(e2.getPosition().getY() - dy * (overlap / 2.0));
        }
    }
}
