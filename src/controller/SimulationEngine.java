package controller;

import model.environment.Environment;
import model.environment.map.*;
import view.SimulationPanel;
import model.spawner.Spawner; 
import model.Entity;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.List;

public class SimulationEngine {
    private SimulationPanel panel;
    private Environment env; 
    private Spawner spawner; 

    // SỬA CONSTRUCTOR: Nhận trực tiếp Environment từ Main
    public SimulationEngine(SimulationPanel panel, Environment env) {
        this.panel = panel;
        
        // 1. Nhận trực tiếp môi trường từ ngoài
        this.env = env; 
        
        // 3. Kích hoạt Singleton
        Environment.setActiveEnvironment(this.env); 

        // 4. Khởi tạo Spawner 
        this.spawner = new Spawner(this.env);
    }

    public void start() {
        // Tạo một Thread (Luồng) riêng để vòng lặp while(true) không làm đơ giao diện
        Thread gameThread = new Thread(() -> {
            double updateAccumulator = 0.0;
            while (true) {
                // ====================================================
                // 1. CẬP NHẬT LOGIC GAME (VẬT LÝ, AI)
                // ====================================================
                updateAccumulator += SimulationTime.getTimeScale();
                int updatesThisFrame = Math.min(20, (int) updateAccumulator);
                for (int i = 0; i < updatesThisFrame; i++) {
                    env.update();
                    spawner.update(); 
                }
                updateAccumulator -= updatesThisFrame;

                // ====================================================
                // 2. VẼ LÊN MÀN HÌNH (ĐÃ FIX LỖI TRÀN RAM)
                // ====================================================
                List<Entity> renderSnapshot = new ArrayList<>(env.getEntities());
                
                try {
                    // Dùng invokeAndWait: Ép vòng lặp game phải CHỜ giao diện vẽ xong
                    // Không cho phép ném thêm việc nếu Card đồ họa/CPU chưa xử lý kịp
                    SwingUtilities.invokeAndWait(() -> {
                        panel.setEntities(renderSnapshot);
                        panel.repaint(); 
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // ====================================================
                // 3. NGỦ ĐỂ GIỮ NHỊP FPS
                // ====================================================
                try {
                    Thread.sleep(SimulationConstant.FRAME_DELAY_MS); 
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        
        gameThread.start(); 
    }
}