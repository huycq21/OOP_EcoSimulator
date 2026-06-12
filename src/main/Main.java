package main;

import view.SimulationPanel;
import controller.SimulationEngine;
import model.environment.Environment;
import model.environment.map.Jungle;

import javax.swing.JFrame;

public class Main {
     public static void main(String[] args) {
         // Kích thước thế giới logic. SimulationPanel sẽ load hình Forest.tmx cho Jungle.
         double mapWidth = 1800.0;
         double mapHeight = 1600.0;

         Environment mainMap = new Jungle(mapWidth, mapHeight);

         JFrame frame = new JFrame("Mô phỏng Hệ sinh thái - Forest");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setSize(1000, 700);

         SimulationPanel panel = new SimulationPanel(mainMap);
         frame.add(panel);
        
         frame.setLocationRelativeTo(null); 
         frame.setVisible(true);

         SimulationEngine engine = new SimulationEngine(panel, mainMap);
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
