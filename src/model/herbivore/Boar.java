package model.herbivore;

import model.Vector2D;
import model.strategy.ForagingStrategy;
import model.Animal;
import model.Entity;
import model.Reproducible;

public class Boar extends Herbivore implements Reproducible{

    public Boar(Vector2D position) {
        // Kích thước: 4.5, Máu: 120 (Khá trâu), Năng lượng: 150
        // Tốc độ: 4.5, Tầm nhìn: 50.0
        super(position, 4.5, 120, 150, 4.5, 50.0);
        
        // Lắp não mặc định.
        // TODO: Sau này thay bằng DefensiveStrategy (Bị dồn vào chân tường sẽ quay lại húc)
        this.setBrain(new ForagingStrategy());
    }

    @Override
    public Entity reproduce(Animal partner) {

        setEnergy(getEnergy() * 0.7);
        partner.setEnergy(partner.getEnergy() * 0.7);

        return new Boar(
            new Vector2D(
                getPosition().getX() + 15,
                getPosition().getY() + 15
            )
        );
    }
}
