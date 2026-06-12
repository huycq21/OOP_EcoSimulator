package model.environment;

import model.Vector2D;

import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MapCollider {
    private TerrainType terrainType = TerrainType.NORMAL_DIRT;

    public enum Shape {
        RECTANGLE,
        ELLIPSE,
        POLYGON
    }

    private static final double EPSILON = 0.0001;

    private final Shape shape;
    private final double x;
    private final double y;
    private final double width;
    private final double height;
    private final double area;
    private final Path2D polygonPath;
    private final Rectangle2D polygonBounds;
    private final List<Vector2D> polygonPoints;

    public MapCollider(Shape shape, double x, double y, double width, double height) {
        this.shape = shape;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.area = calculateShapeArea(shape, width, height);
        this.polygonPath = null;
        this.polygonBounds = null;
        this.polygonPoints = new ArrayList<>();
    }

    public MapCollider(List<Vector2D> polygonPoints) {
        this.shape = Shape.POLYGON;
        this.polygonPoints = new ArrayList<>(polygonPoints);

        Path2D path = new Path2D.Double();
        if (!polygonPoints.isEmpty()) {
            Vector2D first = polygonPoints.get(0);
            path.moveTo(first.getX(), first.getY());
            for (int i = 1; i < polygonPoints.size(); i++) {
                Vector2D point = polygonPoints.get(i);
                path.lineTo(point.getX(), point.getY());
            }
            path.closePath();
        }

        this.polygonPath = path;
        this.polygonBounds = path.getBounds2D();
        this.x = polygonBounds.getX();
        this.y = polygonBounds.getY();
        this.width = polygonBounds.getWidth();
        this.height = polygonBounds.getHeight();
        this.area = calculatePolygonArea(this.polygonPoints);
    }

    public boolean containsCircle(Vector2D center, double radius) {
        if (shape == Shape.POLYGON) {
            return containsCircleInPolygon(center, radius);
        }
        if (shape == Shape.ELLIPSE) {
            return containsCircleInEllipse(center, radius);
        }

        return center.getX() - radius >= x
                && center.getX() + radius <= x + width
                && center.getY() - radius >= y
                && center.getY() + radius <= y + height;
    }

    public boolean clampCircleInside(Vector2D center, double radius) {
        double oldX = center.getX();
        double oldY = center.getY();

        // ==========================================
        // TRƯỜNG HỢP 1: BIÊN HÌNH CHỮ NHẬT
        // ==========================================
        if (shape == Shape.RECTANGLE) {
            double minX = x + radius;
            double maxX = x + width - radius;
            double minY = y + radius;
            double maxY = y + height - radius;

            if (minX > maxX) {
                center.setX(x + width / 2.0);
            } else {
                center.setX(clamp(oldX, minX, maxX));
            }

            if (minY > maxY) {
                center.setY(y + height / 2.0);
            } else {
                center.setY(clamp(oldY, minY, maxY));
            }

            return Math.abs(center.getX() - oldX) > EPSILON || Math.abs(center.getY() - oldY) > EPSILON;
        }

        // ==========================================
        // TRƯỜNG HỢP 2: BIÊN HÌNH ELIP
        // ==========================================
        if (shape == Shape.ELLIPSE) {
            double rx = width / 2.0;
            double ry = height / 2.0;
            double cx = x + rx;
            double cy = y + ry;

            double dx = center.getX() - cx;
            double dy = center.getY() - cy;

            if (Math.abs(dx) < EPSILON && Math.abs(dy) < EPSILON) {
                return false;
            }

            // Giới hạn bán kính an toàn bên trong lòng Elip
            double limitRx = Math.max(EPSILON, rx - radius);
            double limitRy = Math.max(EPSILON, ry - radius);

            // Công thức chuẩn hóa vị trí elip
            double normalized = (dx * dx) / (limitRx * limitRx) + (dy * dy) / (limitRy * limitRy);
            
            // Nếu thực thể vượt quá vùng lòng an toàn của Elip -> Ép co lại vào trong
            if (normalized > 1.0) {
                double scale = 1.0 / Math.sqrt(normalized);
                center.setX(cx + dx * scale);
                center.setY(cy + dy * scale);
                return true;
            }
            return false;
        }

        // ==========================================
        // TRƯỜNG HỢP 3: BIÊN HÌNH ĐA GIÁC (Ví dụ: Đảo, thung lũng)
        // ==========================================
        if (shape == Shape.POLYGON) {
            if (polygonPath == null || polygonPoints.size() < 2) return false;

            // Kiểm tra xem tâm hiện tại đang ở TRONG hay ở NGOÀI đa giác ranh giới
            boolean isCenterInside = polygonPath.contains(oldX, oldY);

            Vector2D closest = null;
            double bestDistanceSquared = Double.MAX_VALUE;

            // Bước 1: Tìm điểm trên cạnh đa giác gần với tâm con vật nhất
            for (int i = 0; i < polygonPoints.size(); i++) {
                Vector2D a = polygonPoints.get(i);
                Vector2D b = polygonPoints.get((i + 1) % polygonPoints.size());
                Vector2D candidate = closestPointOnSegment(center, a, b);
                double dx = center.getX() - candidate.getX();
                double dy = center.getY() - candidate.getY();
                double distanceSquared = dx * dx + dy * dy;
                if (distanceSquared < bestDistanceSquared) {
                    bestDistanceSquared = distanceSquared;
                    closest = candidate;
                }
            }

            if (closest == null) return false;

            // Vector hướng từ điểm sát vách (closest) tới tâm con vật (center)
            double dx = center.getX() - closest.getX();
            double dy = center.getY() - closest.getY();
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance < EPSILON) {
                // Nếu tâm nằm trúng vách ngăn, không có hướng Vector, tạm thời không xử lý đẩy
                return false;
            }

            if (isCenterInside) {
                // TÂM Ở TRONG MAP: Nhưng phần viền (radius) lỡ lẹm ra ngoài vách đá (distance < radius)
                if (distance < radius) {
                    // dx/distance là Vector chỉ hướng từ vách đi VÀO TRONG lòng map.
                    // Ta nhấc con vật từ điểm vách, đẩy sâu vào trong lòng đúng bằng khoảng cách 'radius'
                    center.setX(closest.getX() + (dx / distance) * radius);
                    center.setY(closest.getY() + (dy / distance) * radius);
                    return true;
                }
            } else {
                // TÂM Ở NGOÀI MAP: Con vật bị văng hẳn ra ngoài không gian trống (ví dụ do lực đẩy quá mạnh)
                // Lúc này Vector (dx, dy) đang hướng ra ngoài map. Ta dùng dấu TRỪ (-) để đảo hướng chạy VÀO TRONG
                center.setX(closest.getX() - (dx / distance) * radius);
                center.setY(closest.getY() - (dy / distance) * radius);
                return true;
            }
        }

        // Trả về true nếu tọa độ thực sự bị thay đổi (có sửa sai va chạm)
        return Math.abs(center.getX() - oldX) > EPSILON || Math.abs(center.getY() - oldY) > EPSILON;
    }

    public boolean resolveCircleCollision(Vector2D center, double radius) {
        if (shape == Shape.POLYGON) {
            return resolvePolygonCollision(center, radius);
        }
        if (shape == Shape.ELLIPSE) {
            return resolveEllipseCollision(center, radius);
        }

        return resolveRectangleCollision(center, radius);
    }

    public Vector2D randomPointInside(Random random, double radius) {
        for (int i = 0; i < 120; i++) {
            double px = x + radius + random.nextDouble() * Math.max(1, width - radius * 2);
            double py = y + radius + random.nextDouble() * Math.max(1, height - radius * 2);
            Vector2D point = new Vector2D(px, py);
            if (containsCircle(point, radius)) return point;
        }
        return new Vector2D(x + width / 2.0, y + height / 2.0);
    }

    public double getArea() {
        return Math.max(1.0, area);
    }

    private boolean containsCircleInEllipse(Vector2D center, double radius) {
        double rx = width / 2.0;
        double ry = height / 2.0;
        if (rx <= 0 || ry <= 0) return false;

        double cx = x + rx;
        double cy = y + ry;
        double normalizedX = (center.getX() - cx) / Math.max(EPSILON, rx - radius);
        double normalizedY = (center.getY() - cy) / Math.max(EPSILON, ry - radius);
        return normalizedX * normalizedX + normalizedY * normalizedY <= 1.0;
    }

    private boolean containsCircleInPolygon(Vector2D center, double radius) {
        if (polygonPath == null) return false;

        return polygonPath.contains(center.getX(), center.getY())
                && polygonPath.contains(center.getX() + radius, center.getY())
                && polygonPath.contains(center.getX() - radius, center.getY())
                && polygonPath.contains(center.getX(), center.getY() + radius)
                && polygonPath.contains(center.getX(), center.getY() - radius);
    }

    private boolean resolvePolygonCollision(Vector2D center, double radius) {
        if (polygonPath == null || polygonPoints.size() < 2) return false;

        boolean isCenterInside = polygonPath.contains(center.getX(), center.getY());
        Vector2D closest = null;
        double bestDistanceSquared = Double.MAX_VALUE;

        // Tìm điểm gần nhất trên các cạnh của đa giác
        for (int i = 0; i < polygonPoints.size(); i++) {
            Vector2D a = polygonPoints.get(i);
            Vector2D b = polygonPoints.get((i + 1) % polygonPoints.size());
            Vector2D candidate = closestPointOnSegment(center, a, b);
            double dx = center.getX() - candidate.getX();
            double dy = center.getY() - candidate.getY();
            double distanceSquared = dx * dx + dy * dy;
            if (distanceSquared < bestDistanceSquared) {
                bestDistanceSquared = distanceSquared;
                closest = candidate;
            }
        }

        if (closest == null) return false;

        // BỘ LỌC CHUẨN: Tâm ở ngoài và khoảng cách > bán kính -> Chưa chạm
        if (!isCenterInside && bestDistanceSquared >= radius * radius) {
            return false;
        }

        double distance = Math.sqrt(bestDistanceSquared);
        double dx = center.getX() - closest.getX();
        double dy = center.getY() - closest.getY();

        if (distance < EPSILON) {
            center.setY(closest.getY() - radius);
            return true;
        }

        if (isCenterInside) {
            // TÂM Ở TRONG: Kéo ra ngoài mép
            center.setX(closest.getX() - (dx / distance) * radius);
            center.setY(closest.getY() - (dy / distance) * radius);
        } else {
            // TÂM Ở NGOÀI BỊ LẸM VÀO: Đẩy dội ngược lại
            double pushDistance = radius - distance;
            center.setX(center.getX() + (dx / distance) * pushDistance);
            center.setY(center.getY() + (dy / distance) * pushDistance);
        }
        return true;
    }

    private Vector2D closestPointOnSegment(Vector2D point, Vector2D a, Vector2D b) {
        double abX = b.getX() - a.getX();
        double abY = b.getY() - a.getY();
        double lengthSquared = abX * abX + abY * abY;
        if (lengthSquared < EPSILON) return new Vector2D(a.getX(), a.getY());

        double t = ((point.getX() - a.getX()) * abX + (point.getY() - a.getY()) * abY) / lengthSquared;
        t = clamp(t, 0, 1);
        return new Vector2D(a.getX() + abX * t, a.getY() + abY * t);
    }

    private double calculateShapeArea(Shape shape, double width, double height) {
        if (shape == Shape.ELLIPSE) {
            return Math.PI * (width / 2.0) * (height / 2.0);
        }
        return width * height;
    }

    private double calculatePolygonArea(List<Vector2D> points) {
        if (points.size() < 3) return 0;

        double sum = 0;
        for (int i = 0; i < points.size(); i++) {
            Vector2D current = points.get(i);
            Vector2D next = points.get((i + 1) % points.size());
            sum += current.getX() * next.getY() - next.getX() * current.getY();
        }
        return Math.abs(sum) / 2.0;
    }

    private boolean resolveEllipseCollision(Vector2D center, double radius) {
        double rx = width / 2.0 + radius;
        double ry = height / 2.0 + radius;
        if (rx <= 0 || ry <= 0) return false;

        double cx = x + width / 2.0;
        double cy = y + height / 2.0;
        double dx = center.getX() - cx;
        double dy = center.getY() - cy;
        double normalized = (dx * dx) / (rx * rx) + (dy * dy) / (ry * ry);
        if (normalized >= 1.0) return false;

        if (Math.abs(dx) < EPSILON && Math.abs(dy) < EPSILON) {
            center.setY(cy + ry);
            return true;
        }

        double scale = 1.0 / Math.sqrt(normalized);
        center.setX(cx + dx * scale);
        center.setY(cy + dy * scale);
        return true;
    }

    private boolean resolveRectangleCollision(Vector2D center, double radius) {
        double closestX = clamp(center.getX(), x, x + width);
        double closestY = clamp(center.getY(), y, y + height);
        double dx = center.getX() - closestX;
        double dy = center.getY() - closestY;
        double distanceSquared = dx * dx + dy * dy;

        if (distanceSquared > EPSILON && distanceSquared < radius * radius) {
            double distance = Math.sqrt(distanceSquared);
            double pushDistance = radius - distance;
            center.setX(center.getX() + (dx / distance) * pushDistance);
            center.setY(center.getY() + (dy / distance) * pushDistance);
            return true;
        }

        boolean insideRectangle = center.getX() >= x
                && center.getX() <= x + width
                && center.getY() >= y
                && center.getY() <= y + height;
        if (!insideRectangle) return false;

        double left = center.getX() - x;
        double right = x + width - center.getX();
        double top = center.getY() - y;
        double bottom = y + height - center.getY();
        double nearest = Math.min(Math.min(left, right), Math.min(top, bottom));

        if (nearest == left) {
            center.setX(x - radius);
        } else if (nearest == right) {
            center.setX(x + width + radius);
        } else if (nearest == top) {
            center.setY(y - radius);
        } else {
            center.setY(y + height + radius);
        }
        return true;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

        public void setTerrainType(TerrainType type) {
    this.terrainType = type;
    }
    
    public TerrainType getTerrainType() {
        return this.terrainType;
    }
}
