package model;

public enum AnimalState {
    WANDERING,  // Đi lang thang/ngẫu nhiên
    CHASING,    // Đuổi theo con mồi
    FLEEING,    // Bỏ chạy khỏi kẻ thù
    EATING,     // Đang ăn
    SLEEPING,   // Đang ngủ (hồi năng lượng)
    HIDING,     // Đang trốn trong bụi rậm
    DEAD        // Đã chết (chờ thành Carcass hoặc bị xóa)
}