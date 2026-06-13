package model;

public enum AnimalState {
    WANDERING,  // Đi lang thang/ngẫu nhiên
    SNEAKING,   // Rón rén
    CHASING,    // Đuổi theo con mồi
    FLEEING,    // Bỏ chạy khỏi kẻ thù
    FORAGING,   // Đi tìm thức ăn
    ATTACKING,  // Đang tấn công
    HURT,       // Vừa bị tấn công
    EATING,     // Đang ăn
    SLEEPING,   // Đang ngủ (hồi năng lượng)
    HIDING,     // Đang trốn trong bụi rậm
    ALERT,      // Cảnh giác/phát hiện nguy hiểm
    DEAD,        // Đã chết (chờ thành Carcass hoặc bị xóa)
}
