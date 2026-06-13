package model.strategy;

import model.Animal;
import model.AnimalState;
import model.Entity;
import model.Vector2D;
import model.environment.Environment;
import model.environment.Rectangle;
import model.plant.Plant;

import java.util.List;

public class ForagingStrategy implements SurvivalStrategy {
    private final SurvivalStrategy nextLogic;

    public ForagingStrategy() {
        this.nextLogic = new PassiveStrategy();
    }
    
    public ForagingStrategy(SurvivalStrategy customLogic) {
        this.nextLogic = customLogic;
    }

    @Override
    public void execute(Animal herbivore) {
        // --- 1. TÍNH TOÁN TRẠNG THÁI NĂNG LƯỢNG ĐÓI KHÁT ---
        double energyPercentage = herbivore.getEnergy() / herbivore.getMaxEnergy();
        
        // Cân bằng mốc bắt đầu tìm đồ ăn: Dưới 60% năng lượng
        boolean hungry = energyPercentage < 0.60;
        
        if (hungry) {
            // Kiểm tra xem có đang rơi vào trạng thái nguy kịch hay không (Dưới 25%)
            boolean veryHungry = energyPercentage < 0.25;
            
            // ĐỘC QUYỀN BẢN MỚI: Chỉ khi RẤT ĐÓI mới bộc phát khứu giác kích thích tầm quét lên x1.25 lần
            double searchRadius = veryHungry ? (herbivore.getVisionRadius() * 1.25) : herbivore.getVisionRadius();
            
            // Tìm kiếm thực vật gần nhất trong bán kính được tính toán qua QuadTree
            Entity food = findPlantInRadius(herbivore, searchRadius);
            
            if (food != null) {
                // Chuyển trạng thái sang kiếm ăn công khai
                herbivore.setCurrentState(AnimalState.FORAGING);
                
                // Di chuyển tới nguồn thức ăn với tốc độ vừa phải, điềm tĩnh (65% tốc độ tối đa)
                moveToward(herbivore, food, herbivore.getSpeed() * 0.65);
                return;
            }
        }
        
        // --- 2. CHUYỂN TIẾP LOGIC KHI NO HOẶC KHÔNG TÌM THẤY ĐỒ ĂN ---
        // Nếu năng lượng dồi dào (> 60%), hoặc đói nhưng radar không quét được cây nào -> Đi dạo / Nhập đàn
        nextLogic.execute(herbivore);
    }

    /**
     * Thuật toán tìm kiếm Thực vật (Plant) gần nhất sử dụng không gian QuadTree 
     * Kết hợp cơ chế quét đa tầng bán kính linh hoạt
     */
    private Entity findPlantInRadius(Animal herbivore, double radius) {
        Entity nearest = null;
        double minDistance = radius;

        // Tạo vùng quét hình chữ nhật bao quanh thực thể dựa theo bán kính radar tầm nhìn/khứu giác
        Rectangle searchRange = new Rectangle(
            herbivore.getPosition().getX(), herbivore.getPosition().getY(), 
            radius * 2, radius * 2
        );

        // Truy vấn nhanh các thực thể nằm trong vùng thông qua QuadTree
        List<Entity> nearbyEntities = Environment.getInstance().getQuadTree().query(searchRange, null);

        for (Entity entity : nearbyEntities) {
            // Kiểm tra: Thuộc lớp cây cối (Plant), cây còn sống/chưa bị ăn hết
            if (entity instanceof Plant && entity.isAlive()) {
                double distance = herbivore.getPosition().distanceTo(entity.getPosition());
                
                // Cập nhật thực thể thực vật gần nhất
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = entity;
                }
            }
        }
        return nearest;
    }

    /**
     * Điều hướng con vật hướng thẳng tới tọa độ của mục tiêu (Thức ăn)
     */
    private void moveToward(Animal animal, Entity target, double speed) {
        Vector2D direction = new Vector2D(
            target.getPosition().getX() - animal.getPosition().getX(), 
            target.getPosition().getY() - animal.getPosition().getY()
        );
        applyVelocity(animal, direction, speed);
    }

    /**
     * Chuẩn hóa Vector hướng toán học và áp đặt vận tốc di chuyển thực tế cho thực thể
     */
    private void applyVelocity(Animal animal, Vector2D direction, double speed) {
        direction.normalize(); // Biến đổi Vector về độ dài đơn vị = 1
        
        animal.getVelocity().setX(direction.getX() * speed);
        animal.getVelocity().setY(direction.getY() * speed);
    }
}