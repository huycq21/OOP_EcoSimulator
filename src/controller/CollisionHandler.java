package controller;

import model.environment.*;
import model.*;
import model.carnivore.*;
import model.herbivore.*;
import model.plant.*;
import java.util.List;

import apple.laf.JRSUIUtils.Tree;

import java.util.ArrayList;

public class CollisionHandler {

    // Hàm chính được gọi mỗi khung hình để kiểm tra toàn bộ thực thể
    public static void processCollisions(Environment env) {
        List<Entity> entities = env.getEntities();
        
        // Danh sách lưu trữ các thực thể mới sinh ra (ví dụ: Xác chết)
        // Phải dùng danh sách tạm để tránh lỗi khi đang lặp qua mảng chính
        List<Entity> newEntities = new ArrayList<>();

        // Thuật toán quét va chạm (O(N^2)) - So sánh từng cặp với nhau
        for (int i = 0; i < entities.size(); i++) {
            Entity e1 = entities.get(i);
            if (!e1.isAlive()) continue;

            for (int j = i + 1; j < entities.size(); j++) {
                Entity e2 = entities.get(j);
                if (!e2.isAlive()) continue;

                // Tính khoảng cách giữa 2 tâm của vật thể
                double distance = e1.getPosition().distanceTo(e2.getPosition());
                
                // Khoảng cách va chạm = Tổng 2 bán kính (size / 2)
                double collisionRadius = (e1.getSize() + e2.getSize()) / 2.0;

                // NẾU CHẠM NHAU!
                if (distance < collisionRadius) {
                    resolveCollision(e1, e2, newEntities);
                }
            }
        }

        // Thêm các thực thể mới (như Carcass) vào bản đồ
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

        // 2. TRƯỜNG HỢP: Động vật ăn cỏ đụng Thực vật (Ăn uống)
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

        // Thú ăn thịt gọi hàm cắn (có tính toán Cooldown ở bên trong class Carnivore)
        predator.attack(prey);

        // Nếu con mồi cạn máu
        if (prey.getHp() <= 0) {
            prey.destroy(); // Chết!
            
            // TẠO RA XÁC CHẾT (CARCASS) TẠI ĐÚNG VỊ TRÍ ĐÓ
            // Lượng thịt bằng chính năng lượng tối đa của con mồi
            Carcass meat = new Carcass(prey.getPosition(), prey.getSize(), prey.getMaxEnergy());
            newEntities.add(meat);
            
            System.out.println("Một con " + prey.getClass().getSimpleName() + " đã bị hạ gục! Rơi ra cục thịt.");
        }
    }

    // Xử lý ăn cỏ / nấm độc
    private static void handleEatingPlant(Herbivore herbivore, Eatable food) {

        if (food instanceof OldTree) {
            // Giả sử các loài có size >= 5.0 (như Voi, Hươu cao cổ) mới với tới lá cây
            if (herbivore.getSize() < 5.0) {
                return; // Lùn quá với không tới, từ chối cho ăn, ép con vật đi tìm cỏ!
            }
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
}