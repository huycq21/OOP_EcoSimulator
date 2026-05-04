package view;

import model.Entity;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class SimulationPanel extends JPanel {
    private List<Entity> entities;

    public void setEntities(List<Entity> entities) {
        this.entities = entities;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(new Color(34, 139, 34));
        if (entities == null) return;

        for (Entity e : entities) {
            g.setColor(Color.WHITE); // Bạn có thể đổi màu tùy theo loài sau này
            
            // Lỗi 1 & 2: Phải gọi qua getPosition(). Vị trí vẽ là góc trên cùng bên trái của hình chữ nhật bao quanh hình tròn
            int size = (int) e.getSize();
            int x = (int) (e.getPosition().getX() - size / 2.0); 
            int y = (int) (e.getPosition().getY() - size / 2.0);
            
            // Lỗi 3: fillOval chỉ nhận số nguyên (int)
            g.fillOval(x, y, size, size);
        }
    }
}