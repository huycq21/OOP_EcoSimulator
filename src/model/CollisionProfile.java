package model;

import model.apex.Bear;
import model.apex.Human;
import model.apex.Lion;
import model.carnivore.Cheetah;
import model.herbivore.Goat;
import model.herbivore.Horse;

public final class CollisionProfile {
    private CollisionProfile() {
    }

    public static double mapRadius(Entity entity) {
        Double customRadius = customRadius(entity);
        if (customRadius != null) return customRadius;
        return Math.max(4.0, entity.getSize());
    }

    public static double bodyRadius(Entity entity) {
        Double customRadius = customRadius(entity);
        if (customRadius != null) return customRadius;
        return entity.getSize() / 2.0;
    }

    private static Double customRadius(Entity entity) {
        if (entity instanceof Goat) return 4.5;
        if (entity instanceof Horse) return 5.8;
        if (entity instanceof Cheetah) return 5.2;
        if (entity instanceof Lion) return 8.0;
        if (entity instanceof Bear) return 9.0;
        if (entity instanceof Human) return 3.8;
        return null;
    }
}
