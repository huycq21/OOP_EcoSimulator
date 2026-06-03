package model.herbivore;

import model.Vector2D;
import model.strategy.*;

public class Deer extends Herbivore {

    public Deer(Vector2D position) {
        // Kích thước: 4.0, Máu: 60, Năng lượng: 120
        // Tốc độ: 6.5 (Rất nhanh), Tầm nhìn: 80.0 (Rất xa)
        super(position, 4.0, 60, 120, 6.5, 80.0);
        
        // --- LẮP RÁP BỘ NÃO 3 TẦNG CHUẨN THÚ ĂN CỎ ---
        
        // 1. Não đi dạo (Lớp trong cùng - Dùng khi cực kỳ an toàn và no bụng)
        SurvivalStrategy passive = new PassiveStrategy();
        
        // 2. Não tìm thức ăn (Kích hoạt khi đói)
        // Lưu ý: Trong ForagingStrategy bạn nên xử lý việc tìm tới Plant/Eatable
        SurvivalStrategy foraging = new ForagingStrategy(passive);
        
        // (TÙY CHỌN BỔ SUNG): Hươu thường sống thành bầy, nếu bạn muốn chúng túm tụm lại cho đẹp
        SurvivalStrategy flocking = new FlockingStrategy(foraging);
        
        // 3. Não sợ hãi (VỎ BỌC NGOÀI CÙNG - Tối quan trọng)
        // Gặp thú ăn thịt (Carnivore) là bẻ lái chạy 
        SurvivalStrategy scared = new ScaredStrategy(flocking); 
        
        this.setBrain(scared);
    }
}