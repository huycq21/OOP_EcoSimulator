package model.herbivore;

import model.Vector2D;
import model.strategy.*;

public class Elephant extends Herbivore {

    public Elephant(Vector2D position) {
        // Kích thước: 12.0 (Khổng lồ), Máu: 500 (Cực trâu), Năng lượng: 400
        // Tốc độ: 2.0 (Chậm), Tầm nhìn: 50.0
        super(position, 12.0, 500, 400, 2.0, 300.0);
        // Voi thì cứ đi dạo ngẫu nhiên thôi, thú nhỏ tự phải né nó
        // 1. Não đi dạo (Lớp trong cùng - Dùng khi cực kỳ an toàn và no bụng)
        SurvivalStrategy passive = new PassiveStrategy();
        SurvivalStrategy flocking = new FlockingStrategy(passive);
        SurvivalStrategy foraging = new ForagingStrategy(flocking);
        SurvivalStrategy scared = new ScaredStrategy(foraging); 

        this.setBrain(scared);
    }
}
