package model.herbivore;

import model.Animal;
import model.Reproducible;
import model.Vector2D;
import model.strategy.FlockingStrategy;
import model.strategy.ForagingStrategy;
import model.strategy.PassiveStrategy;
import model.strategy.ScaredStrategy;
import model.strategy.SurvivalStrategy;
import model.Entity;

public class Deer extends Herbivore implements Reproducible{

    public Deer(Vector2D position) {
        // Kích thước: 4.0, Máu: 60, Năng lượng: 120
        // Tốc độ: 6.5 (Rất nhanh), Tầm nhìn: 80.0 (Rất xa)
        super(position, 4.0, 60, 120, 6.5, 120.0);
        
        // --- LẮP RÁP BỘ NÃO 3 TẦNG CHUẨN THÚ ĂN CỎ ---
        
        // 1. Não đi dạo (Lớp trong cùng - Dùng khi cực kỳ an toàn và no bụng)
        SurvivalStrategy passive = new PassiveStrategy();
        SurvivalStrategy flocking = new FlockingStrategy(passive);
        SurvivalStrategy foraging = new ForagingStrategy(flocking);
        SurvivalStrategy scared = new ScaredStrategy(foraging); 
        
        this.setBrain(scared);
    }

    @Override
    public Entity reproduce(Animal partner) {

        setEnergy(getEnergy() * 0.5);
        partner.setEnergy(partner.getEnergy() * 0.5);

        return new Deer(
            new Vector2D(
                getPosition().getX() + 15,
                getPosition().getY() + 15
            )
        );
    }
}
 
