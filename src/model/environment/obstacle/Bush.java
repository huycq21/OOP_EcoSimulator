package model.environment.obstacle;

import model.Animal;
import model.AnimalState;
import model.Vector2D;
import java.util.ArrayList;
import java.util.List;

public class Bush extends Obstacle implements Hideable {
    // Đổi list từ Entity sang Animal vì chỉ có Animal mới có tập tính nấp
    private List<Animal> hiddenEntities;

    public Bush(Vector2D position, double size) {
        super(position, size); // Gọi constructor của Obstacle
        this.hiddenEntities = new ArrayList<>();
    }

    @Override
    public int getMaxCapacity() {
        return 3; // Một bụi rậm chứa được tối đa 3 con vật nhỏ
    }

    @Override
    public double getMaxAllowedSize() {
        return 5.0; // Chỉ con vật có size <= 5.0 (Thỏ, Cáo...) mới chui lọt
    }

    @Override
    public boolean isFull() {
        return hiddenEntities.size() >= getMaxCapacity();
    }

    @Override
    public void hideEntity(Animal animal) {
        // Kiểm tra an toàn kép (Double-check)
        if (!isFull() && animal.getSize() <= getMaxAllowedSize()) {
            // Nếu con vật chưa có mặt trong danh sách thì mới thêm vào (Chống lỗi chiếm 2 slot)
            if (!hiddenEntities.contains(animal)) {
                hiddenEntities.add(animal);
            }
        }
    }

    @Override
    public void removeEntity(Animal animal) {
        hiddenEntities.remove(animal);
    }

    @Override
    public List<Animal> getHiddenEntities() {
        return hiddenEntities;
    }

    // --- CƠ CHẾ TỰ DỌN DẸP SIÊU THÔNG MINH ---
    @Override
    public void update() {
        // Hàm update của Bush sẽ được gọi mỗi frame (giống các Entity khác)
        // Nó sẽ tự động quét danh sách những con đang nấp bên trong.
        // Nếu con nào ĐÃ CHẾT, hoặc ĐÃ ĐỔI STATE KHÁC (không còn HIDING nữa) -> Đá nó ra khỏi list để nhường chỗ!
        
        hiddenEntities.removeIf(animal -> 
            !animal.isAlive() || animal.getCurrentState() != AnimalState.HIDING
        );
    }
}