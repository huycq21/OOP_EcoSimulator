package view;

import model.plant.Berry;
import model.plant.Grass;
import model.plant.GrowthStage;
import model.plant.Mushroom;
import model.plant.Plant;
import model.plant.SmallTree;
import model.plant.TreePlant;

import java.awt.Color;
import java.awt.Graphics2D;

public final class PlantRenderer {
    public static void drawGrass(Graphics2D g, int cx, int cy, int size) {
        g.setColor(new Color(55,154,58));
        int bh = Math.max(5, size);
        g.drawLine(cx,cy,cx,cy-bh);
        g.drawLine(cx,cy,cx-size/3,cy-bh*2/3);
        g.drawLine(cx,cy,cx+size/3,cy-bh*2/3);
    }

    public static void drawBerry(Graphics2D g, int cx, int cy, int size, boolean hasFruits) {
        drawGrass(g, cx, cy, size);
        if (!hasFruits) return;
        g.setColor(new Color(182,38,62));
        int bs = Math.max(3,size/4);
        g.fillOval(cx-bs,cy-size,bs,bs); g.fillOval(cx+bs/2,cy-size*3/4,bs,bs);
    }

    public static void drawMushroom(Graphics2D g, int cx, int cy, int size) {
        int sw = Math.max(3,size/4), sh = Math.max(5,size/2);
        g.setColor(new Color(232,220,185)); g.fillRoundRect(cx-sw/2,cy-sh/2,sw,sh,4,4);
        g.setColor(new Color(185,45,48));   g.fillArc(cx-size/2,cy-sh,size,size,0,180);
    }

    public static void drawSmallTree(Graphics2D g, int cx, int cy, int size, GrowthStage stage) {
        int th = Math.max(6, size/2), tw = Math.max(3, size/7), cs = Math.max(8, size);
        if (stage == GrowthStage.SEED) { g.setColor(new Color(96,67,38)); g.fillOval(cx-size/8,cy-size/8,Math.max(3,size/4),Math.max(3,size/4)); return; }
        g.setColor(new Color(104,70,38)); g.fillRoundRect(cx-tw/2,cy-th/2,tw,th,4,4);
        g.setColor(stage==GrowthStage.OLD ? new Color(91,128,55) : new Color(42,139,61));
        g.fillOval(cx-cs/2,cy-th/2-cs/2,cs,cs);
        g.setColor(new Color(29,104,49)); g.fillOval(cx-cs/3,cy-th/2-cs/3,cs/2,cs/2);
    }
}
