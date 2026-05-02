package model.environment;

import model.Entity;
import java.util.ArrayList;
import java.util.List;

import controller.CollisionHandler;

public abstract class Environment {
    // THỦ THUẬT SINGLETON LINH HOẠT: Lưu trữ bản đồ đang được kích hoạt
    private static Environment activeEnvironment;

    protected List<Entity> entities;
    protected double width;
    protected double height;

    public Environment(double width, double height) {
        this.width = width;
        this.height = height;
        this.entities = new ArrayList<>();
    }

    // --- CÁC HÀM QUẢN LÝ BẢN ĐỒ TOÀN CỤC ---

    // Gọi hàm này trong Main hoặc SimulationEngine khi bắt đầu game
    // Ví dụ: Environment.setActiveEnvironment(new Jungle(1024, 768));
    public static void setActiveEnvironment(Environment env) {
        activeEnvironment = env;
    }

    // Bộ não HunterStrategy sẽ gọi hàm này để mượn "radar" quét bản đồ
    public static Environment getInstance() {
        return activeEnvironment;
    }

    // --- CÁC HÀM TƯƠNG TÁC THỰC THỂ ---

    public void addEntity(Entity entity) {
        entities.add(entity);
    }

    public void removeEntity(Entity entity) {
        entities.remove(entity);
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public double getWidth() { return width; }
    public double getHeight() { return height; }

    // --- VÒNG LẶP CỐT LÕI (GAME LOOP) ---

    public void update() {
        // Dùng một danh sách tạm để lưu các thực thể đã chết
        // ĐIỀU NÀY CỰC KỲ QUAN TRỌNG: Tránh lỗi ConcurrentModificationException khi đang lặp mà lại xóa phần tử
        List<Entity> entitiesToRemove = new ArrayList<>();

        for (Entity entity : entities) {
            // 1. Cập nhật logic của từng con vật / cái cây
            entity.update(); 

            // 2. Chặn không cho động vật chạy tị nạn ra khỏi viền màn hình
            keepWithinBounds(entity);

            // 3. Nếu máu hoặc năng lượng <= 0 (isAlive = false), đưa vào danh sách chờ dọn dẹp
            if (!entity.isAlive()) {
                entitiesToRemove.add(entity);
            }
        }
        CollisionHandler.processCollisions(this); 
        // 4. Quét dọn chiến trường: Xóa vĩnh viễn các xác chết và cây cối bị ăn hết khỏi bản đồ
        entities.removeAll(entitiesToRemove);
    }

    // --- HÀM HỖ TRỢ VẬT LÝ ---

    // Đẩy con vật dội lại vào trong nếu chạm mép bản đồ
    protected void keepWithinBounds(Entity entity) {
        double x = entity.getPosition().getX();
        double y = entity.getPosition().getY();
        boolean changed = false;

        if (x < 0) { 
            x = 0; 
            changed = true; 
        } else if (x > width) { 
            x = width; 
            changed = true; 
        }

        if (y < 0) { 
            y = 0; 
            changed = true; 
        } else if (y > height) { 
            y = height; 
            changed = true; 
        }

        // Nếu có sự điều chỉnh, ép lại tọa độ mới cho con vật
        if (changed) {
            entity.getPosition().setX(x);
            entity.getPosition().setY(y);
        }
    }
}