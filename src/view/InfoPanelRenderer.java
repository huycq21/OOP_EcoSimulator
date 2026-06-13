package view;

import java.awt.*;
import java.util.List;

import model.Entity;
import model.environment.Environment;
import model.plant.Grass;

import model.herbivore.*;
import model.carnivore.*;
import model.apex.*;

public final class InfoPanelRenderer {

    private InfoPanelRenderer() {}

    public static void draw(
            Graphics2D g,
            List<Entity> entities
    ) {

        if (entities == null) return;

        Font oldFont = g.getFont();
        Color oldColor = g.getColor();

        g.setFont(new Font("Arial", Font.BOLD, 16));

        int rabbit = 0;
        int deer = 0;
        int boar = 0;
        int fox = 0;
        int wolf = 0;
        int goat = 0;
        int horse = 0;
        int cheetah = 0;
        int lion = 0;
        int bear = 0;
        int human = 0;
        int grass = 0;

        String weather = "UNKNOWN";

        if (Environment.getInstance() != null
                && Environment.getInstance().getWeather() != null) {

            weather =
                    Environment.getInstance()
                            .getWeather()
                            .getCurrentWeather()
                            .toString();
        }

        for (Entity e : entities) {

            if (e instanceof Rabbit) rabbit++;
            else if (e instanceof Deer) deer++;
            else if (e instanceof Boar) boar++;
            else if (e instanceof Fox) fox++;
            else if (e instanceof Wolf) wolf++;
            else if (e instanceof Goat) goat++;
            else if (e instanceof Horse) horse++;
            else if (e instanceof Cheetah) cheetah++;
            else if (e instanceof Lion) lion++;
            else if (e instanceof Bear) bear++;
            else if (e instanceof Human) human++;
            else if (e instanceof Grass) grass++;
        }

        g.setColor(new Color(0, 0, 0, 180));
        g.fillRoundRect(10, 10, 190, 310, 10, 10);

        g.setColor(Color.WHITE);

        g.drawString("Rabbit : " + rabbit, 20, 35);
        g.drawString("Deer : " + deer, 20, 55);
        g.drawString("Boar : " + boar, 20, 75);
        g.drawString("Fox : " + fox, 20, 95);
        g.drawString("Wolf : " + wolf, 20, 115);
        g.drawString("Goat : " + goat, 20, 135);
        g.drawString("Horse : " + horse, 20, 155);
        g.drawString("Cheetah : " + cheetah, 20, 175);
        g.drawString("Lion : " + lion, 20, 195);
        g.drawString("Bear : " + bear, 20, 215);
        g.drawString("Human : " + human, 20, 235);
        g.drawString("Grass : " + grass, 20, 255);
        g.drawString("Weather : " + weather, 20, 285);

        g.setFont(oldFont);
        g.setColor(oldColor);
    }
}