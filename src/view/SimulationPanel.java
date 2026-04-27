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
            g.setColor(Color.WHITE);
            g.fillOval((int)e.getX(), (int)e.getY(), e.getSize(), e.getSize());
        }
    }
}