package model.carnivore;

import model.Vector2D;
import model.strategy.*;
import model.Animal;
import model.Entity;
import model.Reproducible;

public class Wolf extends Carnivore implements Reproducible{

    public Wolf(Vector2D position) {
        super(position, 5.0, 100.0, 200.0, 5.5, 50.0, 80, 60.0, 60);
        this.setBrain(new HunterStrategy());
    }

    @Override
    public Entity reproduce(Animal partner) {

        setEnergy(getEnergy() * 0.7);
        partner.setEnergy(partner.getEnergy() * 0.7);

        return new Wolf(
            new Vector2D(
                getPosition().getX() + 15,
                getPosition().getY() + 15
            )
        );
    }
}
