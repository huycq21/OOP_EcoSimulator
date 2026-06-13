package model.strategy;

import model.Animal;
import model.carnivore.Carnivore;
import java.util.List;

public class PackFlockingStrategy extends FlockingStrategy {
    private final double buffPerAlly; // Mỗi đồng bọn tăng bao nhiêu % sức mạnh
    private final double maxBuff;     // Giới hạn buff tối đa (tránh việc 100 con tụ lại sức mạnh vô cực)

    // Constructor cho phép truyền tham số linh hoạt cho từng loài
    public PackFlockingStrategy(SurvivalStrategy customLogic, double buffPerAlly, double maxBuff) {
        super(customLogic);
        this.buffPerAlly = buffPerAlly;
        this.maxBuff = maxBuff;
    }

    @Override
    public void execute(Animal animal) {
        List<Animal> allies = findNearbyAllies(animal);

        // 2. Tính toán sức mạnh bầy đàn
        // Ví dụ: Có 3 đồng bọn, mỗi con buff 0.2 (20%). Tổng buff = 1.0 + 0.6 = 1.6
        double currentBuff = 1.0 + Math.min(allies.size() * buffPerAlly, maxBuff);

        // 3. Cập nhật hệ số sức mạnh cho con vật
        if (animal instanceof Carnivore) {
            ((Carnivore) animal).setPackMultiplier(currentBuff);
        }

        super.execute(animal);
    }
}