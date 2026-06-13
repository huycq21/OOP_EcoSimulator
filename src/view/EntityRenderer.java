package view;

import model.Animal;
import model.Entity;

import java.awt.*;

public final class EntityRenderer {

    private EntityRenderer() {}

    public static void drawHealthBar(
            Graphics2D g,
            Animal animal,
            int cx,
            int cy,
            int size
    ) {
        if (animal.getCurrentState() == model.AnimalState.DEAD) {
            return;
        }

        int barWidth = Math.max(18, size * 2);
        int barHeight = 4;

        int x = cx - barWidth / 2;
        int y = cy - size - 8;

        double percent =
                Math.max(
                        0,
                        Math.min(
                                1,
                                animal.getHp() / animal.getMaxHp()
                        )
                );

        int fillWidth =
                (int) Math.round(barWidth * percent);

        g.setColor(new Color(20,20,20,180));
        g.fillRect(
                x - 1,
                y - 1,
                barWidth + 2,
                barHeight + 2
        );

        g.setColor(new Color(154,35,35));
        g.fillRect(x,y,barWidth,barHeight);

        g.setColor(new Color(58,190,74));
        g.fillRect(x,y,fillWidth,barHeight);
    }

    public static void drawDefault(
            Graphics2D g,
            int cx,
            int cy,
            int size
    ) {
        g.setColor(Color.WHITE);

        g.fillOval(
                cx - size / 2,
                cy - size / 2,
                size,
                size
        );
    }

    public static void drawRabbit(Graphics2D g, int cx, int cy, int size) {
        int bodyW = size, bodyH = Math.max(12, size - 4);
        g.setColor(new Color(245, 245, 238));
        g.fillOval(cx - bodyW/2, cy - bodyH/2, bodyW, bodyH);
        g.fillOval(cx + size/5, cy - size/3, size/2, size/2);
        g.fillRoundRect(cx + size/4, cy - size, size/6, size/2, 8, 8);
        g.fillRoundRect(cx + size/2, cy - size, size/6, size/2, 8, 8);
        g.setColor(new Color(255, 178, 190));
        g.fillRoundRect(cx + size/4 + 2, cy - size + 4, Math.max(2, size/10), size/3, 6, 6);
        g.fillRoundRect(cx + size/2 + 2, cy - size + 4, Math.max(2, size/10), size/3, 6, 6);
        g.setColor(Color.BLACK);
        g.fillOval(cx + size/2, cy - size/5, 3, 3);
    }

    public static void drawWolf(Graphics2D g, int cx, int cy, int size) {
        int bodyW = size + 6, bodyH = Math.max(14, size - 2);
        int x = cx - bodyW/2;
        g.setColor(new Color(91, 101, 111));
        g.fillOval(x, cy - bodyH/2, bodyW, bodyH);
        Polygon head = new Polygon();
        head.addPoint(cx + bodyW/2 - 2, cy - bodyH/3);
        head.addPoint(cx + bodyW/2 + size/2, cy);
        head.addPoint(cx + bodyW/2 - 2, cy + bodyH/3);
        g.fillPolygon(head);
        Polygon ear = new Polygon();
        ear.addPoint(cx + bodyW/4, cy - bodyH/2);
        ear.addPoint(cx + bodyW/3, cy - bodyH);
        ear.addPoint(cx + bodyW/2, cy - bodyH/3);
        g.fillPolygon(ear);
        g.setColor(new Color(70, 78, 86));
        Polygon tail = new Polygon();
        tail.addPoint(x + 4, cy - 3); tail.addPoint(x - size/2, cy - bodyH/2); tail.addPoint(x, cy + bodyH/3);
        g.fillPolygon(tail);
        g.setColor(Color.BLACK);
        g.fillOval(cx + bodyW/2 + size/4, cy - 3, 4, 4);
    }
}