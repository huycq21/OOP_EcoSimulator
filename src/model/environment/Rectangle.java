package model.environment;

import model.*;

public class Rectangle {
    public double x, y; // Tọa độ tâm của hình chữ nhật
    public double w, h; // Nửa chiều rộng (half-width) và nửa chiều cao (half-height)

    public Rectangle(double x, double y, double w, double h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    // Kiểm tra xem một thực thể (Entity) có nằm trong vùng này không
    public boolean contains(Entity e) {
        double px = e.getPosition().getX();
        double py = e.getPosition().getY();
        return (px >= x - w && px <= x + w &&
                py >= y - h && py <= y + h);
    }

    // Kiểm tra xem 2 vùng có giao nhau không (Dùng để tối ưu khi truy vấn)
    public boolean intersects(Rectangle range) {
        return !(range.x - range.w > x + w ||
                 range.x + range.w < x - w ||
                 range.y - range.h > y + h ||
                 range.y + range.h < y - h);
    }
}