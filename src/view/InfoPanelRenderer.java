package view;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Entity;
import model.environment.Environment;

public final class InfoPanelRenderer {

    private InfoPanelRenderer() {}

    public static void draw(Graphics2D g, List<Entity> entities) {
        if (entities == null) return;

        Font oldFont = g.getFont();
        Color oldColor = g.getColor();
        g.setFont(new Font("Arial", Font.BOLD, 16));

        // TỰ ĐỘNG đếm mọi thực thể đang sống
        Map<String, Integer> population = new HashMap<>();
        for (Entity e : entities) {
            if (e.isAlive()) {
                String speciesName = e.getClass().getSimpleName();
                population.put(speciesName, population.getOrDefault(speciesName, 0) + 1);
            }
        }

        String weather = "UNKNOWN";
        if (Environment.getInstance() != null && Environment.getInstance().getWeather() != null) {
            weather = Environment.getInstance().getWeather().getCurrentWeather().toString();
        }

        // Khung UI tự co giãn
        int panelHeight = 45 + (population.size() * 20);

        g.setColor(new Color(0, 0, 0, 180));
        g.fillRoundRect(10, 10, 190, panelHeight, 10, 10);
        g.setColor(Color.WHITE);

        int textY = 35;
        for (Map.Entry<String, Integer> entry : population.entrySet()) {
            g.drawString(entry.getKey() + " : " + entry.getValue(), 20, textY);
            textY += 20;
        }

        g.drawString("Weather : " + weather, 20, textY + 10);

        g.setFont(oldFont);
        g.setColor(oldColor);
    }
}