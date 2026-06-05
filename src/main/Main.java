package main;

import view.ForestTileMap;
import view.SimulationPanel;
import controller.SimulationEngine;
import model.environment.Map.EmptyMap;
import model.environment.Map.Jungle;
import model.environment.Environment;
import model.carnivore.*;
import model.herbivore.*;
import model.plant.*;
import model.Vector2D;

import javax.swing.JFrame;
import java.util.Random; // 1. Import thư viện Random
import java.awt.Dimension;

public class Main {
    public static void main(String[] args) {
        //Khởi tạo kích thước map để dễ tái sử dụng
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

            // Random tọa độ cho Cỏ (từ 0 đến viền map)
            double grassX = rand.nextDouble() * mapWidth;
            double grassY = rand.nextDouble() * mapHeight;
            testMap.addEntity(new Grass(new Vector2D(grassX, grassY)));

            double BerryX = rand.nextDouble() * mapWidth;
            double BerryY = rand.nextDouble() * mapHeight;
            testMap.addEntity(new Berry(new Vector2D(BerryX, BerryY)));

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
        // ForestTileMap tmxLoader = new ForestTileMap("assets/Environment/Forest/Forest.tmx");
        // double mapWidth = tmxLoader.isLoaded() ? tmxLoader.getPixelWidth() : 800.0;
        // double mapHeight = tmxLoader.isLoaded() ? tmxLoader.getPixelHeight() : 600.0;

        // // 2. KHỞI TẠO MAP RỪNG VỚI KÍCH THƯỚC VỪA LẤY
        // Environment mainMap = new Jungle(mapWidth, mapHeight);

        // // 3. THIẾT LẬP GIAO DIỆN
        // JFrame frame = new JFrame("Wild-Life Eco Simulation");
        // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // SimulationPanel panel = new SimulationPanel(mainMap); 
        
        // // Mẹo Swing: Ép Panel phải to đúng bằng kích thước Map, 
        // // sau đó dùng frame.pack() để khung viền cửa sổ tự bọc khít lại, không bị hụt pixel
        // panel.setPreferredSize(new Dimension((int)mapWidth, (int)mapHeight));
        // frame.add(panel);
        // frame.pack(); 
        
        // // Căn giữa màn hình Mac và hiện lên
        // frame.setLocationRelativeTo(null); 
        // frame.setVisible(true);

        // // 4. CHẠY ENGINE
        // SimulationEngine engine = new SimulationEngine(panel, mainMap);
        // engine.start();
    }
}