package model.environment.map;

import model.environment.Environment;
import model.environment.TmxCollisionLoader;
import model.environment.TmxPlantLoader;
import model.environment.obstacle.Bush;

public class Jungle extends Environment {

    // Khởi tạo Rừng nhiệt đới với kích thước truyền vào
    public Jungle(double width, double height) {
        super(width, height);
        
        // 1. Nạp toàn bộ địa hình, vách ngăn, vùng nước, chuồng trại từ file TMX
        TmxCollisionLoader.loadInto(this, "assets/Environment/Forest/Forest.tmx");
        
        // 2. Nạp thực vật tĩnh từ file TMX
        TmxPlantLoader.loadInto(this, "assets/Environment/Forest/Forest.tmx");

        // Ghi chú: Việc spawn thú di động (Thỏ, Sói, Cá...) giờ đây 
        // giao toàn quyền cho class Spawner quản lý để tránh rác code ở Environment.
    }
}
