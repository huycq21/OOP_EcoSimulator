package controller;

public final class SimulationConstant {

    private SimulationConstant() {}

    // ===== GAME LOOP =====

    public static final int TARGET_FPS = 60;
    public static final int FRAME_DELAY_MS = 16;

    // ===== ANIMAL =====
    public static final double CATCH_SIZE = 5;              // Độ lớn có thể coi là lớn (để xét có thể chui vào cỏ, con vật này có thể ăn thực vật này không)
    public static final double ENERGY_DECAY_PER_TICK = 0.02;    // Tốc độ tiêu hao năng lượng
    public static final double HP_REGEN_PER_TICK = 0.01;    // Tốc độ hồi máu

    public static final int DEAD_STATE_DURATION = 42;   // Thời gian trạng thái chết
    public static final int HURT_STATE_DURATION = 20;   // Thời gian trạng thái bị thương
    public static final int ATTACK_STATE_DURATION = 18; // Thời gian trạng thái tấn công

    public static final int RABBIT_HIDE_DURATION = 300;  // Thời gian thỏ ẩn nấp
    public static final int MAX_RABBIT = 24;             // Số lượng thỏ tối đa
    public static final int REPRODUCTION_INTERVAL = 1800;       // Thời gian giữa các lần sinh sản

    public static final int DEFAULT_MAX_AGE = 20000;        // Tuổi tối đa

    // ===== SPAWNER =====

    public static final int MIN_GRASS = 10;
    public static final int MIN_RABBIT = 10;

    // ===== PLANT =====

    public static final int GRASS_MAX_AGE = 1500;
    public static final int TREE_MAX_AGE = 15000;

    // ===== MAP =====

    public static final double EDGE_MARGIN = 80.0;

    public static final int MAX_FLOCK_TICKS = 1200; // Đi đàn tối đa 20 giây
    public static final int REST_TICKS = 360;      // Tách bầy đi dạo 6 giây

    // Lấy số lượng tối thiểu của một loài
    public static int getMinPopulation(String speciesName) {
        switch (speciesName.toLowerCase()) {
            case "grass": return MIN_GRASS; 
            case "rabbit": return MIN_RABBIT;
            case "wolf": return 5;  // Ví dụ: Luôn giữ ít nhất 5 con sói
            case "deer": return 8;
            default: return 0; // Các con khác chết hết thì thôi, không tự sinh ra thêm
        }
    }
    // Lấy số lượng tối đa của một loài (nếu cần thiết)
    public static int getMaxPopulation(String speciesName) {
        switch (speciesName.toLowerCase()) {
            case "rabbit": return MAX_RABBIT;
            case "wolf": return 15; // Đẻ tối đa 15 con sói
            case "deer": return 25;
            default: return 50; // Giới hạn an toàn chung cho các loài chưa cấu hình
        }
    }
}
