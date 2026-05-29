
package model.strategy;

import model.Animal;

public interface SurvivalStrategy {
    // Hàm này sẽ quyết định con vật (animal) di chuyển hoặc hành động thế nào trong Tick hiện tại
    void execute(Animal animal);
}