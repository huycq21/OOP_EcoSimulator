package model.environment;

import model.Entity;
import java.util.ArrayList;
import java.util.List;

public class QuadTree {
    private final int MAX_DEPTH = 10; // Giới hạn số lần cắt tối đa để chống StackOverflow khi quái dồn cục
    
    private Rectangle boundary;        // Giới hạn không gian của node này
    private int capacity;              // Số lượng tối đa thực thể trước khi phải chia nhỏ
    private List<Entity> entities;     // Danh sách thực thể chứa trong node này
    private boolean divided;           // Node này đã bị chia làm 4 chưa?
    private int depth;                 // Theo dõi tầng hiện tại của node (Gốc bắt đầu từ 0)

    private QuadTree northWest, northEast, southWest, southEast;

    // Constructor gốc dùng cho toàn bộ Map (Khởi tạo từ Environment)
    public QuadTree(Rectangle boundary, int capacity) {
        this(boundary, capacity, 0); 
    }

    // Constructor nội bộ phục vụ việc tự động phân nhánh (subdivide)
    private QuadTree(Rectangle boundary, int capacity, int depth) {
        this.boundary = boundary;
        this.capacity = capacity;
        this.entities = new ArrayList<>();
        this.divided = false;
        this.depth = depth;
    }

    public boolean insert(Entity e) {
        // Nếu thực thể không nằm trong ranh giới của ô này -> Từ chối
        if (!boundary.contains(e)) {
            return false;
        }

        // Nếu node này ĐÃ CHIA RỒI (Nó là node cha điều hướng) -> Đẩy thẳng trách nhiệm xuống các node con
        if (divided) {
            if (northWest.insert(e)) return true;
            if (northEast.insert(e)) return true;
            if (southWest.insert(e)) return true;
            if (southEast.insert(e)) return true;
            return false;
        }

        // ĐIỂM CHỐT HẠ: Nếu chưa đầy HOẶC đã chạm đáy giới hạn độ sâu -> Chấp nhận lưu trữ tại đây
        if (entities.size() < capacity || this.depth >= MAX_DEPTH) {
            entities.add(e);
            return true;
        }

        // Nếu node lá đầy và chưa chạm giới hạn tầng -> Tiến hành cắt nhỏ không gian!
        subdivide();

        // Sau khi chia và đẩy đồ cũ đi, nhét thực thể mới này vào đúng ô con của nó
        if (northWest.insert(e)) return true;
        if (northEast.insert(e)) return true;
        if (southWest.insert(e)) return true;
        if (southEast.insert(e)) return true;

        return false;
    }

    // Hàm chia node hiện tại thành 4 ô con bằng nhau
    private void subdivide() {
        double x = boundary.x;
        double y = boundary.y;
        double w = boundary.w;
        double h = boundary.h;
        
        int nextDepth = this.depth + 1; // Tầng của node con tăng lên 1

        // Tạo cấu trúc 4 vùng địa lý mới
        northEast = new QuadTree(new Rectangle(x + w/2, y - h/2, w/2, h/2), capacity, nextDepth);
        northWest = new QuadTree(new Rectangle(x - w/2, y - h/2, w/2, h/2), capacity, nextDepth);
        southEast = new QuadTree(new Rectangle(x + w/2, y + h/2, w/2, h/2), capacity, nextDepth);
        southWest = new QuadTree(new Rectangle(x - w/2, y + h/2, w/2, h/2), capacity, nextDepth);

        this.divided = true;

        // CHUYỂN NHÀ: Đẩy toàn bộ danh sách thực thể cũ từ node cha xuống các node con tương ứng
        for (Entity e : entities) {
            if (northWest.insert(e)) continue;
            if (northEast.insert(e)) continue;
            if (southWest.insert(e)) continue;
            if (southEast.insert(e)) continue;
        }

        // Giải phóng bộ nhớ của node cha vì dữ liệu đã nằm ở các node con
        entities.clear();
    }

    // Lấy ra tất cả các thực thể nằm trong một phạm vi (range) nhất định (Radar quét tìm mục tiêu/va chạm)
    public List<Entity> query(Rectangle range, List<Entity> found) {
        if (found == null) {
            found = new ArrayList<>();
        }

        // Nếu phạm vi quét không hề giao nhau với ô này -> Bỏ qua toàn bộ nhánh (Tiết kiệm CPU)
        if (!boundary.intersects(range)) {
            return found;
        }

        // GOM THỰC THỂ: Quét tất cả các thực thể đang nằm tại node này (Đặc biệt quan trọng với node đã chạm MAX_DEPTH)
        for (Entity e : entities) {
            if (range.contains(e)) {
                found.add(e);
            }
        }

        // Nếu node này đã từng chia nhỏ, tiếp tục đào sâu xuống các node con để gom thêm dữ liệu
        if (divided) {
            northWest.query(range, found);
            northEast.query(range, found);
            southWest.query(range, found);
            southEast.query(range, found);
        }

        return found;
    }
}