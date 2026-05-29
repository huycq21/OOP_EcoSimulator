package model;

import model.strategy.SurvivalStrategy;
import model.*;

public abstract class Animal extends Entity implements Ageable {
    // 1. Các chỉ số sinh tồn cơ bản
    protected double hp;
    protected int age;
    protected int maxAge;
    protected double maxHp;
    protected double energy;
    protected double maxEnergy;
    protected double speed;
    protected double visionRadius;
    protected boolean canEnterWater;
    protected boolean requiresWater;
    
    // 2. Trạng thái và Vật lý (Di chuyển)
    protected Vector2D velocity; 
    protected AnimalState currentState;
    protected int stateLockTicks;
    private boolean carcassSpawned;
    private int hidingTicks;
    private boolean justLeftBush = false;
    
    // 3. ĐÂY CHÍNH LÀ BỘ NÃO (Strategy Pattern)
    protected SurvivalStrategy brain; 

    // Constructor
    public Animal(Vector2D position, double size, double maxHp, double maxEnergy, double speed, double visionRadius) {
        super(position, size); 
        
        this.maxHp = maxHp;
        this.hp = maxHp;             
        this.maxAge = 20000;
        
        this.maxEnergy = maxEnergy;
        this.energy = maxEnergy;     
        
        this.speed = speed;
        this.visionRadius = visionRadius;
        this.canEnterWater = false;
        this.requiresWater = false;
        
        this.velocity = new Vector2D(0, 0); 
        this.currentState = AnimalState.WANDERING; 
        this.stateLockTicks = 0;
        this.carcassSpawned = false;
    }

    @Override
    public void update() {
        if (!isAlive) return; 

        if (currentState == AnimalState.DEAD) {
            velocity.setX(0);
            velocity.setY(0);
            if (stateLockTicks > 0) {
                stateLockTicks--;
            } else {
                destroy();
            }
            return;
        }

        // 1. Giảm thể lực theo thời gian
        decreaseEnergy();
        // Mỗi khung hình, tăng tuổi
        growOlder();

        // 2. Kiểm tra sinh tử
        if (hp <= 0 || energy <= 0 || isTooOld()) {
            die();
            return; 
        }

        // 3. UỶ QUYỀN SUY NGHĨ CHO BỘ NÃO
        // Thú vị ở đây: Class Animal không cần biết nó đang chạy trốn hay đi dạo.
        // Nó chỉ gọi cái "não" ra và bảo: "Mày tính toán hướng đi cho tao đi!"
        if (currentState == AnimalState.HIDING) {

            hidingTicks--;

            velocity.setX(0);
            velocity.setY(0);

            if (hidingTicks <= 0) {

                currentState = AnimalState.WANDERING;
                justLeftBush = true;

                double angle = Math.random() * Math.PI * 2;

                position.setX(
                    position.getX() + Math.cos(angle) * 50
                );

                position.setY(
                    position.getY() + Math.sin(angle) * 50
                );
            }

        } else if (stateLockTicks > 0) {

            stateLockTicks--;

        } else if (this.brain != null) {

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
        if (this.currentState == AnimalState.DEAD) return;
        if (stateLockTicks > 0 && currentState != AnimalState.DEAD) return;
        this.currentState = currentState;
    }

    public void startAttackState() {
        setTemporaryState(AnimalState.ATTACKING, 18);
    }

    public void receiveDamage(double damage) {
        if (currentState == AnimalState.DEAD) return;

        this.hp -= damage;
        if (this.hp <= 0) {
            die();
        } else {
            setTemporaryState(AnimalState.HURT, 20);
        }
    }

    public void die() {
        this.hp = 0;
        this.currentState = AnimalState.DEAD;
        this.stateLockTicks = 42;
        this.velocity.setX(0);
        this.velocity.setY(0);
    }

    public boolean shouldSpawnCarcass() {
        if (currentState != AnimalState.DEAD || carcassSpawned) return false;

        carcassSpawned = true;
        return true;
    }

    private void setTemporaryState(AnimalState state, int ticks) {
        if (currentState == AnimalState.DEAD) return;

        this.currentState = state;
        this.stateLockTicks = ticks;
    }

    @Override
    public void growOlder() {
        age++;
    }

    @Override
    public boolean isTooOld() {
        return age > maxAge;
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

    public boolean canEnterWater() {
        return canEnterWater;
    }

    public boolean requiresWater() {
        return requiresWater;
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
    public double getMaxHp() {
        return maxHp;
    }
    public void setHp(double x) {
        if (x < this.hp) {
            receiveDamage(this.hp - x);
        } else {
            this.hp = x;
        }
    }
    public void setEnergy(double energy) {
        this.energy = energy;
    }
    // (Bạn có thể thêm các getter/setter khác cho hp, maxHp... nếu cần)

    public int getHidingTicks() {
        return hidingTicks;
    }

    public void setHidingTicks(int hidingTicks) {
        this.hidingTicks = hidingTicks;
    }

    public boolean hasJustLeftBush() {
        return justLeftBush;
    }

    public void setJustLeftBush(boolean value) {
        this.justLeftBush = value;
    }
}


