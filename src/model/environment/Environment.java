package model.environment;

import model.Entity;
import java.util.ArrayList;
import java.util.List;
import controller.CollisionHandler;

public abstract class Environment {
    private static Environment activeEnvironment;
    private QuadTree currentQuadTree; 

    protected List<Entity> entities;
    protected double width;
    protected double height;

    // --- VÒNG LẶP CỐT LÕI (GAME LOOP) ---
    public void update() {
        // 1. [QUAN TRỌNG NHẤT] ĐẬP CÂY CŨ, XÂY CÂY MỚI Ở ĐẦU MỖI KHUNG HÌNH!
        Rectangle mapBoundary = new Rectangle(width / 2, height / 2, width / 2, height / 2);
        currentQuadTree = new QuadTree(mapBoundary, 4);
        
        // Nhét thú vào cây để làm radar chung cho mọi class
        for (Entity e : entities) {
            if (e.isAlive()) {
                currentQuadTree.insert(e);
            }
        }

        List<Entity> entitiesToRemove = new ArrayList<>();

        for (Entity entity : entities) {
            // 2. Cập nhật logic (Bây giờ bọn Gấu/Thợ săn gọi getInstance().getQuadTree() thoải mái)
            entity.update(); 

            // 3. Chặn không cho ra khỏi map
            keepWithinBounds(entity);

            if (!entity.isAlive()) {
                entitiesToRemove.add(entity);
            }
        }
        
        // 4. Xử lý va chạm
        CollisionHandler.processCollisions(this); 
        
        // 5. Dọn dẹp xác chết
        entities.removeAll(entitiesToRemove);
    }

    // --- HÀM HỖ TRỢ VẬT LÝ ---
    protected void keepWithinBounds(Entity entity) {
        double x = entity.getPosition().getX();
        double y = entity.getPosition().getY();
        boolean changed = false;

        if (x < 0) { x = 0; changed = true; } 
        else if (x > width) { x = width; changed = true; }

        if (y < 0) { y = 0; changed = true; } 
        else if (y > height) { y = height; changed = true; }

        if (changed) {
            entity.getPosition().setX(x);
            entity.getPosition().setY(y);
        }
    }

    public void addEntity(Entity entity) {
        entities.add(entity); 
    }
    public void removeEntity(Entity entity) {
        entities.remove(entity);
    }
    public List<Entity> getEntities() {
        return entities;
    }
    public double getWidth() {
        return width;
    }
    public double getHeight() {
        return height;
    }

    public QuadTree getQuadTree() {
        return currentQuadTree;
    }
    public void setQuadTree(QuadTree qTree) {
        this.currentQuadTree = qTree;
    }
    public Environment(double width, double height) {
        this.width = width;
        this.height = height;
        this.entities = new ArrayList<>();
    }

    public static void setActiveEnvironment(Environment env) {
        activeEnvironment = env;
    }

    public static Environment getInstance() {
        return activeEnvironment;
    }
}