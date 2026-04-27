package model;

public abstract class Animal extends Entity {
    protected int hp = 100;
    protected double speed;
    protected double vX, vY; // Vận tốc di chuyển theo trục X và Y

    public Animal(double x, double y, int size, double speed) {
        super(x, y, size);
        this.speed = speed;
        
        // Khởi tạo hướng đi ngẫu nhiên ban đầu cho con vật
        this.vX = (Math.random() - 0.5) * speed;
        this.vY = (Math.random() - 0.5) * speed;
    }
    public void takeDamage(int damage) {
        this.hp -= damage;
        System.out.println("Một con vật vừa bị cắn! Máu còn: " + this.hp);
        
        if (this.hp <= 0) {
            die(); // Nếu máu <= 0 thì gọi hàm chết
        }
    }

    // Hàm xử lý cái chết (Sau này View sẽ dựa vào đây để xóa hình ảnh)
    protected void die() {
        System.out.println("Một con vật đã gục ngã...");
        // Logic xóa con vật khỏi danh sách sẽ được xử lý ở Controller
    }
    
    // Thuật toán dùng chung cho mọi động vật: Di chuyển và dội tường
    protected void move() {
        x += vX;
        y += vY;

        // Giả sử khung hình của bạn là 800x600. Trừ hao kích thước con vật để không lẹm ra ngoài.
        if (x <= 0 || x >= 780) {
            vX = -vX; // Đụng tường dọc (trái/phải) -> Đổi chiều trục X
        }
        if (y <= 0 || y >= 560) {
            vY = -vY; // Đụng tường ngang (trên/dưới) -> Đổi chiều trục Y
        }
    }

    // Hàm trừu tượng: Ép các loài cụ thể phải tự định nghĩa tiếng kêu
    public abstract void makeSound();
}