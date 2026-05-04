package model.environment;

import model.Entity;
import java.util.ArrayList;
import java.util.List;

public class QuadTree {
    private Rectangle boundary; // Giới hạn không gian của node này
    private int capacity;       // Số lượng tối đa thực thể trước khi phải chia nhỏ
    private List<Entity> entities; // Danh sách thực thể chứa trong node này
    private boolean divided;    // Node này đã bị chia làm 4 chưa?

    private QuadTree northWest, northEast, southWest, southEast;

    public QuadTree(Rectangle boundary, int capacity) {
        this.boundary = boundary;
        this.capacity = capacity;
        this.entities = new ArrayList<>();
        this.divided = false;
    }

    public boolean insert(Entity e) {
        // Nếu không thuộc vùng này, từ chối luôn
        if (!boundary.contains(e)) {
            return false;
        }

        // Nếu node này ĐÃ CHIA RỒI (tức là nó đã trở thành Node cha chỉ đường)
        // Thì không lưu đồ ở đây nữa, đẩy thẳng xuống con luôn!
        if (divided) {
            if (northWest.insert(e)) return true;
            if (northEast.insert(e)) return true;
            if (southWest.insert(e)) return true;
            if (southEast.insert(e)) return true;
            return false;
        }

        // Nếu node này chưa chia (vẫn là Node lá), và còn chỗ trống
        if (entities.size() < capacity) {
            entities.add(e);
            return true;
        }

        // Nếu đến đây tức là Node lá này đã ĐẦY. Tiến hành chia nhỏ!
        subdivide();

        // Sau khi chia và chuyển nhà xong, nhét nốt cái thực thể mới (thực thể thứ 5) này xuống con
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

        // 1. Tạo 4 ô con
        northEast = new QuadTree(new Rectangle(x + w/2, y - h/2, w/2, h/2), capacity);
        northWest = new QuadTree(new Rectangle(x - w/2, y - h/2, w/2, h/2), capacity);
        southEast = new QuadTree(new Rectangle(x + w/2, y + h/2, w/2, h/2), capacity);
        southWest = new QuadTree(new Rectangle(x - w/2, y + h/2, w/2, h/2), capacity);

        divided = true;

        // 2. CHUYỂN NHÀ CHO THỰC THỂ! 
        // Đẩy toàn bộ thực thể đang có ở Node này xuống các Node con
        for (Entity e : entities) {
            if (northWest.insert(e)) continue;
            if (northEast.insert(e)) continue;
            if (southWest.insert(e)) continue;
            if (southEast.insert(e)) continue;
        }

        // 3. LÀM RỖNG DANH SÁCH Ở NODE NÀY
        entities.clear();
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