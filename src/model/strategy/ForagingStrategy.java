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
        // Tính toán phần trăm năng lượng hiện tại
        double energyPercentage = herbivore.getEnergy() / herbivore.getMaxEnergy();
        
        // Đổi mốc đói xuống 60%
        boolean hungry = energyPercentage < 0.60;
        
        // Chỉ bận tâm tìm thức ăn khi thực sự bắt đầu đói
        if (hungry) {
            boolean veryHungry = energyPercentage < 0.25;
            
            // CHỈ KHI RẤT ĐÓI (< 25%) mới được mở rộng khứu giác lên x1.25
            double searchRadius = veryHungry ? (herbivore.getVisionRadius() * 1.25) : herbivore.getVisionRadius();
            
            Entity food = findPlantInRadius(herbivore, searchRadius);
            
            if (food != null) {
                herbivore.setCurrentState(AnimalState.FORAGING);
                moveToward(herbivore, food, herbivore.getSpeed() * 0.65);
                return;
            }
        }
        
        // No rồi (> 60%), hoặc đói nhưng không thấy đồ ăn -> đi dạo / nhập bầy
        nextLogic.execute(herbivore);
    }

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