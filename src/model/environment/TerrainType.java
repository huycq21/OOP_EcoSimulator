package model.environment;

public enum TerrainType {
    FOREST(1.0),
    WATER(0.5);

    private final double speedMultiplier;

    TerrainType(double speedMultiplier) {
        this.speedMultiplier = speedMultiplier;
    }

    public double getSpeedMultiplier() {
        return speedMultiplier;
    }
}
