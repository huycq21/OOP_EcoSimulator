package model.environment;

import model.Entity;
import java.util.ArrayList;
import java.util.List;

public class QuadTree {
    private Rectangle boundary; // Giới hạn không gian của node này
    private int capacity;       // Số lượng tối đa thực thể trước khi phải chia nhỏ
    private List<Entity> entities; // Danh sách thực thể chứa trong node này
    private boolean divided;    // Node này đã bị chia làm 4 chưa?

    // 4 Node con
    private QuadTree northWest, northEast, southWest, southEast;

    public QuadTree(Rectangle boundary, int capacity) {
        this.boundary = boundary;
        this.capacity = capacity;
        this.entities = new ArrayList<>();
        this.divided = false;
    }

    // Hàm thêm thực thể vào QuadTree
    public boolean insert(Entity e) {
        // Nếu thực thể không nằm trong ranh giới, bỏ qua
        if (!boundary.contains(e)) {
            return false;
        }

        // Nếu còn chỗ trống, nhét vào đây
        if (entities.size() < capacity) {
            entities.add(e);
            return true;
        }

        // Nếu hết chỗ, chia làm 4 nếu chưa chia
        if (!divided) {
            subdivide();
        }

        // Đẩy thực thể xuống các node con
        if (northWest.insert(e)) return true;
        if (northEast.insert(e)) return true;
        if (southWest.insert(e)) return true;
        if (southEast.insert(e)) return true;
        
        return false;
    }

    // Hàm chia node hiện tại thành 4 ô nhỏ hơn
    private void subdivide() {
        double x = boundary.x;
        double y = boundary.y;
        double w = boundary.w;
        double h = boundary.h;

        northEast = new QuadTree(new Rectangle(x + w/2, y - h/2, w/2, h/2), capacity);
        northWest = new QuadTree(new Rectangle(x - w/2, y - h/2, w/2, h/2), capacity);
        southEast = new QuadTree(new Rectangle(x + w/2, y + h/2, w/2, h/2), capacity);
        southWest = new QuadTree(new Rectangle(x - w/2, y + h/2, w/2, h/2), capacity);

        divided = true;
    }

    // Lấy ra tất cả các thực thể nằm trong một phạm vi (range) nhất định
    public List<Entity> query(Rectangle range, List<Entity> found) {
        if (found == null) {
            found = new ArrayList<>();
        }

        // Nếu ranh giới tìm kiếm không hề chạm vào node này thì bỏ qua luôn (Đây là mấu chốt giúp game chạy nhanh!)
        if (!boundary.intersects(range)) {
            return found;
        }

        // Gom các thực thể trong node này
        for (Entity e : entities) {
            if (range.contains(e)) {
                found.add(e);
            }
        }

        // Nếu có node con, tiếp tục tìm xuống dưới
        if (divided) {
            northWest.query(range, found);
            northEast.query(range, found);
            southWest.query(range, found);
            southEast.query(range, found);
        }

        return found;
    }
}