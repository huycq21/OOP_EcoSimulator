package model.strategy;

import model.Animal;
import model.AnimalState;
import model.Vector2D;
import model.environment.Environment;
import java.util.Random;

public class PassiveStrategy implements SurvivalStrategy {
    private final Random random;
    
    // Bỏ hết HashMap đi! Giờ mỗi đối tượng bộ não chỉ cần tự nhớ 3 biến này:
    private Vector2D currentWanderDirection;
    private int directionTimer;
    private int restTimer;

    public PassiveStrategy() {
        this.random = new Random();
        this.directionTimer = 0;
        this.restTimer = 0;
    }

    @Override
    public void execute(Animal animal) {
        animal.setCurrentState(AnimalState.WANDERING);

        // 1. Nếu đang trong thời gian nghỉ ngơi -> Đứng im
        if (restTimer > 0) {
            restTimer--;
            animal.getVelocity().setX(0);
            animal.getVelocity().setY(0);
            return;
        }

        // 2. Nếu hết thời gian đi dạo hoặc chưa có hướng đi -> Bốc thăm chọn hành động mới
        if (directionTimer <= 0 || currentWanderDirection == null) {
            chooseNewWanderState();
            
            // Bốc thăm xui trúng ô nghỉ ngơi thì lại đứng im
            if (restTimer > 0) {
                animal.getVelocity().setX(0);
                animal.getVelocity().setY(0);
                return;
            }
        }

        // 3. Đang trong thời gian đi dạo -> Tiếp tục di chuyển
        double pace = animal.getSpeed() * 0.2; // Đi chậm hơn tốc độ tối đa để trông có vẻ đang thong thả đi dạo
        animal.getVelocity().setX(currentWanderDirection.getX() * pace);
        animal.getVelocity().setY(currentWanderDirection.getY() * pace);
        
        steerAwayFromMapEdges(animal);
        directionTimer--;
    }

    private void chooseNewWanderState() {
        if (random.nextDouble() < 0.25) {
            // 25% cơ hội quyết định đứng nghỉ ngơi
            restTimer = 30 + random.nextInt(90);
            directionTimer = 0;
        } else {
            // 75% cơ hội chọn hướng đi mới ngẫu nhiên
            double angle = random.nextDouble() * 2 * Math.PI;
            currentWanderDirection = new Vector2D(Math.cos(angle), Math.sin(angle));
            directionTimer = 45 + random.nextInt(150);
        }
    }

    private void steerAwayFromMapEdges(Animal animal) {
        Environment env = Environment.getInstance();
        if (env == null) return;

        double margin = 80;
        double x = animal.getPosition().getX();
        double y = animal.getPosition().getY();
        Vector2D velocity = animal.getVelocity();

        if (x < margin) {
            velocity.setX(Math.abs(velocity.getX()));
        } else if (x > env.getWidth() - margin) {
            velocity.setX(-Math.abs(velocity.getX()));
        }

        if (y < margin) {
            velocity.setY(Math.abs(velocity.getY()));
        } else if (y > env.getHeight() - margin) {
            velocity.setY(-Math.abs(velocity.getY()));
        }
    }
}