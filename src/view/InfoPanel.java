package view;

import model.Entity;
import model.carnivore.Fox;
import model.carnivore.Wolf;
import model.herbivore.Boar;
import model.herbivore.Deer;
import model.herbivore.Rabbit;
import model.plant.Grass;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class InfoPanel extends JPanel {

    private int rabbitCount;
    private int deerCount;
    private int boarCount;
    private int foxCount;
    private int wolfCount;
    private int grassCount;
    private String season = "";

    public InfoPanel() {

        setPreferredSize(new Dimension(220, 120));

        setOpaque(true);

        setBackground(new Color(0,0,0,170));
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public void updateStats(List<Entity> entities) {

        rabbitCount = 0;
        deerCount = 0;
        boarCount = 0;
        foxCount = 0;
        wolfCount = 0;
        grassCount = 0;

        for(Entity e : entities) {

            if(e instanceof Rabbit) rabbitCount++;

            else if(e instanceof Deer) deerCount++;

            else if(e instanceof Boar) boarCount++;

            else if(e instanceof Fox) foxCount++;

            else if(e instanceof Wolf) wolfCount++;

            else if(e instanceof Grass) grassCount++;
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(Color.WHITE);

        int y = 20;

        g2.drawString("=== ECOSYSTEM ===", 10, y);

        y += 20;
        g2.drawString("Rabbit : " + rabbitCount, 10, y);

        y += 20;
        g2.drawString("Deer : " + deerCount, 10, y);

        y += 20;
        g2.drawString("Boar : " + boarCount, 10, y);

        y += 20;
        g2.drawString("Fox : " + foxCount, 10, y);

        y += 20;
        g2.drawString("Wolf : " + wolfCount, 10, y);

        y += 20;
        g2.drawString("Grass : " + grassCount, 10, y);

        y += 20;
        g2.drawString("Season : " + season, 10, y);
    }
}