package model;

import java.util.Random;

public abstract class Animal extends Entity {
    // Các chỉ số sinh tồn cơ bản
    protected double hp;
    protected double maxHp;
    protected double energy;
    protected double maxEnergy;
    protected double speed;
    protected double visionRadius;
    
    // Quản lý di chuyển và trạng thái
    protected Vector2D velocity; // Vận tốc hiện tại (hướng + tốc độ)
    protected AnimalState currentState;
    
    // Công cụ random dùng chung cho các lớp con
    protected Random random;

    public Animal(Vector2D position, double size, double maxHp, double maxEnergy, double speed, double visionRadius) {
        super(position, size); // Gọi constructor của Entity để set tọa độ và kích thước
        
        this.maxHp = maxHp;
        this.hp = maxHp;             // Mới sinh ra mặc định đầy máu
        
        this.maxEnergy = maxEnergy;
        this.energy = maxEnergy;     // Mới sinh ra mặc định đầy năng lượng
        
        this.speed = speed;
        this.visionRadius = visionRadius;
        
        this.velocity = new Vector2D(0, 0); // Đứng yên lúc mới sinh
        this.currentState = AnimalState.WANDERING; // Mặc định là đi dạo
        this.random = new Random();
    }

    // Override phương thức trừu tượng từ Entity
    @Override
    public void update() {
        if (!isAlive) return; // Nếu đã chết thì ngắt luôn, không làm gì cả

        // 1. Tiêu hao thể lực theo thời gian
        decreaseEnergy();
        
        // 2. Kiểm tra sinh tồn
        if (hp <= 0 || energy <= 0) {
            this.currentState = AnimalState.DEAD;
            this.destroy(); // Đánh dấu isAlive = false (kế thừa từ Entity)
            return; // Chết rồi thì không di chuyển nữa
        }

        // 3. Xử lý hành vi dựa trên Trạng thái hiện tại
        switch (currentState) {
            case WANDERING:
                wander();
                break;
            case CHASING:
                // Kẻ thù sẽ ghi đè (override) logic này ở lớp Carnivore
                break;
            case FLEEING:
                // Con mồi sẽ ghi đè (override) logic này ở lớp Herbivore
                break;
            case EATING:
                // Dừng lại để hồi năng lượng (vận tốc = 0)
                velocity.setX(0);
                velocity.setY(0);
                break;
            case HIDING:
                // Trốn trong bụi rậm, không di chuyển
                velocity.setX(0);
                velocity.setY(0);
                break;
            default:
                break;
        }
        
        // 4. Cộng vận tốc vào tọa độ để tạo ra sự di chuyển trên màn hình
        position.add(velocity);
    }

    // --- CÁC HÀM LOGIC CƠ BẢN ---

    // Thuật toán đi dạo ngẫu nhiên
    protected void wander() {
        // Chỉ có 5% cơ hội đổi hướng mỗi Tick để đường đi mượt mà, không bị giật cục
        if (random.nextDouble() < 0.05) { 
            // Tạo một góc ngẫu nhiên từ 0 đến 360 độ (2 PI rad)
            double angle = random.nextDouble() * 2 * Math.PI; 
            
            // Tính toán vector vận tốc (Đi dạo thì chỉ đi với 50% tốc độ tối đa)
            velocity.setX(Math.cos(angle) * speed * 0.5); 
            velocity.setY(Math.sin(angle) * speed * 0.5);
        }
    }

    private void decreaseEnergy() {
        // Giảm một lượng nhỏ năng lượng qua mỗi vòng lặp
        this.energy -= 0.02; 
    }

    // --- GETTERS & SETTERS (Tính đóng gói - Encapsulation) ---
    
    public AnimalState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(AnimalState currentState) {
        this.currentState = currentState;
    }

    public double getVisionRadius() {
        return visionRadius;
    }
}