package model;

public class Vector2D {
    private double x;
    private double y;

    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    // Các hàm Get/Set
    public double getX() { return x; }
    public double getY() { return y; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }

    // Cộng 2 vector (dùng để cộng vận tốc vào tọa độ)
    public void add(Vector2D v) {
        this.x += v.x;
        this.y += v.y;
    }

    // Tính khoảng cách giữa 2 điểm (Cực kỳ quan trọng để check va chạm và tầm nhìn)
    public double distanceTo(Vector2D other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    // Lấy độ lớn của vector (tốc độ hiện tại)
    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }

    // Chuẩn hóa vector về độ dài bằng 1 (Dùng để tìm hướng đi)
    public void normalize() {
        double mag = magnitude();
        if (mag > 0) {
            this.x /= mag;
            this.y /= mag;
        }
    }

    // Giới hạn độ lớn vector (Không cho con vật chạy quá tốc độ tối đa)
    public void limit(double max) {
        if (magnitude() > max) {
            normalize();
            this.x *= max;
            this.y *= max;
        }
    }
}
