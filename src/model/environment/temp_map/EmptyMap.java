package model.environment.map;

import model.environment.Environment;

public class EmptyMap extends Environment {
    
    // Khởi tạo một map hoàn toàn trống với kích thước truyền vào
    public EmptyMap(double width, double height) {
        // Gọi constructor của lớp cha (Environment)
        super(width, height);
        
        // Cố tình ĐỂ TRỐNG phần này:
        // - KHÔNG gọi TmxCollisionLoader (để không bị vướng cây cối tàng hình)
        // - KHÔNG spawn động vật ngẫu nhiên
    }
}