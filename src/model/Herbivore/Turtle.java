package model.herbivore;

import model.Vector2D;
import model.strategy.PassiveStrategy;

public class Turtle extends Herbivore {

    // Rùa: Kích thước 4.0 (to hơn thỏ tí), Máu 200 (trâu), Năng lượng 80, Tốc độ 0.8 (chậm rì), Tầm nhìn 20.0 (cận thị)
    public Turtle(Vector2D position) {
        super(position, 4.0, 200, 80, 0.8, 20.0);
        
        // Cứ lắp não đi dạo bình thường, rùa thì chả cần vội
        this.setBrain(new PassiveStrategy());
    }
    
    // Tương tự, không cần đụng chạm gì đến update() hay logic di chuyển!
}