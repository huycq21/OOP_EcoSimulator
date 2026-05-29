package model;

public abstract class Entity {
    // Thuộc tính protected để các lớp con có thể truy cập
    protected Vector2D position;
    protected double size;        // Kích thước (bán kính) để tính va chạm
    protected boolean isAlive;    // Trạng thái sống/tồn tại
    protected boolean runtimePlaced = false;
    
    // Một ID duy nhất để phân biệt các thực thể (rất hữu ích khi debug)
    private static int idCounter = 0;
    protected final int id;

    public Entity(Vector2D position, double size) {
        this.id = ++idCounter;
        this.position = position;
        this.size = size;  
        this.isAlive = true;
    }

    // Phương thức trừu tượng cốt lõi: Mọi vật thể phải tự cập nhật trạng thái của nó
    // Hàm này sẽ được SimulationEngine gọi 60 lần/giây
    public abstract void update();

    // Getters / Setters
    public Vector2D getPosition() {
        return position;
    }

    public void setPosition(Vector2D position) {
        this.position = position;
    }

    public double getSize() {
        return size;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public boolean isRuntimePlaced() {
        return runtimePlaced;
    }

    public void setRuntimePlaced(boolean runtimePlaced) {
        this.runtimePlaced = runtimePlaced;
    }

    // Khi con vật chết hoặc cây bị ăn hết, gọi hàm này
    public void destroy() {
        this.isAlive = false;
    }

    public int getId() {
        return id;
    }
}