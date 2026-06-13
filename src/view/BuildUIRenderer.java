package view;

import java.awt.*;
import view.BuildMode;

public final class BuildUIRenderer {
    private BuildUIRenderer() {}
    public static void draw(Graphics2D g, int panelHeight, BuildMode buildMode) {
        drawBuildButton(g, "Plant", 20, panelHeight - 80, buildMode == BuildMode.FOOD_PLANT);
        drawBuildButton(g, "Bush", 150, panelHeight - 80, buildMode == BuildMode.BUSH);
        drawBuildButton(g, "Tree", 280, panelHeight - 80, buildMode == BuildMode.TREE);
        drawBuildButton(g, "Rock", 410, panelHeight - 80, buildMode == BuildMode.ROCK);
    }

    private static void drawBuildButton(Graphics2D g, String text, int x, int y, boolean active) {
        Color oldColor = g.getColor();
        Font oldFont = g.getFont();
        g.setColor(active ? new Color(90, 180, 90) : new Color(40, 40, 40, 220));
        g.fillRoundRect(x, y, 110, 50, 18, 18);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, x + (110 - fm.stringWidth(text)) / 2, y + 31);

        // trả lại trạng thái Graphics
        g.setFont(oldFont);
        g.setColor(oldColor);
    }
}