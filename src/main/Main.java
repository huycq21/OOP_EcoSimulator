package main;

import view.SimulationPanel;
import controller.SimulationEngine;
import model.environment.Map.EmptyMap;
import model.environment.Environment;
import model.carnivore.Wolf;
import model.herbivore.Rabbit;
import model.Vector2D;

import javax.swing.JFrame;
import java.util.Random; // 1. Import thư viện Random

public class Main {
    public static void main(String[] args) {
        // Khởi tạo kích thước map để dễ tái sử dụng
        double mapWidth = 800.0;
        double mapHeight = 600.0;

        // Tạo Map trống kích thước 800x600
        Environment testMap = new EmptyMap(mapWidth, mapHeight);
        
        // 2. Khởi tạo đối tượng Random
        Random rand = new Random();
        
        // Sinh ra 30 con Sói và 30 con Thỏ ở vị trí ngẫu nhiên
        for (int i = 0; i < 30; i++) {
            // Random tọa độ cho Sói (từ 0 đến viền map)
            double wolfX = rand.nextDouble() * mapWidth;
            double wolfY = rand.nextDouble() * mapHeight;
            testMap.addEntity(new Wolf(new Vector2D(wolfX, wolfY)));
            
            // Random tọa độ cho Thỏ (từ 0 đến viền map)
            double rabbitX = rand.nextDouble() * mapWidth;
            double rabbitY = rand.nextDouble() * mapHeight;
            testMap.addEntity(new Rabbit(new Vector2D(rabbitX, rabbitY)));
        }

        // Khởi tạo giao diện và truyền testMap vào
        JFrame frame = new JFrame("Test Tương Tác");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Cast (ép kiểu) về int vì JFrame setSize nhận int
        frame.setSize((int)mapWidth, (int)mapHeight);

        SimulationPanel panel = new SimulationPanel(testMap); 
        frame.add(panel);
        frame.setVisible(true);

        // Khởi tạo Engine và chạy
        SimulationEngine engine = new SimulationEngine(panel, testMap);
        engine.start();
    }
}