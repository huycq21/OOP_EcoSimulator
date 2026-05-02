package model.carnivore;

import model.Vector2D;
import model.strategy.HunterStrategy;

public class Fox extends Carnivore {
    public Fox(Vector2D position) {
        // ... các thông số cũ ...
        // Bổ sung: 
        // Lực cắn: 30.0 (Cần 2 phát để giết Thỏ 50 HP)
        // Cooldown: 40 tick (Cắn 1 phát, phải đuổi theo Thỏ gần 1 giây sau mới cắn được phát nữa)
        super(position, 3.5, 60, 120, 6.0, 70.0, 40.0, 30.0, 40);
        this.setBrain(new HunterStrategy());
    }
}