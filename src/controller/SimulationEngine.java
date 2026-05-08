package controller;

import model.environment.Environment;
import model.environment.Map.Jungle;
import view.SimulationPanel;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.List;
import model.Entity;

public class SimulationEngine {
    private SimulationPanel panel;
    private Environment env; 

    public SimulationEngine(SimulationPanel panel) {
        this.panel = panel;
        
        // 1. Khởi tạo một môi trường CỤ THỂ với world lớn hơn cửa sổ render
        this.env = new Jungle(4800, 3600); 
        this.panel.setWorldSize(this.env.getWidth(), this.env.getHeight());
        
        // 2. Kích hoạt Singleton để các con vật ở mọi nơi đều có thể gọi Environment.getInstance()
        Environment.setActiveEnvironment(this.env); 
    }

    public void start() {
        // Tạo một Thread (Luồng) riêng để vòng lặp while(true) không làm đơ giao diện cửa sổ Swing
        Thread gameThread = new Thread(() -> {
            while (true) {
                // ====================================================
                // BƯỚC 1, 2, 3: ĐÃ ĐƯỢC GÓI GỌN TRONG HÀM UPDATE
                // ====================================================
                // Hàm này sẽ tự động: Xây QuadTree -> Gọi Entity.update() -> Xét Va chạm -> Dọn xác chết
                env.update(); 

                // ====================================================
                // BƯỚC 4: VẼ LÊN MÀN HÌNH
                // ====================================================
                // Truyền danh sách thực thể mới nhất qua cho Panel
                List<Entity> renderSnapshot = new ArrayList<>(env.getEntities());
                SwingUtilities.invokeLater(() -> {
                    panel.setEntities(renderSnapshot);
                    panel.repaint(); // Yêu cầu cửa sổ vẽ lại hình ảnh
                });

                // ====================================================
                // BƯỚC 5: NGỦ ĐỂ GIỮ NHỊP 60 FPS
                // ====================================================
                try {
                    Thread.sleep(16); // 1000ms / 60 = ~16.6ms
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        
        gameThread.start(); // Bắt đầu chạy vòng lặp
    }
}
