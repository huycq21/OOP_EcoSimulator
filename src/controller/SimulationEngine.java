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

    // Sửa lại Constructor để nhận cả Panel lẫn Environment từ Main
    public SimulationEngine(SimulationPanel panel, Environment env) {
        this.panel = panel;
        
        // 1. Nhận trực tiếp môi trường từ ngoài truyền vào thay vì khởi tạo cứng Jungle
        this.env = env; 
        
        // Cập nhật lại kích thước cho panel khớp với môi trường
        this.panel.setWorldSize(this.env.getWidth(), this.env.getHeight());
        
        // 2. Kích hoạt Singleton để các con vật ở mọi nơi đều có thể gọi Environment.getInstance()
        Environment.setActiveEnvironment(this.env); 
    }

    public void start() {
        // ... (Giữ nguyên toàn bộ phần hàm start() của bạn, code Thread và vòng lặp update đang rất chuẩn rồi)
        Thread gameThread = new Thread(() -> {
            while (true) {
                env.update(); 

                List<Entity> renderSnapshot = new ArrayList<>(env.getEntities());
                SwingUtilities.invokeLater(() -> {
                    panel.setEntities(renderSnapshot);
                    panel.repaint(); 
                });

                try {
                    Thread.sleep(16); 
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        
        gameThread.start(); 
    }
}