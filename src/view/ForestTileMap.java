package view;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ForestTileMap {
    private static final long FLIPPED_HORIZONTALLY_FLAG = 0x80000000L;
    private static final long FLIPPED_VERTICALLY_FLAG = 0x40000000L;
    private static final long FLIPPED_DIAGONALLY_FLAG = 0x20000000L;
    private static final long FLIPPED_HEXAGONAL_FLAG = 0x10000000L;

    private final List<Tileset> tilesets;
    private final BufferedImage pattern;
    private BufferedImage worldImage;
    private int worldImageWidth;
    private int worldImageHeight;
    private int pixelWidth;
    private int pixelHeight;

    public ForestTileMap(String tmxPath) {
        this.tilesets = new ArrayList<>();
        this.pattern = loadPattern(tmxPath);
    }

    public boolean isLoaded() {
        return pattern != null;
    }

    public int getPixelWidth() {
        return pixelWidth;
    }

    public int getPixelHeight() {
        return pixelHeight;
    }

    public void draw(Graphics2D g, int x, int y, int width, int height, double scale) {
        if (pattern == null) return;

        ensureWorldImage(width, height);
        g.drawImage(worldImage, x, y, width, height, null);
    }

    private void ensureWorldImage(int width, int height) {
        if (worldImage != null && worldImageWidth == width && worldImageHeight == height) return;

        worldImageWidth = width;
        worldImageHeight = height;
        worldImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = worldImage.createGraphics();
        g.drawImage(pattern, 0, 0, width, height, null);
        g.dispose();
    }

    private BufferedImage loadPattern(String tmxPath) {
        try {
            File tmxFile = new File(tmxPath);
            File baseDir = tmxFile.getParentFile();
            Document document = parseXml(tmxFile);
            Element map = document.getDocumentElement();

            int tileWidth = readInt(map, "tilewidth");
            int tileHeight = readInt(map, "tileheight");
            TileBounds bounds = readTileBounds(map);

            loadTilesets(map, baseDir, tileWidth, tileHeight);

            this.pixelWidth = bounds.width() * tileWidth;
            this.pixelHeight = bounds.height() * tileHeight;

            BufferedImage image = new BufferedImage(
                    pixelWidth,
                    pixelHeight,
                    BufferedImage.TYPE_INT_ARGB
            );
            Graphics2D g = image.createGraphics();
            drawLayers(g, map, tileWidth, tileHeight, bounds.minX, bounds.minY);
            g.dispose();
            return image;
        } catch (Exception e) {
            System.err.println("Cannot load forest tile map: " + tmxPath);
            e.printStackTrace();
            return null;
        }
    }

    private Document parseXml(File file) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setIgnoringComments(true);
        factory.setCoalescing(true);
        return factory.newDocumentBuilder().parse(file);
    }

    private void loadTilesets(Element map, File baseDir, int tileWidth, int tileHeight) throws Exception {
        NodeList tilesetNodes = map.getChildNodes();
        for (int i = 0; i < tilesetNodes.getLength(); i++) {
            Node node = tilesetNodes.item(i);
            if (!(node instanceof Element) || !"tileset".equals(node.getNodeName())) continue;

            Element tilesetElement = (Element) node;
            int firstGid = readInt(tilesetElement, "firstgid");
            Element resolvedTileset = tilesetElement;

            if (tilesetElement.hasAttribute("source")) {
                File tsxFile = new File(baseDir, tilesetElement.getAttribute("source"));
                resolvedTileset = parseXml(tsxFile).getDocumentElement();
            }

            Element imageElement = firstChild(resolvedTileset, "image");
            if (imageElement == null) continue;

            File imageFile = new File(baseDir, imageElement.getAttribute("source"));
            BufferedImage image = ImageIO.read(imageFile);
            int columns = readInt(resolvedTileset, "columns");
            tilesets.add(new Tileset(firstGid, columns, tileWidth, tileHeight, image));
        }

        tilesets.sort(Comparator.comparingInt(tileset -> tileset.firstGid));
    }

    private TileBounds readTileBounds(Element map) {
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

    private void drawLayers(Graphics2D g, Element map, int tileWidth, int tileHeight, int minTileX, int minTileY) {
        NodeList layerNodes = map.getChildNodes();
        for (int i = 0; i < layerNodes.getLength(); i++) {
            Node node = layerNodes.item(i);
            if (!(node instanceof Element) || !"layer".equals(node.getNodeName())) continue;

            Element layer = (Element) node;
            if (isDynamicEntityLayer(layer.getAttribute("name"))) continue;
            Element data = firstChild(layer, "data");

            if (data == null) continue;

            NodeList chunks = data.getChildNodes();
            for (int j = 0; j < chunks.getLength(); j++) {
                Node chunkNode = chunks.item(j);
                if (!(chunkNode instanceof Element) || !"chunk".equals(chunkNode.getNodeName())) continue;

                Element chunk = (Element) chunkNode;
                int chunkX = readInt(chunk, "x");
                int chunkY = readInt(chunk, "y");
                int chunkWidth = readInt(chunk, "width");
                String[] rawGids = chunk.getTextContent().trim().split(",");

                for (int tileIndex = 0; tileIndex < rawGids.length; tileIndex++) {
                    long rawGid = parseGid(rawGids[tileIndex]);
                    int gid = clearFlipFlags(rawGid);
                    if (gid == 0) continue;

                    int localX = tileIndex % chunkWidth;
                    int localY = tileIndex / chunkWidth;
                    int worldTileX = chunkX + localX;
                    int worldTileY = chunkY + localY;
                    drawTile(g, rawGid, (worldTileX - minTileX) * tileWidth, (worldTileY - minTileY) * tileHeight);
                }
            }
        }
    }

    private boolean isDynamicEntityLayer(String layerName) {
        if (layerName == null) return false;
        String normalized = layerName.trim().toLowerCase();
        return "vine".equals(normalized) || "plants".equals(normalized) || "trees".equals(normalized);
    }

    private long parseGid(String raw) {
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) return 0;
        return Long.parseLong(trimmed);
    }

    private int clearFlipFlags(long gid) {
        return (int) (gid & ~(FLIPPED_HORIZONTALLY_FLAG
                | FLIPPED_VERTICALLY_FLAG
                | FLIPPED_DIAGONALLY_FLAG
                | FLIPPED_HEXAGONAL_FLAG));
    }

    private void drawTile(Graphics2D g, long rawGid, int x, int y) {
        int gid = clearFlipFlags(rawGid);
        Tileset tileset = findTileset(gid);
        if (tileset == null) return;

        int localId = gid - tileset.firstGid;
        int sx = (localId % tileset.columns) * tileset.tileWidth;
        int sy = (localId / tileset.columns) * tileset.tileHeight;

        BufferedImage tile = tileset.image.getSubimage(sx, sy, tileset.tileWidth, tileset.tileHeight);
        AffineTransform transform = createTileTransform(rawGid, x, y, tileset.tileWidth, tileset.tileHeight);
        g.drawImage(tile, transform, null);
    }

    private AffineTransform createTileTransform(long rawGid, int x, int y, int tileWidth, int tileHeight) {
        boolean flippedHorizontally = (rawGid & FLIPPED_HORIZONTALLY_FLAG) != 0;
        boolean flippedVertically = (rawGid & FLIPPED_VERTICALLY_FLAG) != 0;
        boolean flippedDiagonally = (rawGid & FLIPPED_DIAGONALLY_FLAG) != 0;

        if (!flippedDiagonally) {
            double scaleX = flippedHorizontally ? -1 : 1;
            double scaleY = flippedVertically ? -1 : 1;
            double translateX = flippedHorizontally ? x + tileWidth : x;
            double translateY = flippedVertically ? y + tileHeight : y;
            return new AffineTransform(scaleX, 0, 0, scaleY, translateX, translateY);
        }

        if (flippedHorizontally && flippedVertically) {
            return new AffineTransform(0, -1, -1, 0, x + tileWidth, y + tileHeight);
        }
        if (flippedHorizontally) {
            return new AffineTransform(0, 1, -1, 0, x + tileWidth, y);
        }
        if (flippedVertically) {
            return new AffineTransform(0, -1, 1, 0, x, y + tileHeight);
        }

        return new AffineTransform(0, 1, 1, 0, x, y);
    }

    private Tileset findTileset(int gid) {
        Tileset selected = null;
        for (Tileset tileset : tilesets) {
            if (gid >= tileset.firstGid) {
                selected = tileset;
            } else {
                break;
            }
        }
        return selected;
    }

    private Element firstChild(Element parent, String name) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node instanceof Element && name.equals(node.getNodeName())) {
                return (Element) node;
            }
        }
        return null;
    }

    private int readInt(Element element, String attribute) {
        return Integer.parseInt(element.getAttribute(attribute));
    }

    private static class Tileset {
        private final int firstGid;
        private final int columns;
        private final int tileWidth;
        private final int tileHeight;
        private final BufferedImage image;

        private Tileset(int firstGid, int columns, int tileWidth, int tileHeight, BufferedImage image) {
            this.firstGid = firstGid;
            this.columns = columns;
            this.tileWidth = tileWidth;
            this.tileHeight = tileHeight;
            this.image = image;
        }
    }

    private static class TileBounds {
        private final int minX;
        private final int minY;
        private final int maxX;
        private final int maxY;

        private TileBounds(int minX, int minY, int maxX, int maxY) {
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
        }

        private int width() {
            return maxX - minX;
        }

        private int height() {
            return maxY - minY;
        }
    }
}
