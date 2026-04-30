package model.environment;

import model.Entity;

public interface Hideable {
    // Trả về số lượng động vật tối đa có thể trốn cùng lúc
    int getMaxCapacity();
    
    // Kích thước tối đa của động vật được phép chui vào (Sói to quá sẽ không chui vừa bụi rậm của Thỏ)
    double getMaxAllowedSize();
    
    // Gọi khi một con vật chui vào
    void hideEntity(Entity entity);
    
    // Gọi khi con vật rời đi
    void removeEntity(Entity entity);
}