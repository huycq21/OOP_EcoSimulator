package model.environment;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Vector2D;

public class TmxCollisionLoader {
    private static final String COLLISION_LAYER = "collision";
    private static final String MAP_BOUNDS_LAYER = "map_bounds";
    private static final String WICKET_LAYER = "wicket";

    // BỘ TỪ ĐIỂN DỊCH TÊN LAYER CŨ SANG TERRAIN TYPE MỚI
    private static final Map<String, TerrainType> LEGACY_TERRAIN_MAP = new HashMap<>();
    static {
        LEGACY_TERRAIN_MAP.put("water_zone", TerrainType.WATER);
        // Gộp toàn bộ các loại chuồng cũ thành vùng PEN chung
        LEGACY_TERRAIN_MAP.put("coop", TerrainType.PEN);
        LEGACY_TERRAIN_MAP.put("cowshed", TerrainType.PEN);
        LEGACY_TERRAIN_MAP.put("pigsty", TerrainType.PEN);
    }

    public static void loadInto(Environment environment, String tmxPath) {
        try {
            Document document = parseXml(new File(tmxPath));
            Element map = document.getDocumentElement();
            int tileWidth = readInt(map, "tilewidth");
            int tileHeight = readInt(map, "tileheight");
            TileBounds bounds = readTileBounds(map);
            double offsetX = -bounds.minX * tileWidth;
            double offsetY = -bounds.minY * tileHeight;

            NodeList mapChildren = map.getChildNodes();
            int blockerCount = 0;
            int terrainCount = 0;
            
            for (int i = 0; i < mapChildren.getLength(); i++) {
                Node node = mapChildren.item(i);
                if (!(node instanceof Element) || !"objectgroup".equals(node.getNodeName())) continue;

                Element objectGroup = (Element) node;
                String layerName = objectGroup.getAttribute("name").trim().toLowerCase();
                NodeList objects = objectGroup.getChildNodes();
                
                for (int j = 0; j < objects.getLength(); j++) {
                    Node objectNode = objects.item(j);
                    if (!(objectNode instanceof Element) || !"object".equals(objectNode.getNodeName())) continue;

                    Element object = (Element) objectNode;
                    MapCollider collider = readCollider(object, offsetX, offsetY);
                    if (collider == null) continue;

                    if (MAP_BOUNDS_LAYER.equals(layerName)) {
                        environment.setMapBounds(collider);
                    } else if (COLLISION_LAYER.equals(layerName)) {
                        environment.addMapCollider(collider);
                        blockerCount++;
                    } else if (WICKET_LAYER.equals(layerName)) {
                        environment.addWicketCollider(collider);
                        blockerCount++;
                    } else {
                        // 1. Kiểm tra xem tên layer có nằm trong bộ từ điển cũ không
                        TerrainType mappedType = LEGACY_TERRAIN_MAP.get(layerName);
                        if (mappedType != null) {
                            environment.addTerrainZone(mappedType, collider);
                            terrainCount++;
                        } 
                        // 2. Chấp nhận hệ thống tên mới linh hoạt (Ví dụ: terrain_mud, terrain_ice)
                        else if (layerName.startsWith("terrain_")) {
                            try {
                                String typeStr = layerName.substring(8).toUpperCase();
                                TerrainType type = TerrainType.valueOf(typeStr);
                                environment.addTerrainZone(type, collider);
                                terrainCount++;
                            } catch (IllegalArgumentException ignored) {
                                System.err.println("Warning: Invalid flexible terrain layer - " + layerName);
                            }
                        }
                    }
                }
            }

            System.out.println("Loaded TMX collision: " + blockerCount
                    + " blockers, " + terrainCount + " terrain zones from " + tmxPath);
        } catch (Exception e) {
            System.err.println("Cannot load TMX collision: " + tmxPath);
            e.printStackTrace();
        }
    }

    private static Document parseXml(File file) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setIgnoringComments(true);
        factory.setCoalescing(true);
        return factory.newDocumentBuilder().parse(file);
    }

    private static MapCollider readCollider(Element object, double offsetX, double offsetY) {
        if (!object.hasAttribute("x") || !object.hasAttribute("y")) return null;
        double x = readDouble(object, "x") + offsetX;
        double y = readDouble(object, "y") + offsetY;

        Element polygon = firstChild(object, "polygon");
        if (polygon != null) {
            return readPolygonCollider(polygon, x, y);
        }

        if (!object.hasAttribute("width") || !object.hasAttribute("height")) return null;
        double width = readDouble(object, "width");
        double height = readDouble(object, "height");
        if (width <= 0 || height <= 0) return null;

        MapCollider.Shape shape = hasChild(object, "ellipse")
                ? MapCollider.Shape.ELLIPSE
                : MapCollider.Shape.RECTANGLE;
        return new MapCollider(shape, x, y, width, height);
    }

    private static MapCollider readPolygonCollider(Element polygon, double originX, double originY) {
        String rawPoints = polygon.getAttribute("points").trim();
        if (rawPoints.isEmpty()) return null;

        List<Vector2D> points = new ArrayList<>();
        String[] pointPairs = rawPoints.split(" ");
        for (String pair : pointPairs) {
            String[] xy = pair.split(",");
            if (xy.length != 2) continue;

            double x = originX + Double.parseDouble(xy[0]);
            double y = originY + Double.parseDouble(xy[1]);
            points.add(new Vector2D(x, y));
        }

        if (points.size() < 3) return null;
        return new MapCollider(points);
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

    private static boolean hasChild(Element parent, String name) {
        return firstChild(parent, name) != null;
    }

    private static int readInt(Element element, String attribute) {
        return Integer.parseInt(element.getAttribute(attribute));
    }

    private static double readDouble(Element element, String attribute) {
        return Double.parseDouble(element.getAttribute(attribute));
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
