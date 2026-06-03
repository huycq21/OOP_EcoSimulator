package model.plant;

import model.Vector2D;

public class VinePlant extends Plant {
    private static final int GROWTH_STAGE_DURATION_TICKS = 600;
    private static final int VINE_STAGE_COUNT = 4;

    public VinePlant(Vector2D position) {
        super(position, 12.0, 120.0, Integer.MAX_VALUE, "vine", GROWTH_STAGE_DURATION_TICKS);
    }

    @Override
    public void update() {
        if (!isAlive) return;

        growOlder();
        updateGrowth();
    }

    @Override
    protected void updateGrowth() {
        int stageIndex = (age / growthStageDurationTicks) % VINE_STAGE_COUNT;
        if (stageIndex == 0) {
            growthStage = GrowthStage.SEED;
            size = matureSize * 0.30;
        } else if (stageIndex == 1) {
            growthStage = GrowthStage.SPROUT;
            size = matureSize * 0.50;
        } else if (stageIndex == 2) {
            growthStage = GrowthStage.YOUNG;
            size = matureSize * 0.75;
        } else {
            growthStage = GrowthStage.MATURE;
            size = matureSize;
        }
    }
}
