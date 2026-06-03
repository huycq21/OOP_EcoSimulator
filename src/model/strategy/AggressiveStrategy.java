package model.strategy;

import model.Animal;
import model.AnimalState;

public class AggressiveStrategy implements SurvivalStrategy {
    @Override
    public void execute(Animal animal) {
        // Logic: Bỏ qua nỗi sợ. Quét tìm thức ăn (cỏ hoặc quả mọng).
        // Lao thẳng tới mục tiêu bất chấp có Sói ở gần.
        animal.setCurrentState(AnimalState.CHASING); // Lúc này thỏ cũng "săn" cỏ như một kẻ săn mồi
        // code tính toán vector lao tới thức ăn...
    }
}