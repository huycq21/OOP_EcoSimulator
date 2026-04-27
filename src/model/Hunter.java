package model;

public interface Hunter {
    // Hành vi tấn công mục tiêu

    void hunt(Entity target); 
    int getNutritionalValue();
    // Kẻ đi săn cũng có thể bị ăn
}