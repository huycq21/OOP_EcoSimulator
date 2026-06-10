package controller;

public final class SimulationConstant {

    private SimulationConstant() {}

    // ===== GAME LOOP =====

    public static final int TARGET_FPS = 60;
    public static final int FRAME_DELAY_MS = 16;

    // ===== ANIMAL =====

    public static final double ENERGY_DECAY_PER_TICK = 0.02;

    public static final int DEAD_STATE_DURATION = 42;
    public static final int HURT_STATE_DURATION = 20;
    public static final int ATTACK_STATE_DURATION = 18;

    public static final int RABBIT_HIDE_DURATION = 300;
    public static final int MAX_RABBIT = 24;
    public static final int REPRODUCTION_INTERVAL = 1800;

    public static final int DEFAULT_MAX_AGE = 120000;

    // ===== SPAWNER =====

    public static final int MIN_GRASS = 90;
    public static final int MIN_RABBIT = 12;
    public static final int MAX_DEER = 20;
    public static final int MAX_BOAR = 15;
    public static final int MAX_FOX = 10;
    public static final int MAX_WOLF = 8;

    // ===== PLANT =====

    public static final int GRASS_MAX_AGE = 1500;
    public static final int TREE_MAX_AGE = 15000;

    // ===== MAP =====

    public static final double EDGE_MARGIN = 80.0;
}