package model.carnivore;

import model.strategy.HunterStrategy;
import model.*;
import model.herbivore.BlackGrouse;
import model.herbivore.Rabbit;
import model.Animal;
import model.Entity;
import model.Reproducible;

public class Fox extends Carnivore implements Reproducible{
    public Fox(Vector2D position) {
        // ... các thông số cũ ...
        // Bổ sung: 
        // Lực cắn: 30.0 (Cần 2 phát để giết Thỏ 50 HP)
        // Cooldown: 40 tick (Cắn 1 phát, phải đuổi theo Thỏ gần 1 giây sau mới cắn được phát nữa)
        super(position, 3.5, 60, 120, 6.0, 70.0, 40.0, 30.0, 40);
        this.setPreyDetectionRadius(110.0);
        this.addPreyType(Rabbit.class);
        this.addPreyType(BlackGrouse.class);
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

    @Override
    public Entity reproduce(Animal partner) {

        setEnergy(getEnergy() * 0.7);
        partner.setEnergy(partner.getEnergy() * 0.7);

        return new Fox(
            new Vector2D(
                getPosition().getX() + 15,
                getPosition().getY() + 15
            )
        );
    }
}
