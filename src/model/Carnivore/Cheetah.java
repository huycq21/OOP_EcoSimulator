package model.carnivore;

import model.Vector2D;
import model.strategy.HunterStrategy;
import model.*;

public class Cheetah extends Carnivore implements Ageable {

    public Cheetah(Vector2D position) {
        // Thứ tự super: position, size, maxHp, maxEnergy, speed, visionRadius, strengthWeight, attackDamage, attackCooldown
        super(position, 4.5, 80, 80, 8.5, 90.0, 60.0, 45.0, 30);
        //cắn nhanh dame nằm giữa sói và cáo
        this.setBrain(new HunterStrategy());
    }

    @Override
    public void growOlder() {
        age++;
    }

    @Override
    public boolean isTooOld() {
        return age > maxAge;
    }
}
