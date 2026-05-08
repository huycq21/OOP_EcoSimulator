package model.herbivore;

import model.Vector2D;
import model.strategy.ForagingStrategy;

public class Boar extends Herbivore {

    public Boar(Vector2D position) {
        // Kích thước: 4.5, Máu: 120 (Khá trâu), Năng lượng: 150
        // Tốc độ: 4.5, Tầm nhìn: 50.0
        super(position, 4.5, 120, 150, 4.5, 50.0);
        
        // Lắp não mặc định.
        // TODO: Sau này thay bằng DefensiveStrategy (Bị dồn vào chân tường sẽ quay lại húc)
        this.setBrain(new ForagingStrategy());
    }
}
