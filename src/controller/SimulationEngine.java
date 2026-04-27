package controller;

import model.Entity;
import view.SimulationPanel;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class SimulationEngine {
    private List<Entity> entities;     // Phần Model
    private SimulationPanel viewPanel; // Phần View

    public SimulationEngine(SimulationPanel viewPanel) {
        this.viewPanel = viewPanel;
        this.entities = new ArrayList<>();

        // Truyền dữ liệu cho View để nó biết đường vẽ
        this.viewPanel.setEntities(entities);
    }

    // Hàm bắt đầu vòng lặp mô phỏng
    public void start() {
        Timer timer = new Timer(16, e -> {
            // Bước 1: Controller cập nhật Logic (Model)
            for (Entity entity : entities) {
                entity.update();
            }

            // Bước 2: Controller ra lệnh cho View vẽ lại khung hình mới
            viewPanel.repaint(); 
        });
        timer.start();
    }
}