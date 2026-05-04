package controller;

import model.environment.Environment;
import model.environment.Map.Jungle;
import view.SimulationPanel;

public class SimulationEngine {
    private SimulationPanel panel;
    private Environment env; 

    public SimulationEngine(SimulationPanel panel) {
        this.panel = panel;
        
        // 1. Khởi tạo một môi trường CỤ THỂ (Ví dụ: Rừng kích thước 800x600)
        this.env = new Jungle(800, 600); 
        
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
                panel.setEntities(env.getEntities()); 
                panel.repaint(); // Yêu cầu cửa sổ vẽ lại hình ảnh

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