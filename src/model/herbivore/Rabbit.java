package model.herbivore;

import model.Vector2D;
import model.strategy.ForagingStrategy;

public class Rabbit extends Herbivore {

    // Thỏ có các chỉ số mặc định: Kích thước 3.0, Máu 50, Năng lượng 100, Tốc độ 5.0, Tầm nhìn 50.0
    public Rabbit(Vector2D position) {
        super(position, 3.0, 50, 100, 5.0, 50.0);
        
        // QUAN TRỌNG: Lắp bộ não đi dạo cho thỏ ngay khi mới sinh ra
        this.setBrain(new ForagingStrategy());
    }

    // Bạn thậm chí không cần ghi đè (override) hàm update() ở đây!
    // Con Thỏ sẽ tự động dùng hàm update() của Animal, tự động gọi não PassiveStrategy để tính toán.
}
