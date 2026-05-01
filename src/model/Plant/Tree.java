package model.plant;

import model.Vector2D;

public class Tree {
    public Tree(Vector2D position) {
        // Kích thước: 5.0 (to lớn), Dinh dưỡng: 100, Tuổi thọ: 5000 (tick)
        super(position, 8.0, 100.0, 10000);
    }
}
