package model.herbivore;

import model.Vector2D;
import model.strategy.ForagingStrategy;
import model.Reproducible;
import model.Entity;
import model.environment.Environment;
import model.environment.Season;

public class Rabbit extends Herbivore implements Reproducible {

    private int reproductionCooldown;
    private boolean female;

    // Thỏ có các chỉ số mặc định: Kích thước 3.0, Máu 50, Năng lượng 100, Tốc độ 5.0, Tầm nhìn 50.0
    public Rabbit(Vector2D position) {
        super(position, 3.0, 50, 100, 5.0, 50.0);
        
        // QUAN TRỌNG: Lắp bộ não đi dạo cho thỏ ngay khi mới sinh ra
        this.setBrain(new ForagingStrategy());
        this.reproductionCooldown = 0;
        female = Math.random() < 0.5;
    }

    @Override
    public boolean canReproduce() {
        if (!female) {
            return false;
        }

        if(reproductionCooldown > 0)
            return false;

        if(getEnergy() < getMaxEnergy() * 0.8)
            return false;

        Season season =
                Environment.getInstance()
                        .getWeather()
                        .getCurrentSeason();

        return season == Season.SPRING;
    }

    @Override
    public Entity reproduce() {

        setEnergy(getEnergy() * 0.5);

        reproductionCooldown = 1500;

        System.out.println(
            "Rabbit reproduced id=" + getId()
        );

        return new Rabbit(
                new Vector2D(
                        getPosition().getX() + 10,
                        getPosition().getY() + 10
                )
        );
    }

    @Override
    public void update() {

        super.update();

        if (reproductionCooldown > 0) {
            reproductionCooldown--;
        }
    }

    public boolean isFemale() {
        return female;
    }

    // Bạn thậm chí không cần ghi đè (override) hàm update() ở đây!
    // Con Thỏ sẽ tự động dùng hàm update() của Animal, tự động gọi não PassiveStrategy để tính toán.
}

