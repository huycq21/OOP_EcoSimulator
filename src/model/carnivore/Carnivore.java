package model.carnivore;

import java.util.ArrayList;
import java.util.List;
import model.Animal;
import model.Entity;
import model.Vector2D;
import util.SoundManager;

public abstract class Carnivore extends Animal {
    protected double strengthWeight;       // Mức độ đe dọa / Khí chất gốc của loài
    protected double attackDamage;         // Lực sát thương gây ra mỗi lần tấn công

    protected int attackCooldown;          // Thời gian chờ hồi chiêu giữa các lần cắn (số tick)
    protected int currentCooldownTimer;    // Bộ đếm ngược thời gian hồi chiêu
    protected double preyDetectionRadius;  // Bán kính phát hiện con mồi xung quanh
    protected double strikeRadius;         // Khoảng cách bộc phát tốc độ sau khi rón rén tiếp cận mồi
    protected double packMultiplier = 1.0; // Hệ số cộng dồn sức mạnh khi đi theo bầy đàn

    // Danh sách các loài có thể ăn (Để kiểu Animal giúp thú ăn thịt lớn ăn được thú ăn thịt nhỏ)
    protected List<Class<? extends Animal>> preyTypes; 

    public Carnivore(Vector2D position, double size, double maxHp, double maxEnergy, 
                     double speed, double visionRadius, double strengthWeight, 
                     double attackDamage, int attackCooldown) {
                     
        super(position, size, maxHp, maxEnergy, speed, visionRadius);
        this.strengthWeight = strengthWeight;
        this.attackDamage = attackDamage;
        this.attackCooldown = attackCooldown;
        this.currentCooldownTimer = 0; // Sẵn sàng tấn công ngay khi vừa vào map
        this.preyDetectionRadius = visionRadius;
        this.strikeRadius = this.preyDetectionRadius * 0.5; // Mặc định áp sát 50% tầm nhìn sẽ vồ mồi
        this.preyTypes = new ArrayList<>();
    }

    // Kiểm tra xem thú ăn thịt có đang đói hay không (Năng lượng tụt xuống dưới 25%)
    public boolean isStarving() {
        return this.getEnergy() < (this.getMaxEnergy() * 0.25);
    }
    
    @Override
    public void update() {
        super.update(); // Gọi logic của lớp cha Animal để cập nhật di chuyển và tiêu hao năng lượng
        
        if (!isAlive) return;

        // Giảm thời gian chờ hồi chiêu theo từng khung hình (tick) của Game Loop
        if (currentCooldownTimer > 0) {
            currentCooldownTimer--;
        }
    }

    // Hàm thực hiện hành vi tấn công/cắn con mồi
    public void attack(Animal prey) {
        if (currentCooldownTimer == 0 && prey != null && prey.isAlive()) {
            this.startAttackState(); // Kích hoạt trạng thái hoạt ảnh tấn công công khai

            // Gây sát thương trực tiếp lên thanh máu của con mồi
            prey.receiveDamage(this.attackDamage);
            
            // Phát âm thanh cào xé / cắn từ bản mới
            SoundManager.playSound("attack_swipe.wav");
            
            // Đặt lại bộ đếm thời gian hồi chiêu
            this.currentCooldownTimer = this.attackCooldown;
            
            // Console Debug (Có thể mở ra khi cần thiết)
            // System.out.println(this.getClass().getSimpleName() + " tấn công " + prey.getClass().getSimpleName() + " gây " + this.attackDamage + " dame!");
        }
    }

    // Kiểm tra xem đối tượng đích có nằm trong thực đơn (danh sách món ăn) của loài này không
    public boolean canAttack(Animal prey) {
        if (prey == null || !prey.isAlive()) return false;
        if (preyTypes.isEmpty()) return true; // Nếu danh sách rỗng, mặc định ăn tạp mọi thứ

        for (Class<? extends Animal> preyType : preyTypes) {
            if (preyType.isInstance(prey)) {
                return true;
            }
        }
        return false;
    }

    // Đăng ký thêm con mồi vào thực đơn khi khởi tạo các class con cụ thể (Fox, Wolf, Lion,...)
    protected void addPreyType(Class<? extends Animal> preyType) {
        preyTypes.add(preyType);
    }
    
    public List<Class<? extends Animal>> getPreyType() {
        return preyTypes;
    }

    public void setAttackDamage(double attackDamage) {
        this.attackDamage = attackDamage;
    }

    public double getAttackDamage() { 
        return attackDamage; 
    }

    public double getPreyDetectionRadius() {
        return preyDetectionRadius;
    }

    public void setPreyDetectionRadius(double preyDetectionRadius) {
        this.preyDetectionRadius = preyDetectionRadius;
    }

    public void setPackMultiplier(double multiplier) {
        this.packMultiplier = multiplier;
    }

    // Tính toán mức độ đe dọa thực tế (Càng đông đàn, hào quang uy hiếp càng mạnh)
    public double getStrengthWeight() {
        return this.strengthWeight * this.packMultiplier; 
    }

    public void setStrikeRadius(double strikeRadius) {
        this.strikeRadius = strikeRadius;
    }
    
    public double getStrikeRadius() {
        return strikeRadius;
    }
}