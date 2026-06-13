package model.strategy;

import java.util.List;
import model.Animal;
import model.AnimalState;
import model.Entity;
import model.Vector2D;
import model.carnivore.Carnivore;
import model.herbivore.Herbivore;
import model.environment.Environment; 
import model.environment.Rectangle;
import model.environment.obstacle.Hideable;

public class ScaredStrategy implements SurvivalStrategy {
    private final SurvivalStrategy nextLogic;

    public ScaredStrategy() {
        this.nextLogic = new PassiveStrategy();
    }

    public ScaredStrategy(SurvivalStrategy nextLogic) {
        this.nextLogic = nextLogic;
    }

    @Override
    public void execute(Animal prey) {
        // --- 1. QUÉT RADAR ĐỂ TÌM MỐI ĐE DỌA THỰC SỰ ---
        Entity threat = findActualThreat(prey); 

        // Nếu không có kẻ thù nào đe dọa -> Trả quyền điều khiển về cho AI tầng dưới (Đi dạo/Ăn cỏ)
        if (threat == null) {
            // Sửa lỗi bản mới: Nếu trước đó đang trốn (HIDING) mà giờ hết nguy hiểm, 
            // AI tầng dưới cần kéo nó ra khỏi trạng thái trốn để đi lại bình thường.
            nextLogic.execute(prey);
            return;
        }

        double distanceToThreat = prey.getPosition().distanceTo(threat.getPosition());
        
        // Tính toán khoảng cách hoảng loạn động dựa trên kích thước vật lý (Thay vì hardcode)
        double panicDistance = prey.getSize() + threat.getSize() + 5.0; 
        boolean isPanicking = distanceToThreat < panicDistance; 

        // --- 2. XỬ LÝ LOGIC KHI ĐANG NẤP TRONG BỤI RẬM (HIDING) ---
        if (prey.getCurrentState() == AnimalState.HIDING) {
            // Quy tắc hệ sinh thái EcoSimulator: Chỉ sinh vật có kích thước nhỏ (size <= 5.0) mới vào được bụi
            boolean predatorCanEnter = threat.getSize() <= 5.0; 

            // Chỉ hoảng loạn bật dậy bỏ chạy NẾU kẻ thù đã sát sườn VÀ kẻ thù đủ nhỏ để chui vào bụi
            if (isPanicking && predatorCanEnter) {
                prey.setCurrentState(AnimalState.FLEEING);
            } else {
                // Kẻ thù quá to xác không vào được bụi, hoặc ở xa -> Tiếp tục nằm im nín thở
                prey.getVelocity().setX(0);
                prey.getVelocity().setY(0);
                return;
            }
        }

        // --- 3. TOÁN HỌC VECTOR: TÍNH TOÁN HƯỚNG CHẠY TRỐN (FLEE) ---
        prey.setCurrentState(AnimalState.FLEEING);
        
        // Vector hướng chạy ngược lại với tâm vị trí kẻ thù: Nguồn (Prey) - Đích (Threat)
        Vector2D fleeDir = new Vector2D(
            prey.getPosition().getX() - threat.getPosition().getX(),
            prey.getPosition().getY() - threat.getPosition().getY()
        );
        fleeDir.normalize();

        // --- 4. CƠ CHẾ BẺ LÁI LAO VÀO BỤI RẬM THÔNG MINH ---
        // Chỉ những sinh vật nhỏ (size <= 5.0 như Thỏ) mới có thể tư duy tìm bụi để trốn
        if (!isPanicking && prey.getSize() <= 5.0) {
            Entity bush = findNearestValidBush(prey); 
            
            if (bush != null) {
                Vector2D bushDir = new Vector2D(
                    bush.getPosition().getX() - prey.getPosition().getX(),
                    bush.getPosition().getY() - prey.getPosition().getY()
                );
                bushDir.normalize(); 
                
                // Tính tích vô hướng để kiểm tra góc bẻ lái
                double dotProduct = (fleeDir.getX() * bushDir.getX()) + (fleeDir.getY() * bushDir.getY());
                
                // Chỉ lao vào bụi rậm nếu nó nằm cùng hoặc lệch không quá nhiều so với hướng chạy trốn (cos góc > 0.3)
                if (dotProduct > 0.3) { 
                    fleeDir = bushDir; // Thay đổi mục tiêu di chuyển hướng thẳng vào bụi
                }
            }
        }

        // Áp đặt vận tốc bứt tốc tối đa (Sprint Speed) để chạy thoát thân
        fleeDir.normalize();
        double sprintSpeed = prey.getSpeed();
        prey.getVelocity().setX(fleeDir.getX() * sprintSpeed);
        prey.getVelocity().setY(fleeDir.getY() * sprintSpeed);
    }
    
    /**
     * Dò quét tìm kiếm kẻ thù thực sự trong không gian QuadTree 
     * Tích hợp cơ chế lọc chuỗi thức ăn canAttack() và cơ chế chống rình rập (SNEAKING)
     */
    private Entity findActualThreat(Animal prey) {
        Entity nearestThreat = null;
        double baseDetection = getDetectionRadius(prey);
        double minDistance = baseDetection;

        // Định hình vùng quét hình vuông bao quanh thực thể
        Rectangle searchRange = new Rectangle(
            prey.getPosition().getX(), prey.getPosition().getY(), 
            baseDetection * 2, baseDetection * 2
        );

        List<Entity> nearbyEntities = Environment.getInstance().getQuadTree().query(searchRange, null);

        for (Entity entity : nearbyEntities) {
            if (entity instanceof Carnivore && entity.isAlive()) {
                Carnivore predator = (Carnivore) entity;

                // CHỐT CHẶN SINH THÁI: Nếu kẻ săn mồi không thể ăn loài này (ví dụ Sói vs Voi) -> Bỏ qua không sợ
                if (!predator.canAttack(prey)) {
                    continue; 
                }

                double distance = prey.getPosition().distanceTo(predator.getPosition());
                if (distance > baseDetection) continue; 

                boolean isDetected = false;

                // HỆ THỐNG PHÁT HIỆN ĐA TẦNG STATE
                if (prey.getCurrentState() == AnimalState.FLEEING) {
                    isDetected = true; // Đang chạy trốn thì radar kích hoạt 100% không thể bị động
                } 
                else if (predator.getCurrentState() == AnimalState.SNEAKING) {
                    // Kẻ thù đang rình rập: Có 40% vùng lõi bán kính chắc chắn bị lộ
                    double absoluteDetectionZone = baseDetection * 0.4; 
                    if (distance <= absoluteDetectionZone) {
                        isDetected = true; 
                    } else {
                        // Tính toán xác suất phát hiện động tăng dần khi kẻ địch tiến lại gần vùng lõi
                        double riskChance = 0.01 + 0.03 * (1.0 - (distance - absoluteDetectionZone) / (baseDetection - absoluteDetectionZone));
                        if (Math.random() < riskChance) {
                            isDetected = true; 
                        }
                    }
                } 
                else {
                    // Kẻ địch đi bộ (WANDERING) hoặc đuổi bắt (CHASING) công khai -> Bị lộ hoàn toàn
                    isDetected = true; 
                }

                // Lưu lại kẻ thù nguy hiểm đang ở khoảng cách gần nhất
                if (isDetected && distance < minDistance) {
                    minDistance = distance;
                    nearestThreat = predator;
                }
            }
        }
        return nearestThreat;
    }

    /**
     * Thuật toán quét QuadTree tìm kiếm bụi rậm (Hideable) còn chỗ trống gần nhất
     */
    private Entity findNearestValidBush(Animal prey) {
        if (prey.getSize() >= 5.0) { 
            return null; // Thực thể lớn không thể trốn bụi rậm
        }
        Entity nearestBush = null;
        double vision = prey.getVisionRadius(); 
        double minDistance = vision;

        Rectangle searchRange = new Rectangle(
            prey.getPosition().getX(), prey.getPosition().getY(), 
            vision * 2, vision * 2
        );

        List<Entity> nearbyEntities = Environment.getInstance().getQuadTree().query(searchRange, null);

        for (Entity entity : nearbyEntities) {
            // Thực thể phải kế thừa cấu trúc Hideable và phải còn tồn tại trên bản đồ
            if (entity instanceof Hideable && entity.isAlive()) {
                Hideable bush = (Hideable) entity;
                
                // ĐIỀU KIỆN LỌC QUAN TRỌNG: Bụi cây phải chưa bị đầy chỗ (Slot ẩn nấp khả dụng)
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

    /**
     * Lấy bán kính cảnh giác an toàn của con mồi
     */
    private double getDetectionRadius(Animal prey) {
        if (prey instanceof Herbivore) {
            return ((Herbivore) prey).getVisionRadius();
        }
        return prey.getVisionRadius() * 0.75;
    }
}