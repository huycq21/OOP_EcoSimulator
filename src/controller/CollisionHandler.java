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
import util.SoundManager;

public class CollisionHandler {

    private static long lastBushSound = 0;
    
    public static void processCollisions(Environment env) {
        List<Entity> entities = env.getEntities();
        List<Entity> newEntities = new ArrayList<>();

        QuadTree qTree = env.getQuadTree();

        // Fix Bán kính tìm kiếm: Phải đủ rộng để bắt được con to nhất game (Ví dụ Voi size 15)
        double maxEntityRadius = 9.0; 

        for (Entity e1 : entities) {
            if (!e1.isAlive()) continue;

            double searchRadius = CollisionProfile.bodyRadius(e1) + maxEntityRadius;
            
            Rectangle searchRange = new Rectangle(
                    e1.getPosition().getX(), 
                    e1.getPosition().getY(), 
                    searchRadius * 2, searchRadius * 2
            );

            List<Entity> nearbyEntities = qTree.query(searchRange, null);

            for (Entity e2 : nearbyEntities) {
                // BUG FIX 1: Chống xử lý va chạm 2 lần bằng cách so sánh Id!
                if (e1 != e2 && e2.isAlive() && e1.getId() < e2.getId()) {
                    
                    double distance = e1.getPosition().distanceTo(e2.getPosition());
                    double collisionRadius = CollisionProfile.bodyRadius(e1) + CollisionProfile.bodyRadius(e2);

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
    private static void resolveCollision(Entity e1, Entity e2, List<Entity> newEntities, Environment env) {
        
        // ==========================================
        // LAYER 1: VẬT LÝ (CHƯỚNG NGẠI VẬT) 
        // ==========================================
        if (e1 instanceof Animal && e2 instanceof Obstacle) {
            handleObstacleCollision((Animal) e1, (Obstacle) e2);
            return; // Đã chạm vật cản vật lý thì không xử lý chồng lấn sinh học ở frame này
        } else if (e2 instanceof Animal && e1 instanceof Obstacle) {
            handleObstacleCollision((Animal) e2, (Obstacle) e1);
            return;
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
        
        // 2. Động vật ăn cỏ đụng Eatable (Cỏ, quả bụi, cây cối...)
        else if (e1 instanceof Herbivore && e2 instanceof Eatable && !(e2 instanceof Carcass)) {
            handleEating((Herbivore) e1, (Eatable) e2);
        } else if (e2 instanceof Herbivore && e1 instanceof Eatable && !(e1 instanceof Carcass)) {
            handleEating((Herbivore) e2, (Eatable) e1);
        }

        // 3. Hai thú ăn thịt đụng nhau (Turf War - Tranh giành lãnh thổ)
        else if (e1 instanceof Carnivore && e2 instanceof Carnivore) {
            handleTurfWar((Carnivore) e1, (Carnivore) e2, newEntities, env);
        }

        // 4. Thú ăn thịt đụng Thú ăn cỏ (Săn bắt chiến đấu)
        else if (e1 instanceof Carnivore && e2 instanceof Herbivore) {
            handleCombat((Carnivore) e1, (Animal) e2, newEntities, env);
        } else if (e2 instanceof Carnivore && e1 instanceof Herbivore) {
            handleCombat((Carnivore) e2, (Animal) e1, newEntities, env);
        }

        // 5. Chống đè lên nhau giữa tất cả các loài động vật còn lại (Wander đụng nhau)
        else if (e1 instanceof Animal && e2 instanceof Animal) {
            resolveOverlap(e1, e2);
        }
    }

    // --- CÁC HÀM XỬ LÝ CHI TIẾT ---
    
    // Xử lý săn bắt và rơi ra Xác chết (Kết hợp cơ chế nấp lùm, cắn chí mạng của Apex)
    private static void handleCombat(Carnivore predator, Animal prey, List<Entity> newEntities, Environment env) {
        if (prey.getCurrentState() == AnimalState.DEAD) return;
        if (!predator.canAttack(prey)) return;

        // Xử lý cơ chế nấp lùm
        if (prey.getCurrentState() == AnimalState.HIDING) {
            if (predator.getSize() < 5.0) {
                // Cáo nhỏ vẫn lẻn vào tấn công chay bình thường
                predator.attack(prey);
            } else {
                return; // Kẻ săn mồi lớn hơn (Sói/Hổ/Gấu) không nhìn thấy mồi trong lùm bụi
            }
        } else {
            // NẾU KHÔNG NẤP TRONG LÙM: Phân biệt Apex (Thú đầu bảng quăng chiêu) và Thường
            if (predator instanceof ApexEntity) {
                ((ApexEntity) predator).attack(prey, env);
            } else {
                predator.attack(prey); // Thú ăn thịt thông thường cắn chay
            }
        }

        // KIỂM TRA MÁU VÀ RỚT XÁC (Áp dụng chung chống rơi 2 lần)
        if (prey.getHp() <= 0 && prey.isAlive()) { 
            prey.destroy(); // Chuyển cờ isAlive = false
            
            // Tạo ra cục thịt (Carcass) lưu giữ class của con mồi
            Carcass meat = new Carcass(prey.getPosition(), prey.getSize(), prey.getMaxEnergy(), prey.getClass());
            newEntities.add(meat);
            
            System.out.println("CẢNH BÁO TỬ VONG: " + prey.getClass().getSimpleName() 
                               + " đã bị hạ gục bởi " + predator.getClass().getSimpleName() + "! Rơi ra lượng thịt: " + meat.getEnergyValue());
        }
    }

    // Xử lý ăn cỏ / cây lớn / nấm độc (Bảo lưu toàn bộ logic phân cấp chiều cao từ bản cũ)
    private static void handleEating(Herbivore herbivore, Eatable food) {
        if (food instanceof OldTree) {
            // Giả sử các loài có size >= 5.0 (như Voi, Hươu cao cổ) mới với tới lá cây cổ thụ
            if (herbivore.getSize() < 5.0) {
                return; // Lùn quá với không tới, ép con vật đi tìm cỏ dưới đất!
            }
            double leafGot = food.getEnergyValue();
            herbivore.setHp(herbivore.getHp() + leafGot * 0.5);
            herbivore.setEnergy(herbivore.getEnergy() + leafGot);
            food.getEaten();
            return;

        } else if (food instanceof SmallTree) { 
            // Nếu là cây non thì chỉ có loài to như Voi càn quét ăn được
            if (herbivore instanceof Elephant) {
                double energyGot = food.getEnergyValue();
                herbivore.setHp(herbivore.getHp() + energyGot * 0.5); 
                herbivore.setEnergy(herbivore.getEnergy() + energyGot);
                food.getEaten();
            }
            return;
        }

        // Thực vật thông thường (Cỏ, Bụi Berry, Nấm)
        double energyGot = food.getEnergyValue();
        if (energyGot == 0) return; // Quả đã bị vặt hết, không ăn được nữa

        // Cộng năng lượng và hồi máu
        herbivore.setHp(herbivore.getHp() + energyGot * 0.5); 
        herbivore.setEnergy(herbivore.getEnergy() + energyGot);

        // Nấm độc (Mushroom) có energyValue bị âm -> Trừ năng lượng và phạt thêm 20 máu
        if (food instanceof Mushroom) {
            herbivore.setHp(herbivore.getHp() - 20); 
            System.out.println(herbivore.getClass().getSimpleName() + " ăn trúng nấm độc! Bị trừ máu.");
        }

        food.getEaten(); // Thông báo thực vật đã bị ăn để cập nhật trạng thái/biến mất
    }

    // Xử lý ăn Xác chết (Áp dụng cho cả Carnivore thường và Apex)
    private static void handleEatingCarcass(Carnivore carnivore, Carcass carcass) {
        if (carcass.getEnergyValue() <= 0) return;
        // Mỗi lần chạm, cắn một miếng năng lượng bằng đúng lực sát thương (Attack Damage) của loài đó
        double bite = carcass.takeBite(carnivore.getAttackDamage());
        carnivore.setEnergy(carnivore.getEnergy() + bite);
        carnivore.setCurrentState(AnimalState.EATING);
    }

    // Xử lý đụng tường / chui bụi rậm trốn thoát (Sử dụng giải pháp đẩy trượt quanh mép của bản mới)
    private static void handleObstacleCollision(Animal animal, Obstacle obstacle) {
        // Voi chiến càn quét mọi thứ, xem chướng ngại vật như không khí
        if (animal instanceof Elephant) return; 

        if (obstacle instanceof Hideable) {
            if (animal.getCurrentState() == AnimalState.HIDING) return;

            Hideable hideable = (Hideable) obstacle;

            // Cooldown chống lỗi vừa thoát khỏi bụi lại tự động chui ngược vào lại
            if (animal.hasJustLeftBush()) {
                animal.setJustLeftBush(false);
                return;
            }
            
            // Điều kiện khắt khe: Phải đang sợ hãi bỏ chạy (FLEEING) mới kích hoạt ẩn nấp
            boolean isScared = (animal.getCurrentState() == AnimalState.FLEEING);
            
            if (animal.getSize() <= hideable.getMaxAllowedSize() && !hideable.isFull() && isScared) {
                // Chống lỗi 1 thực thể đăng ký chiếm nhiều slot trong 1 bụi rậm
                if (!hideable.getHiddenEntities().contains(animal)) {
                    hideable.hideEntity(animal);
                    long now = System.currentTimeMillis();

                    if (now - lastBushSound > 1000) {
                        SoundManager.playSound("bush_rustle.wav");
                        lastBushSound = now;
                    }
                    System.out.println(animal.getClass().getSimpleName() + " HIDING");
                    animal.setHidingTicks(SimulationConstant.RABBIT_HIDE_DURATION); 
                    EventManager.animalHide(animal.getClass().getSimpleName());
                }
                
                // Khóa chặt trạng thái vật lý tại tâm bụi rậm
                animal.setCurrentState(AnimalState.HIDING);
                animal.getPosition().setX(obstacle.getPosition().getX());
                animal.getPosition().setY(obstacle.getPosition().getY());
                animal.getVelocity().setX(0);
                animal.getVelocity().setY(0);
                return; 
            }
        }

        // NẾU KHÔNG THỂ CHUI VÀO (Do quá to, bụi đầy, hoặc chỉ đang đi dạo)
        // -> Sử dụng cơ chế TRƯỢT QUANH MÉP VẬT CẢN (Mượt mà hơn phản xạ nảy dội ngược cũ)
        double dist = animal.getPosition().distanceTo(obstacle.getPosition());
        double minCollisionDist = CollisionProfile.bodyRadius(animal) + CollisionProfile.bodyRadius(obstacle);

        if (dist < minCollisionDist && dist > 0) {
            double overlap = minCollisionDist - dist;
            
            double dx = animal.getPosition().getX() - obstacle.getPosition().getX();
            double dy = animal.getPosition().getY() - obstacle.getPosition().getY();
            
            dx /= dist;
            dy /= dist;

            // Đẩy dạt tịnh tiến ra ngoài rìa chướng ngại vật
            animal.getPosition().setX(animal.getPosition().getX() + dx * overlap);
            animal.getPosition().setY(animal.getPosition().getY() + dy * overlap);
        }
    }

    // --- XỬ LÝ TRANH GIÀNH GIỮA CÁC LOÀI ĂN THỊT (TURF WAR) ---
    private static void handleTurfWar(Carnivore c1, Carnivore c2, List<Entity> newEntities, Environment env) {
        if (c1.getClass() == c2.getClass()) {
            // Nếu cùng loài (ví dụ 2 con Sói), đẩy nhau ra bằng tương tác vật lý thông thường
            resolveOverlap(c1, c2);
            return;
        }

        boolean c1Attacking = (c1.getCurrentState() == AnimalState.ATTACKING);
        boolean c2Attacking = (c2.getCurrentState() == AnimalState.ATTACKING);

        // 1. Nếu cả 2 đều hiền hòa (đang đi dạo hoặc cùng tháo chạy kẻ khác) -> chỉ đẩy nhau ra
        if (!c1Attacking && !c2Attacking) {
            resolveOverlap(c1, c2);
            return;
        }

        // 2. Cơ chế phản xạ tự vệ (AGGRO): Một con chủ động cắn, con kia buộc phải đánh trả tự vệ
        if (c1Attacking && !c2Attacking) {
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

        // 3. Thực thi gây sát thương dựa trên trạng thái ATTACKING hiện tại
        if (c1Attacking) {
            executeAttack(c1, c2, env);
        }
        if (c2Attacking) {
            executeAttack(c2, c1, env);
        }

        // 4. Kiểm tra tử vong và tách rời vật lý chống dính nhau
        checkDeathAndDropCarcass(c1, c2, newEntities);
        checkDeathAndDropCarcass(c2, c1, newEntities);
        
        resolveOverlap(c1, c2); 
    }

    // Hàm bổ trợ: Điều phối đòn đánh (Cắn chay hoặc xài kĩ năng diện rộng của Apex)
    private static void executeAttack(Carnivore attacker, Animal victim, Environment env) {
        if (attacker instanceof ApexEntity) {
            ((ApexEntity) attacker).attack(victim, env);
        } else {
            attacker.attack(victim);
        }
    }

    // Hàm bổ trợ: Kiểm tra tử chiến thú ăn thịt và tạo đống thịt (Carcass) tương ứng
    private static void checkDeathAndDropCarcass(Carnivore killer, Animal victim, List<Entity> newEntities) {
        if (victim.getHp() <= 0 && victim.isAlive()) {
            victim.destroy();
            Carcass meat = new Carcass(victim.getPosition(), victim.getSize(), victim.getMaxEnergy(), victim.getClass());
            newEntities.add(meat);
            System.out.println("TỬ CHIẾN THÚ ĂN THỊT: " + victim.getClass().getSimpleName() 
                               + " đã bị xé xác bởi " + killer.getClass().getSimpleName() + "!");
        }
    }

    // Thuật toán chống đè/xuyên thấu vật lý giữa các thực thể động vật
    private static void resolveOverlap(Entity e1, Entity e2) {
        double dist = e1.getPosition().distanceTo(e2.getPosition());
        double minCollisionDist = CollisionProfile.bodyRadius(e1) + CollisionProfile.bodyRadius(e2);

        // Xử lý lỗi trùng khít tuyệt đối (Tọa độ X, Y bằng nhau hoàn toàn làm Khoảng cách = 0)
        if (dist == 0) {
            e1.getPosition().setX(e1.getPosition().getX() + 0.1);
            e1.getPosition().setY(e1.getPosition().getY() + 0.1);
            dist = e1.getPosition().distanceTo(e2.getPosition()); 
        }

        // Đẩy lùi hai thực thể theo tỉ lệ công bằng (mỗi con chịu một nửa độ lún)
        if (dist < minCollisionDist) {
            double overlap = minCollisionDist - dist; 

            double dx = e1.getPosition().getX() - e2.getPosition().getX();
            double dy = e1.getPosition().getY() - e2.getPosition().getY();
            
            dx /= dist;
            dy /= dist;

            e1.getPosition().setX(e1.getPosition().getX() + dx * (overlap / 2.0));
            e1.getPosition().setY(e1.getPosition().getY() + dy * (overlap / 2.0));

            e2.getPosition().setX(e2.getPosition().getX() - dx * (overlap / 2.0));
            e2.getPosition().setY(e2.getPosition().getY() - dy * (overlap / 2.0));
        }
    }
}