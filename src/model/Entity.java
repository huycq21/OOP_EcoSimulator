package model;

public abstract class Entity {
 protected double x, y;
 protected int size;

 public Entity(double x, double y, int size) {
     this.x = x;
     this.y = y;
     this.size = size;
 }

 // Mỗi thực thể tự quyết định cách nó di chuyển trong mỗi khung hình
 public abstract void update(); 

 // Getters
 public double getX() { return x; }
 public double getY() { return y; }
 public int getSize() { return size; }
}