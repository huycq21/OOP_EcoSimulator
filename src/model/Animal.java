package model;

import model.strategy.SurvivalStrategy;

public abstract class Animal extends Entity {
    // 1. Các chỉ số sinh tồn cơ bản
    protected double hp;

    protected double maxHp;
    protected double energy;
    protected double maxEnergy;
    protected double speed;
    protected double visionRadius;
    
    // 2. Trạng thái và Vật lý (Di chuyển)
    protected Vector2D velocity; 
    protected AnimalState currentState;
    
    // 3. ĐÂY CHÍNH LÀ BỘ NÃO (Strategy Pattern)
    protected SurvivalStrategy brain; 

    // Constructor
    public Animal(Vector2D position, double size, double maxHp, double maxEnergy, double speed, double visionRadius) {
        super(position, size); 
        
        this.maxHp = maxHp;
        this.hp = maxHp;             
        
        this.maxEnergy = maxEnergy;
        this.energy = maxEnergy;     
        
        this.speed = speed;
        this.visionRadius = visionRadius;
        
        this.velocity = new Vector2D(0, 0); 
        this.currentState = AnimalState.WANDERING; 
    }

    @Override
    public void update() {
        if (!isAlive) return; 

        // 1. Giảm thể lực theo thời gian
        decreaseEnergy();
        
        // 2. Kiểm tra sinh tử
        if (hp <= 0 || energy <= 0) {
            this.currentState = AnimalState.DEAD;
            this.destroy(); 
            return; 
        }

        // 3. UỶ QUYỀN SUY NGHĨ CHO BỘ NÃO
        // Thú vị ở đây: Class Animal không cần biết nó đang chạy trốn hay đi dạo.
        // Nó chỉ gọi cái "não" ra và bảo: "Mày tính toán hướng đi cho tao đi!"
        if (this.brain != null) {
            this.brain.execute(this); 
        }
        // 4. Thực thi di chuyển (Cộng vector vận tốc vào tọa độ)
        // Vận tốc này vừa được cái "não" ở bước 3 tính toán xong
        position.add(velocity);
    }

    // Hàm giảm năng lượng cơ bản
    private void decreaseEnergy() {
        this.energy -= 0.02; // Có thể đưa hệ số này ra SimulationConstant cho dễ chỉnh
    }

    // --- CÁC HÀM GETTER / SETTER QUAN TRỌNG ---
    
    // ĐÂY LÀ HÀM QUAN TRỌNG NHẤT ĐỂ "THAY NÃO"
    public void setBrain(SurvivalStrategy newBrain) {
        this.brain = newBrain;
    }
    
    public SurvivalStrategy getBrain() {
        return brain;
    }

    public AnimalState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(AnimalState currentState) {
        this.currentState = currentState;
    }

    public Vector2D getVelocity() {
        return velocity;
    }

    public double getSpeed() {
        return speed;
    }

    public double getVisionRadius() {
        return visionRadius;
    }

    public double getEnergy() {
        return energy;
    }

    public double getMaxEnergy() {
        return maxEnergy;
    }
    public double getHp() {
        return hp;
    }
    public void setHp(double x) {
        this.hp = x;
    }
    public void setEnergy(double energy) {
        this.energy = energy;
    }
    // (Bạn có thể thêm các getter/setter khác cho hp, maxHp... nếu cần)
}