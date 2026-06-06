package model.environment;

import model.Vector2D;
import model.plant.TreePlant;
import model.plant.VinePlant;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TmxPlantLoader {
    private static final String PLANTS_LAYER = "plants";
    private static final String TREES_LAYER = "trees";
    private static final String VINE_LAYER = "vine";

    public static int loadInto(Environment environment, String tmxPath) {
        int count = 0;

        try {
            Document document = parseXml(new File(tmxPath));
            Element map = document.getDocumentElement();
            int tileWidth = readInt(map, "tilewidth");
            int tileHeight = readInt(map, "tileheight");
            TileBounds bounds = readTileBounds(map);
            double offsetX = -bounds.minX * tileWidth;
            double offsetY = -bounds.minY * tileHeight;

            NodeList mapChildren = map.getChildNodes();
            for (int i = 0; i < mapChildren.getLength(); i++) {
                Node node = mapChildren.item(i);
                if (!(node instanceof Element)) continue;

                Element layer = (Element) node;
                String layerName = layer.getAttribute("name").trim().toLowerCase();

                if ("objectgroup".equals(node.getNodeName())) {
                    if (!PLANTS_LAYER.equals(layerName) && !TREES_LAYER.equals(layerName) && !VINE_LAYER.equals(layerName)) continue;
                    count += loadObjectLayer(environment, layer, layerName, offsetX, offsetY);
                } else if ("layer".equals(node.getNodeName())) {
                    if (!VINE_LAYER.equals(layerName)) continue;
                    count += loadTileLayer(environment, layer, VINE_LAYER, tileWidth, tileHeight, bounds.minX, bounds.minY);
                }
            }

            if (count > 0) {
                System.out.println("Loaded TMX plants: " + count + " plants from " + tmxPath);
            }
        } catch (Exception e) {
            System.err.println("Cannot load TMX plants: " + tmxPath);
            e.printStackTrace();
        }

        return count;
    }

    private static int loadObjectLayer(Environment environment, Element objectGroup, String layerName, double offsetX, double offsetY) {
        int count = 0;
        NodeList objects = objectGroup.getChildNodes();
        for (int j = 0; j < objects.getLength(); j++) {
            Node objectNode = objects.item(j);
            if (!(objectNode instanceof Element) || !"object".equals(objectNode.getNodeName())) continue;

            Element object = (Element) objectNode;
            String speciesKey = readSpeciesKey(object);
            if (VINE_LAYER.equals(layerName) && "small_tree".equals(speciesKey)) {
                speciesKey = "nho";
            }
            Vector2D position = readPlantPosition(object, offsetX, offsetY);
            environment.addEntity(createPlant(position, speciesKey));
            count++;
        }
        return count;
    }

    private static int loadTileLayer(Environment environment, Element layer, String speciesKey,
                                     int tileWidth, int tileHeight, int minTileX, int minTileY) {
        Element data = firstChild(layer, "data");
        if (data == null) return 0;

        List<TileCell> cells = new ArrayList<>();
        NodeList chunks = data.getChildNodes();
        for (int i = 0; i < chunks.getLength(); i++) {
            Node chunkNode = chunks.item(i);
            if (!(chunkNode instanceof Element) || !"chunk".equals(chunkNode.getNodeName())) continue;

            Element chunk = (Element) chunkNode;
            int chunkX = readInt(chunk, "x");
            int chunkY = readInt(chunk, "y");
            int chunkWidth = readInt(chunk, "width");
            String[] rawGids = chunk.getTextContent().trim().split(",");

            for (int tileIndex = 0; tileIndex < rawGids.length; tileIndex++) {
                int gid = parseGid(rawGids[tileIndex]);
                if (gid == 0) continue;

                int localX = tileIndex % chunkWidth;
                int localY = tileIndex / chunkWidth;
                cells.add(new TileCell(chunkX + localX, chunkY + localY));
            }
        }

        return loadTileComponents(environment, cells, speciesKey, tileWidth, tileHeight, minTileX, minTileY);
    }

    private static int loadTileComponents(Environment environment, List<TileCell> cells, String speciesKey,
                                          int tileWidth, int tileHeight, int minTileX, int minTileY) {
        Set<TileCell> remaining = new HashSet<>(cells);
        int count = 0;

        while (!remaining.isEmpty()) {
            TileCell start = remaining.iterator().next();
            ArrayDeque<TileCell> queue = new ArrayDeque<>();
            List<TileCell> component = new ArrayList<>();
            queue.add(start);
            remaining.remove(start);

            while (!queue.isEmpty()) {
                TileCell cell = queue.removeFirst();
                component.add(cell);

                addNeighbor(cell.x + 1, cell.y, remaining, queue);
                addNeighbor(cell.x - 1, cell.y, remaining, queue);
                addNeighbor(cell.x, cell.y + 1, remaining, queue);
                addNeighbor(cell.x, cell.y - 1, remaining, queue);
            }

            environment.addEntity(createPlant(readTileComponentPosition(component, tileWidth, tileHeight, minTileX, minTileY), speciesKey));
            count++;
        }

        return count;
    }

    private static model.Entity createPlant(Vector2D position, String speciesKey) {
        if (VINE_LAYER.equals(speciesKey) || "nho".equals(speciesKey)) {
            return new VinePlant(position);
        }
        return new TreePlant(position, speciesKey);
    }

    private static void addNeighbor(int x, int y, Set<TileCell> remaining, ArrayDeque<TileCell> queue) {
        TileCell neighbor = new TileCell(x, y);
        if (remaining.remove(neighbor)) {
            queue.add(neighbor);
        }
    }

    private static Vector2D readTileComponentPosition(List<TileCell> component, int tileWidth, int tileHeight, int minTileX, int minTileY) {
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (TileCell cell : component) {
            minX = Math.min(minX, cell.x);
            maxX = Math.max(maxX, cell.x);
            maxY = Math.max(maxY, cell.y);
        }

        double x = ((minX - minTileX) * tileWidth + (maxX - minX + 1) * tileWidth / 2.0);
        double y = (maxY - minTileY + 1) * tileHeight;
        return new Vector2D(x, y);
    }

    private static String readSpeciesKey(Element object) {
        String type = object.getAttribute("type").trim();
        if (!type.isEmpty()) return type;

        String name = object.getAttribute("name").trim();
        if (!name.isEmpty()) return name;

        return "small_tree";
    }

    private static Vector2D readPlantPosition(Element object, double offsetX, double offsetY) {
        double x = readDouble(object, "x") + offsetX;
        double y = readDouble(object, "y") + offsetY;
        double width = object.hasAttribute("width") ? readDouble(object, "width") : 0;
        double height = object.hasAttribute("height") ? readDouble(object, "height") : 0;

        if (object.hasAttribute("gid") && width > 0 && height > 0) {
            return new Vector2D(x + width / 2.0, y - height / 2.0);
        }
        if (width > 0 && height > 0) {
            return new Vector2D(x + width / 2.0, y + height / 2.0);
        }
        return new Vector2D(x, y);
    }

    private static Document parseXml(File file) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setIgnoringComments(true);
        factory.setCoalescing(true);
        return factory.newDocumentBuilder().parse(file);
    }

    private static TileBounds readTileBounds(Element map) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        NodeList layerNodes = map.getChildNodes();
        for (int i = 0; i < layerNodes.getLength(); i++) {
            Node node = layerNodes.item(i);
            if (!(node instanceof Element) || !"layer".equals(node.getNodeName())) continue;

            Element data = firstChild((Element) node, "data");
            if (data == null) continue;

            NodeList chunks = data.getChildNodes();
            for (int j = 0; j < chunks.getLength(); j++) {
                Node chunkNode = chunks.item(j);
                if (!(chunkNode instanceof Element) || !"chunk".equals(chunkNode.getNodeName())) continue;

                Element chunk = (Element) chunkNode;
                int chunkX = readInt(chunk, "x");
                int chunkY = readInt(chunk, "y");
                int chunkWidth = readInt(chunk, "width");
                int chunkHeight = readInt(chunk, "height");

                minX = Math.min(minX, chunkX);
                minY = Math.min(minY, chunkY);
                maxX = Math.max(maxX, chunkX + chunkWidth);
                maxY = Math.max(maxY, chunkY + chunkHeight);
            }
        }

        if (minX == Integer.MAX_VALUE) {
            minX = 0;
            minY = 0;
            maxX = readInt(map, "width");
            maxY = readInt(map, "height");
        }

        return new TileBounds(minX, minY, maxX, maxY);
    }

    private static Element firstChild(Element parent, String name) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node instanceof Element && name.equals(node.getNodeName())) {
                return (Element) node;
            }
        }
        return null;
    }

    private static int readInt(Element element, String attribute) {
        return Integer.parseInt(element.getAttribute(attribute));
    }

    private static int parseGid(String raw) {
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) return 0;
        return Integer.parseInt(trimmed);
    }

    private static double readDouble(Element element, String attribute) {
        return Double.parseDouble(element.getAttribute(attribute));
    }

    private static class TileCell {
        private final int x;
        private final int y;

        private TileCell(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof TileCell)) return false;
            TileCell cell = (TileCell) other;
            return x == cell.x && y == cell.y;
        }

        @Override
        public int hashCode() {
            return 31 * x + y;
        }
    }

    private static class TileBounds {
        private final int minX;
        private final int minY;

        private TileBounds(int minX, int minY, int maxX, int maxY) {
            this.minX = minX;
            this.minY = minY;
        }
    }
}
