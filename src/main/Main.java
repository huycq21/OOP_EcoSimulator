package main;
import view.SimulationPanel;
import controller.SimulationEngine;
import util.SoundManager;
import javax.sound.sampled.Clip;

import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
        // 1. Tạo "Khung gỗ" (Cửa sổ)
        JFrame frame = new JFrame("Mô phỏng Hệ sinh thái");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        // 2. Tạo "Tấm bạt" (View)
        SimulationPanel panel = new SimulationPanel();
        frame.add(panel); // Gắn bạt vào khung

        // 3. Hiển thị cửa sổ trước khi chạy logic
        frame.setVisible(true);

        // 4. Tạo "Bộ não" (Controller) và truyền View vào cho nó điều khiển
        SimulationEngine engine = new SimulationEngine(panel);
        
        // 5. Bấm nút bắt đầu
        engine.start();
    }
}