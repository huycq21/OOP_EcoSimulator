package model.plant;

import model.Vector2D;

public class TreePlant extends Plant {
    private static final int GROWTH_STAGE_DURATION_TICKS = 600;
    private static final int TREE_MAX_AGE_TICKS = GROWTH_STAGE_DURATION_TICKS * GrowthStage.values().length;

    public TreePlant(Vector2D position) {
        super(position, 12.0, 150.0, TREE_MAX_AGE_TICKS,"Tree",  GROWTH_STAGE_DURATION_TICKS);
    }
}
