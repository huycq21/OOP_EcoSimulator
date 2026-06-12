package model.environment;

public enum TerrainType {
    NORMAL_DIRT(1.0), // Đất thường (mặc định)
    WATER(0.5),       // Vùng nước
    MUD(0.7),         // Vùng bùn lầy
    ROCK(1.5),        // Đá gồ ghề
    PEN(1.0);         // Khu vực chuồng trại chung (Gộp coop, cowshed, pigsty);   

    private final double speedMultiplier;

    TerrainType(double speedMultiplier) {
        this.speedMultiplier = speedMultiplier;
    }

    public double getSpeedMultiplier() {
        return speedMultiplier;
    }
}
