package model.environment.obstacle;

import model.Entity;
import model.Vector2D;

public class Obstacle extends Entity {

    public Obstacle(Vector2D position, double size) {
        super(position, size);
        this.isAlive = true; // Vật cản mặc định là luôn tồn tại
    }

    @Override
    public void update() {
        // Chướng ngại vật là những khối tĩnh (không ăn uống, không di chuyển).
        // Do đó, hàm update() của nó để trống để tiết kiệm tài nguyên CPU.
    }
}