package model.strategy;

import java.util.List;

import model.Animal;
import model.AnimalState;
import model.Entity;
import model.Vector2D;
import model.carnivore.Carnivore;
import model.herbivore.*;
import model.environment.Environment; 
import model.environment.*;
import model.environment.Rectangle;
import model.environment.obstacle.*;

public class ScaredStrategy implements SurvivalStrategy {
    private SurvivalStrategy nextLogic;

    public ScaredStrategy() {
        this.nextLogic = new PassiveStrategy();
    }

    public ScaredStrategy(SurvivalStrategy nextLogic) {
        this.nextLogic = nextLogic;
    }

    @Override
    public void execute(Animal prey) {
        Entity threat = findActualThreat(prey); 

        if (threat == null) {
            nextLogic.execute(prey);
            return;
        }

        double distanceToThreat = prey.getPosition().distanceTo(threat.getPosition());
        
        // 1. CHỮA BỆNH HARDCODE KHOẢNG CÁCH HOẢNG LOẠN
        // Đổi 25.0 thành khoảng cách va chạm thực tế (Ví dụ: Tổng bán kính 2 con + 5 pixel an toàn)
        double panicDistance = prey.getSize() + threat.getSize() + 5.0; 
        boolean isPanicking = distanceToThreat < panicDistance; 

        // 2. LOGIC NẤP TRONG BỤI 
        if (prey.getCurrentState() == AnimalState.HIDING) {
            // Giả sử quy tắc chung của hệ sinh thái: Bụi rậm chỉ cho phép size <= 5.0 chui vào
            boolean predatorCanEnter = threat.getSize() <= 5.0; 

            // Chỉ hoảng loạn chạy ra NẾU kẻ thù đủ nhỏ để chui vào bụi bắt mình
            if (isPanicking && predatorCanEnter) {
                prey.setCurrentState(AnimalState.FLEEING);
            } else {
                // Kẻ thù to xác (ví dụ Sói size 8.0) thì cứ ung dung nằm khinh bỉ nó
                prey.getVelocity().setX(0);
                prey.getVelocity().setY(0);
                return;
            }
        }

        prey.setCurrentState(AnimalState.FLEEING);
        Vector2D fleeDir = new Vector2D(
            prey.getPosition().getX() - threat.getPosition().getX(),
            prey.getPosition().getY() - threat.getPosition().getY()
        );
        fleeDir.normalize();

        if (!isPanicking && prey.getSize() <= 5.0) {
            // Lọc bụi rậm thông minh nằm ở hàm findNearestValidBush
            Entity bush = findNearestValidBush(prey); 
            
            if (bush != null) {
                Vector2D bushDir = new Vector2D(
                    bush.getPosition().getX() - prey.getPosition().getX(),
                    bush.getPosition().getY() - prey.getPosition().getY()
                );
                bushDir.normalize(); 
                
                double dotProduct = (fleeDir.getX() * bushDir.getX()) + (fleeDir.getY() * bushDir.getY());
                // thỏ chỉ chạy vào cái bụi mà tích vô hướng của vecto chạy và vecto bụi trống gần nhất lớn hơn 0.3
                if (dotProduct > 0.3) { 
                    fleeDir = bushDir;
                }
            }
        }

        fleeDir.normalize();
        double sprintSpeed = prey.getSpeed();
        prey.getVelocity().setX(fleeDir.getX() * sprintSpeed);
        prey.getVelocity().setY(fleeDir.getY() * sprintSpeed);
    }
    
    private Entity findActualThreat(Animal prey) {
        Entity nearestThreat = null;
        double baseDetection = getDetectionRadius(prey);
        double minDistance = baseDetection;

        // Quét QuadTree trong phạm vi cảnh giác
        Rectangle searchRange = new Rectangle(
            prey.getPosition().getX(), prey.getPosition().getY(), 
            baseDetection * 2, baseDetection * 2
        );

        List<Entity> nearbyEntities = Environment.getInstance().getQuadTree().query(searchRange, null);

        for (Entity entity : nearbyEntities) {
            if (entity instanceof Carnivore && entity.isAlive()) {
                Carnivore predator = (Carnivore) entity;

                // CHỐT CHẶN QUAN TRỌNG NHẤT: Kẻ này có săn được mình không?
                // Ví dụ: Sói (Wolf) gọi canAttack(Voi) -> trả về false -> Vòng lặp bỏ qua luôn con sói này!
                if (!predator.canAttack(prey)) {
                    continue; 
                }

                double distance = prey.getPosition().distanceTo(predator.getPosition());
                if (distance > baseDetection) continue; 

                boolean isDetected = false;

                // TÁI SỬ DỤNG LOGIC CHỐNG RÓN RÉN SIÊU MƯỢT
                if (prey.getCurrentState() == AnimalState.FLEEING) {
                    isDetected = true; // Đang hoảng loạn thì cảnh giác 100%
                } 
                else if (predator.getCurrentState() == AnimalState.SNEAKING) {
                    double absoluteDetectionZone = baseDetection * 0.4; 
                    if (distance <= absoluteDetectionZone) {
                        isDetected = true; 
                    } else {
                        double riskChance = 0.01 + 0.03 * (1.0 - (distance - absoluteDetectionZone) / (baseDetection - absoluteDetectionZone));
                        if (Math.random() < riskChance) {
                            isDetected = true; 
                        }
                    }
                } 
                else {
                    // Kẻ địch đi bộ hoặc đang chạy -> Bị lộ 100%
                    isDetected = true; 
                }

                if (isDetected && distance < minDistance) {
                    minDistance = distance;
                    nearestThreat = predator;
                }
            }
        }
        return nearestThreat;
    }

    // Hàm tiện ích lấy bán kính cảnh báo
    private double getDetectionRadius(Animal prey) {
        // Kiểm tra an toàn xem có phải Herbivore không (ví dụ Voi, Thỏ)
        if (prey instanceof Herbivore) {
            return ((Herbivore) prey).getVisionRadius();
        }
        // Fallback nếu là loài khác
        return prey.getVisionRadius() * 0.75;
    }


    // --- 2. HÀM TÌM BỤI RẬM BẰNG QUADTREE ---
    private Entity findNearestValidBush(Animal prey) {
        if(prey.getSize() >= 5.0) { // sinh vật lớn sẽ không tìm bụi
            return null;
        }
        Entity nearestBush = null;
        double vision = prey.getVisionRadius(); 
        double minDistance = vision;

        model.environment.Rectangle searchRange = new model.environment.Rectangle(
            prey.getPosition().getX(), prey.getPosition().getY(), 
            vision * 2, vision * 2
        );

        List<Entity> nearbyEntities = Environment.getInstance().getQuadTree().query(searchRange, null);

        for (Entity entity : nearbyEntities) {
            // Ép kiểu về Interface Hideable để giao tiếp
            if (entity instanceof Hideable && entity.isAlive()) {
                Hideable bush = (Hideable) entity;
                
                // 3. ĐIỀU KIỆN LỌC: Bụi phải CÒN CHỖ
                if (!bush.isFull()) {
                    double distance = prey.getPosition().distanceTo(entity.getPosition());
                    
                    if (distance <= vision && distance < minDistance) {
                        minDistance = distance;
                        nearestBush = entity;
                    }
                }
            }
        }
        return nearestBush;
    }
}