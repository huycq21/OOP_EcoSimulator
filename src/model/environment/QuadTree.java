package model.environment;

import model.Entity;
import java.util.ArrayList;
import java.util.List;

public class QuadTree {
    private final int MAX_DEPTH = 10; // Giới hạn số lần cắt tối đa (ĐỘ SÂU)
    
    private Rectangle boundary;
    private int capacity;
    private List<Entity> entities;
    private boolean divided;
    private int depth; // Theo dõi node này đang ở độ sâu bao nhiêu

    private QuadTree northWest, northEast, southWest, southEast;

    // Constructor gốc dùng cho toàn bộ map (Độ sâu bắt đầu từ 0)
    public QuadTree(Rectangle boundary, int capacity) {
        this(boundary, capacity, 0); 
    }

    // Constructor nội bộ dùng khi node cha đẻ ra node con
    private QuadTree(Rectangle boundary, int capacity, int depth) {
        this.boundary = boundary;
        this.capacity = capacity;
        this.entities = new ArrayList<>();
        this.divided = false;
        this.depth = depth;
    }

    public boolean insert(Entity e) {
        if (!boundary.contains(e)) {
            return false;
        }

        if (divided) {
            if (northWest.insert(e)) return true;
            if (northEast.insert(e)) return true;
            if (southWest.insert(e)) return true;
            if (southEast.insert(e)) return true;
            return false;
        }

        // ĐIỂM CHỐT HẠ: Nếu chưa đầy HOẶC đã chạm giới hạn độ sâu -> Nhét luôn vào đây!
        if (entities.size() < capacity || this.depth >= MAX_DEPTH) {
            entities.add(e);
            return true;
        }

        subdivide();

        if (northWest.insert(e)) return true;
        if (northEast.insert(e)) return true;
        if (southWest.insert(e)) return true;
        if (southEast.insert(e)) return true;

        return false;
    }

    private void subdivide() {
        double x = boundary.x;
        double y = boundary.y;
        double w = boundary.w;
        double h = boundary.h;
        
        int nextDepth = this.depth + 1; // Node con sẽ sâu hơn node cha 1 bậc

        // Truyền nextDepth vào để các node con biết mình đang ở tầng thứ mấy
        northEast = new QuadTree(new Rectangle(x + w/2, y - h/2, w/2, h/2), capacity, nextDepth);
        northWest = new QuadTree(new Rectangle(x - w/2, y - h/2, w/2, h/2), capacity, nextDepth);
        southEast = new QuadTree(new Rectangle(x + w/2, y + h/2, w/2, h/2), capacity, nextDepth);
        southWest = new QuadTree(new Rectangle(x - w/2, y + h/2, w/2, h/2), capacity, nextDepth);

        divided = true;

        for (Entity e : entities) {
            if (northWest.insert(e)) continue;
            if (northEast.insert(e)) continue;
            if (southWest.insert(e)) continue;
            if (southEast.insert(e)) continue;
        }

        entities.clear();
    }

    public List<Entity> query(Rectangle range, List<Entity> found) {
        if (found == null) {
            found = new ArrayList<>();
        }

        if (!boundary.intersects(range)) {
            return found;
        }

        for (Entity e : entities) {
            if (range.contains(e)) {
                found.add(e);
            }
        }

        if (divided) {
            northWest.query(range, found);
            northEast.query(range, found);
            southWest.query(range, found);
            southEast.query(range, found);
        }

        return found;
    }
}