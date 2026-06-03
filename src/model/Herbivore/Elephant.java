package model.herbivore;

import model.Vector2D;
import model.strategy.*;

public class Elephant extends Herbivore {

    public Elephant(Vector2D position) {
        // Kích thước: 12.0 (Khổng lồ), Máu: 500 (Cực trâu), Năng lượng: 400
        // Tốc độ: 2.0 (Chậm), Tầm nhìn: 50.0
        super(position, 12.0, 500, 400, 2.0, 90.0);
        // Voi thì cứ đi dạo ngẫu nhiên thôi, thú nhỏ tự phải né nó
        // 1. Não đi dạo (Lớp trong cùng - Dùng khi cực kỳ an toàn và no bụng)
        SurvivalStrategy passive = new PassiveStrategy();
        
        // 2. Não tìm thức ăn (Kích hoạt khi đói)
        // Lưu ý: Trong ForagingStrategy bạn nên xử lý việc tìm tới Plant/Eatable
        SurvivalStrategy foraging = new ForagingStrategy(passive);
        
        // (TÙY CHỌN BỔ SUNG): Sống thành bầy
        SurvivalStrategy flocking = new FlockingStrategy(foraging);
        
        // Gặp thú ăn thịt (Carnivore) mạnh hơn tầm là bẻ lái chạy 
        SurvivalStrategy scared = new ScaredStrategy(flocking); 
        
        this.setBrain(scared);
    }
}
