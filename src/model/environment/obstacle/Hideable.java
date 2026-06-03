package model.environment.obstacle;

import model.Animal;
import java.util.List;

public interface Hideable {
    void hideEntity(Animal animal);
    void removeEntity(Animal animal);
    List<Animal> getHiddenEntities(); // Dùng để kiểm tra xem con vật đã ở trong này chưa
    boolean isFull();
    double getMaxAllowedSize();
    int getMaxCapacity();
}