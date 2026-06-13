package view;

import model.Vector2D;
import model.environment.Environment;
import model.environment.obstacle.Bush;
import model.environment.obstacle.OldTree;
import model.environment.obstacle.Rock;
import model.plant.Grass;

public final class BuildPlacementService {

    private BuildPlacementService() {}

    public static void place(
            BuildMode mode,
            double worldX,
            double worldY,
            Environment env
    ) {
        switch (mode) {

            case FOOD_PLANT -> {
                Grass g = new Grass(
                        new Vector2D(worldX, worldY)
                );

                g.setRuntimePlaced(true);
                env.queueEntity(g);
            }

            case BUSH -> {
                Bush b = new Bush(
                        new Vector2D(worldX, worldY),
                        18
                );

                b.setRuntimePlaced(true);
                env.queueEntity(b);
            }

            case TREE -> {
                OldTree t =
                        new OldTree(
                                new Vector2D(worldX, worldY)
                        );

                t.setRuntimePlaced(true);
                env.queueEntity(t);
            }

            case ROCK -> {
                Rock r =
                        new Rock(
                                new Vector2D(worldX, worldY)
                        );

                r.setRuntimePlaced(true);
                env.queueEntity(r);
            }
        }
    }
}