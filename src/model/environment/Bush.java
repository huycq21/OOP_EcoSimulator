package model.environment;

import model.Entity;
import java.util.ArrayList;
import java.util.List;

public class Bush extends Obstacle implements Hideable {
    private List<Entity> hiddenEntities;

    public Bush(Vector2D position) {
        super(position); // Gọi constructor của Obstacle/Entity
        this.hiddenEntities = new ArrayList<>();
    }

    @Override
    public int getMaxCapacity() {
        return 3; // Trốn được tối đa 3 con thỏ
    }

    @Override
    public double getMaxAllowedSize() {
        return 5.0; // Giả sử Thỏ size 3.0 (vừa), Sói size 8.0 (không vừa)
    }

    @Override
    public void hideEntity(Entity entity) {
        if (hiddenEntities.size() < getMaxCapacity() && entity.getSize() <= getMaxAllowedSize()) {
            hiddenEntities.add(entity);
        }
    }

    @Override
    public void removeEntity(Entity entity) {
        hiddenEntities.remove(entity);
    }
}