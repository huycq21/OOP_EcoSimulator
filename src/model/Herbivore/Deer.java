package model.herbivore;

import model.Vector2D;
import model.strategy.PassiveStrategy;

public class Deer extends Herbivore {

    public Deer(Vector2D position) {
        // Kích thước: 4.0, Máu: 60, Năng lượng: 120
        // Tốc độ: 6.5 (Rất nhanh), Tầm nhìn: 80.0 (Rất xa)
        super(position, 4.0, 60, 120, 6.5, 80.0);
        
        // Lắp não mặc định. 
        // TODO: Sau này thay bằng ScaredStrategy để biết bỏ chạy khi thấy Sói
        this.setBrain(new PassiveStrategy());
    }
}
 