package controller;

import model.environment.Environment;
import model.environment.map.*;
import view.SimulationPanel;
import model.spawner.Spawner; // Giữ lại import Spawner của đồng nghiệp
import model.Entity;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.List;

public class SimulationEngine {
    private SimulationPanel panel;
    private Environment env; 
    private Spawner spawner; // Thêm biến Spawner của đồng nghiệp

    // SỬA CONSTRUCTOR: Vẫn nhận Environment từ Main (theo ý bạn)
    public SimulationEngine(SimulationPanel panel, Environment env) {
        this.panel = panel;
        
        // 1. Nhận trực tiếp môi trường từ ngoài thay vì khởi tạo cứng Jungle
        this.env = env; 
        
        // 2. Cập nhật lại kích thước cho panel khớp với môi trường
        this.panel.setWorldSize(this.env.getWidth(), this.env.getHeight());
        
        // 3. Kích hoạt Singleton
        Environment.setActiveEnvironment(this.env); 

        // 4. Khởi tạo Spawner (Tính năng mới của đồng nghiệp)
        this.spawner = new Spawner(this.env);
    }

    public void start() {
        // Tạo một Thread (Luồng) riêng để vòng lặp while(true) không làm đơ giao diện
        Thread gameThread = new Thread(() -> {
            double updateAccumulator = 0.0;
            while (true) {
                // ====================================================
                // CẬP NHẬT LOGIC GAME
                // ====================================================
                updateAccumulator += SimulationTime.getTimeScale();
                int updatesThisFrame = Math.min(20, (int) updateAccumulator);
                for (int i = 0; i < updatesThisFrame; i++) {
                    env.update();
                    spawner.update(); // QUAN TRỌNG: Gọi update của Spawner để nó hoạt động
                }
                updateAccumulator -= updatesThisFrame;

                // ====================================================
                // VẼ LÊN MÀN HÌNH
                // ====================================================
                List<Entity> renderSnapshot = new ArrayList<>(env.getEntities());
                SwingUtilities.invokeLater(() -> {
                    panel.setEntities(renderSnapshot);
                    panel.repaint(); 
                });

                // ====================================================
                // NGỦ ĐỂ GIỮ NHỊP FPS
                // ====================================================
                try {
                    // Dùng hằng số chung của đồng nghiệp thay vì fix cứng 16
                    Thread.sleep(SimulationConstant.FRAME_DELAY_MS); 
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        
        gameThread.start(); 
    }
}