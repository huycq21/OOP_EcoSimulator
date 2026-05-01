package model.strategy;

import model.Animal;
import model.AnimalState;

public class ScaredStrategy implements SurvivalStrategy {
    @Override
    public void execute(Animal animal) {
        // Logic: Quét kẻ thù xung quanh.
        // Nếu thấy sói -> cập nhật Vector2D velocity của animal để chạy ngược hướng.
        // Nếu không thấy ai -> đi dạo ngẫu nhiên (wander).
        animal.setCurrentState(AnimalState.FLEEING);
        // code tính toán vector chạy trốn...
    }
}