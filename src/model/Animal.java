package model;

import model.strategy.SurvivalStrategy;
import controller.EventManager;
import controller.SimulationConstant;
import model.environment.Environment;
import model.environment.TerrainType;

public abstract class Animal extends Entity implements Ageable,Reproducible {
    // --- 1. CÁC CHỈ SỐ SINH TỒN CƠ BẢN ---
    protected double hp;
    protected double maxHp;
    protected int age;
    protected int maxAge;
    protected double energy;
    protected double maxEnergy;
    protected double speed;
    protected double visionRadius;
    protected boolean canEnterWater;
    protected boolean requiresWater;
    protected Gender gender; // Hệ thống giới tính từ bản cũ
    
    // --- 2. TRẠNG THÁI VÀ VẬT LÝ DI CHUYỂN ---
    protected Vector2D velocity; 
    protected AnimalState currentState;
    protected int stateLockTicks;
    private boolean carcassSpawned;
    private int hidingTicks;
    private boolean justLeftBush = false;
    protected double terrainSpeedMultiplier = 1.0;

    // --- 3. CHỈ SỐ ĐIỀU KHIỂN BẦY ĐÀN (Flocking từ bản mới) ---
    protected int flockingTimer = 0;       // Bộ đếm thời gian đi theo đàn
    protected int restingTimer = 0;        // Bộ đếm thời gian tách bầy nghỉ ngơi
    protected boolean isRestingFromFlock = false; // Cờ trạng thái nghỉ ngơi không tụ tập
    
    protected int passiveRestTimer = 0;
    protected int wanderTimer = 0;
    protected Vector2D wanderDirection = null;
        
    // --- 4. BỘ NÃO QUYẾT ĐỊNH HÀNH VI (Strategy Pattern) ---
    protected SurvivalStrategy brain; 

    // Constructor
    public Animal(Vector2D position, double size, double maxHp, double maxEnergy, double speed, double visionRadius) {
        super(position, size); 
        
        this.maxHp = maxHp;
        this.hp = maxHp;             
        this.maxAge = SimulationConstant.DEFAULT_MAX_AGE;
        
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

        // Khởi tạo giới tính ngẫu nhiên (Bản cũ)
        this.gender = Math.random() < 0.5 ? Gender.MALE : Gender.FEMALE;
    }

    @Override
    public void update() {
        this.saveCurrentPosition();
        if (!isAlive) return; 

        // Xử lý thực thể đã chết, chờ phân hủy hoàn toàn để hủy Object
        if (currentState == AnimalState.DEAD) {
            velocity.setX(0);
            velocity.setY(0);
            if (stateLockTicks > 0) {
                stateLockTicks--;
            } else {
                // Rớt xác ra trước khi bốc hơi!
                if (shouldSpawnCarcass()) {
                    Carcass meat = new Carcass(this.position, this.size, this.maxEnergy, this.getClass());
                    Environment.getInstance().queueEntity(meat);
                }
                
                destroy(); // Gọi hàm này để isAlive = false -> Xe rác sẽ tới dọn
            }
            return;
        }

        // 1. Giảm thể lực theo thời gian và tăng tuổi
        decreaseEnergy();
        healHP();
        growOlder();

        // 2. Kiểm tra các điều kiện sinh tử cơ bản
        if (hp <= 0 || energy <= 0 || isTooOld()) {
            die();
            return; 
        }

        // ==========================================
        // 3. KIỂM TRA ĐỊA HÌNH TRƯỚC KHI SUY NGHĨ
        // ==========================================
        // Phải biết mình đang đứng ở đâu để chốt tốc độ trước
        TerrainType terrain = Environment.getInstance().getTerrainAt(position, getSize());
        this.terrainSpeedMultiplier = terrain.getSpeedMultiplier();


        // ==========================================
        // 4. UỶ QUYỀN SUY NGHĨ CHO BỘ NÃO
        // ==========================================
        // Lúc này các Strategy bên trong gọi getSpeed() sẽ nhận được tốc độ đã bị trừ do bùn/nước
        if (stateLockTicks > 0) {
            stateLockTicks--;
        } else if (this.brain != null) {
            this.brain.execute(this); 
        }


        // ==========================================
        // 5. THỰC THI DI CHUYỂN
        // ==========================================
        // Vận tốc này vừa được cái "não" ở bước 4 tính toán xong
        position.add(velocity);
    }

    // Sinh đẻ
    @Override
    public Entity reproduce(Animal partner) {
        // 1. Logic dùng chung: Trừ năng lượng của bố mẹ (Chỉ cần viết 1 lần ở đây)
        this.setEnergy(this.getEnergy() * 0.5);
        partner.setEnergy(partner.getEnergy() * 0.5);

        // 2. Tính toán vị trí đẻ chung
        Vector2D babyPos = new Vector2D(
            this.getPosition().getX() + 15,
            this.getPosition().getY() + 15
        );

        // 3. Gọi hàm trừu tượng để lấy con non tương ứng với loài
        return createBaby(babyPos);
    }

    // Khai báo hàm trừu tượng bắt buộc các class con phải tự trả về loài của nó
    protected abstract Entity createBaby(Vector2D position);

    // Hàm giảm năng lượng tiêu hao cơ bản theo thời gian
    private void decreaseEnergy() {
        this.energy -= SimulationConstant.ENERGY_DECAY_PER_TICK;
    }
    // Hàm hồi máu cơ bản
    private void healHP() {
        this.hp += SimulationConstant.HP_REGEN_PER_TICK;
        if (this.hp > this.maxHp) {
            this.hp = this.maxHp;
        }
    }

    // --- CÁC HÀM KIỂM TRA ĐIỀU KIỆN PHÁT TRIỂN & SINH SẢN (Bản cũ) ---
    public boolean isReproductiveAge() {
        // Đạt độ tuổi sinh sản từ 8% đến 80% vòng đời tối đa
        return age >= maxAge * 0.08 && age <= maxAge * 0.8;
    }

    public boolean canMate() {
        return isReproductiveAge() && energy > maxEnergy * 0.5;
    }

    public boolean canMateWith(Animal other) {
        return other != null
            && getClass() == other.getClass()       // Phải cùng loài sinh học
            && gender != other.gender               // Khác biệt giới tính (Đực - Cái)
            && isReproductiveAge()                  // Bản thân sẵn sàng về tuổi tác
            && other.isReproductiveAge()            // Đối phương sẵn sàng về tuổi tác
            && energy > maxEnergy * 0.5             // Thể trạng bản thân khỏe mạnh
            && other.energy > other.maxEnergy * 0.5; // Thể trạng đối phương khỏe mạnh
    }

    public void startAttackState() {
        setTemporaryState(AnimalState.ATTACKING, SimulationConstant.ATTACK_STATE_DURATION);
    }

    public void receiveDamage(double damage) {
        if (currentState == AnimalState.DEAD) return;

        this.hp -= damage;
        if (this.hp <= 0) {
            die();
        } else {
            setTemporaryState(AnimalState.HURT, SimulationConstant.HURT_STATE_DURATION);
        }
    }

    public void die() {
        this.hp = 0;
        this.currentState = AnimalState.DEAD;
        this.stateLockTicks = SimulationConstant.DEAD_STATE_DURATION;
        this.velocity.setX(0);
        this.velocity.setY(0);
        EventManager.animalDied(getClass().getSimpleName());
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

    // --- HỆ THỐNG GETTER / SETTER ---
    public void setBrain(SurvivalStrategy newBrain) { this.brain = newBrain; }
    public SurvivalStrategy getBrain() { return brain; }

    public AnimalState getCurrentState() { return currentState; }
    public void setCurrentState(AnimalState currentState) {
        if (this.currentState == AnimalState.DEAD) return;
        if (stateLockTicks > 0 && currentState != AnimalState.DEAD) return;
        this.currentState = currentState;
    }

    public Vector2D getVelocity() { return velocity; }
    public double getSpeed() { return speed * terrainSpeedMultiplier; }
    public void setSpeed(double speed) { this.speed = speed; }

    public double getVisionRadius() { return visionRadius; }
    public boolean canEnterWater() { return canEnterWater; }
    public boolean requiresWater() { return requiresWater; }

    public double getEnergy() { return energy; }
    public double getMaxEnergy() { return maxEnergy; }
    public void setEnergy(double energy) {
        this.energy = Math.min(energy, maxEnergy);
    }

    public double getHp() { return hp; }
    public double getMaxHp() { return maxHp; }
    public void setHp(double x) {
        if (x < this.hp) {
            receiveDamage(this.hp - x);
        } else {
            this.hp = x;
        }
    }
    
    public int getHidingTicks() { return hidingTicks; }
    public void setHidingTicks(int hidingTicks) { this.hidingTicks = hidingTicks; }

    public boolean hasJustLeftBush() { return justLeftBush; }
    public void setJustLeftBush(boolean value) { this.justLeftBush = value; }

    public double getTerrainSpeedMultiplier() { return terrainSpeedMultiplier; }
    public boolean isFemale() { return gender == Gender.FEMALE; }
    public boolean isMale() { return gender == Gender.MALE; }

    public int getPassiveRestTimer() {
        return passiveRestTimer;
    }

    public void setPassiveRestTimer(int passiveRestTimer) {
        this.passiveRestTimer = passiveRestTimer;
    }

    public int getWanderTimer() {
        return wanderTimer;
    }

    public void setWanderTimer(int wanderTimer) {
        this.wanderTimer = wanderTimer;
    }

    public Vector2D getWanderDirection() {
        return wanderDirection;
    }
    
    public int getFlockingTimer() { 
        return flockingTimer; 
    }
    public void setFlockingTimer(int flockingTimer) { 
        this.flockingTimer = flockingTimer; 
    }
    public int getRestingTimer() { 
        return restingTimer; 
    }
    public void setRestingTimer(int restingTimer) { 
        this.restingTimer = restingTimer; 
    }
    public boolean isRestingFromFlock() { 
        return isRestingFromFlock; 
    }
    public void setRestingFromFlock(boolean isRestingFromFlock) { 
        this.isRestingFromFlock = isRestingFromFlock; 
    }
    public void setWanderDirection(Vector2D wanderDirection) {
        this.wanderDirection = wanderDirection;
    }
}