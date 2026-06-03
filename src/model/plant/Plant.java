package model.plant;

import model.Entity;
import model.Eatable;
import model.Ageable;
import model.Vector2D;

public abstract class Plant extends Entity implements Eatable, Ageable {
    protected double energyValue; // Lượng dinh dưỡng
    protected int age;            // Tuổi hiện tại (tính bằng số Tick hoặc số Giây)
    protected int maxAge;         // Tuổi thọ tối đa
    protected final double matureSize;
    protected GrowthStage growthStage;
    protected final String speciesKey;
    protected final int growthStageDurationTicks;

    public Plant(Vector2D position, double size, double energyValue, int maxAge) {
        this(position, size, energyValue, maxAge, "plant");
    }

    public Plant(Vector2D position, double size, double energyValue, int maxAge, String speciesKey) {
        this(position, size, energyValue, maxAge, speciesKey, Math.max(1, maxAge / GrowthStage.values().length));
    }

    public Plant(Vector2D position, double size, double energyValue, int maxAge, String speciesKey, int growthStageDurationTicks) {
        super(position, size);
        this.matureSize = size;
        this.energyValue = energyValue;
        this.maxAge = maxAge;
        this.age = 0; // Mới mọc lên thì 0 tuổi
        this.speciesKey = normalizeSpeciesKey(speciesKey);
        this.growthStageDurationTicks = Math.max(1, growthStageDurationTicks);
        this.growthStage = GrowthStage.SEED;
        updateGrowth();
    }

    @Override
    public void update() {
        if (!isAlive) return;

        // Mỗi khung hình, cây sẽ già đi một chút
        growOlder();
        updateGrowth();

        // Nếu quá già, cây tự khô héo và biến mất
        if (isTooOld()) {
            this.destroy(); 
        }
    }

    // --- Triển khai các hàm của Eatable ---
    @Override
    public double getEnergyValue() {
        return energyValue;
    }

    @Override
    public void getEaten() {
        this.destroy(); // Khi bị thỏ cắn, cây lập tức chuyển trạng thái isAlive = false
    }

    // --- Triển khai các hàm của Ageable ---
    @Override
    public void growOlder() {
        this.age++; 
    }

    @Override
    public boolean isTooOld() {
        return this.age >= this.maxAge;
    }

    public GrowthStage getGrowthStage() {
        return growthStage;
    }

    public int getAge() {
        return age;
    }

    public String getSpeciesKey() {
        return speciesKey;
    }

    protected void updateGrowth() {
        GrowthStage[] stages = GrowthStage.values();
        int stageIndex = Math.min(stages.length - 1, age / growthStageDurationTicks);
        growthStage = stages[stageIndex];

        if (growthStage == GrowthStage.SEED) {
            size = matureSize * 0.30;
        } else if (growthStage == GrowthStage.SPROUT) {
            size = matureSize * 0.50;
        } else if (growthStage == GrowthStage.YOUNG) {
            size = matureSize * 0.75;
        } else if (growthStage == GrowthStage.MATURE) {
            size = matureSize;
        } else {
            size = matureSize * 1.15;
        }
    }

    private String normalizeSpeciesKey(String value) {
        if (value == null || value.trim().isEmpty()) return "plant";
        return value.trim().toLowerCase().replace(' ', '_').replace('-', '_');
    }
}
