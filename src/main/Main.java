package main;
import view.SimulationPanel;
import controller.SimulationEngine;
import model.environment.Environment;
import model.environment.map.Jungle;

import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
         // Kích thước thế giới logic. SimulationPanel sẽ load hình Forest.tmx cho Jungle.
         double mapWidth = 1800.0;
         double mapHeight = 1600.0;

         Environment mainMap = new Jungle(mapWidth, mapHeight);

         JFrame frame = new JFrame("Mô phỏng Hệ sinh thái - Forest");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setSize(1000, 700);

         SimulationPanel panel = new SimulationPanel(mainMap);
         frame.add(panel);
        
         frame.setLocationRelativeTo(null); 
         frame.setVisible(true);

         SimulationEngine engine = new SimulationEngine(panel);
         engine.start();
    }
}