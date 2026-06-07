package main;

import view.ForestTileMap;
import view.SimulationPanel;
import controller.SimulationEngine;
import model.environment.Environment;
import model.environment.map.EmptyMap;
import model.environment.map.Jungle;
import model.carnivore.*;
import model.herbivore.*;
import model.plant.*;
import model.Vector2D;

// Thư viện âm thanh từ nhánh của đồng nghiệp
import util.SoundManager; 
import javax.sound.sampled.Clip; 

import javax.swing.JFrame;
import java.awt.Dimension;

public class Main {
     public static void main(String[] args) {
         // 1. Khởi tạo kích thước map để dễ tái sử dụng
         double mapWidth = 800.0;
         double mapHeight = 600.0;

         // 2. Tạo Map trống kích thước 800x600 (Hoặc dùng Jungle tùy bạn)
         Environment testMap = new EmptyMap(mapWidth, mapHeight);
        
         // (Vòng lặp spawn thú ngẫu nhiên ở đây đã được xóa đi cho gọn, 
         // vì class Spawner của chúng ta đã làm nhiệm vụ đó rồi!)

         // 3. Khởi tạo giao diện và truyền testMap vào
         JFrame frame = new JFrame("Mô phỏng Hệ sinh thái ");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setSize((int)mapWidth, (int)mapHeight);

         // Gọi đúng Constructor có tham số mà chúng ta đã gộp
         SimulationPanel panel = new SimulationPanel(testMap); 
         frame.add(panel);
        
         // Căn giữa màn hình Mac và hiện lên
         frame.setLocationRelativeTo(null); 
         frame.setVisible(true);

         // 4. Khởi tạo Engine (Gọi đúng Constructor 2 tham số) và chạy
         SimulationEngine engine = new SimulationEngine(panel, testMap);
         engine.start();
    }
    // public static void main(String[] args) {
    //     // 1. Khởi tạo kích thước map 
    //     double mapWidth = 1800.0;
    //     double mapHeight = 1600.0;

    //     // ==========================================
    //     // 2. SỬ DỤNG MAP "XỊN" (JUNGLE) THAY VÌ EMPTY MAP
    //     // ==========================================
    //     Environment mainMap = new Jungle(mapWidth, mapHeight);

    //     // 3. Khởi tạo giao diện và truyền mainMap vào
    //     JFrame frame = new JFrame("Mô phỏng Hệ sinh thái - Nhóm 12");
    //     frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    //     frame.setSize((int)mapWidth, (int)mapHeight);

    //     // Lúc này SimulationPanel nhận thấy đây không phải EmptyMap, 
    //     // nó sẽ tự động load file hình ảnh Forest.tmx!
    //     SimulationPanel panel = new SimulationPanel(mainMap); 
    //     frame.add(panel);
        
    //     // Căn giữa màn hình Mac và hiện lên
    //     frame.setLocationRelativeTo(null); 
    //     frame.setVisible(true);

    //     // 4. Khởi tạo Engine và chạy
    //     SimulationEngine engine = new SimulationEngine(panel, mainMap);
    //     engine.start();
    //}
}