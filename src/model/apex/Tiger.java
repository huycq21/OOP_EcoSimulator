package model.apex;

import model.Vector2D;
import model.environment.Environment;
import model.environment.Rectangle;
import model.strategy.*;
import model.Entity;
import model.Animal;
import model.herbivore.*;
import model.carnivore.*;
import model.domestic.*;

import java.util.List;

public class Tiger extends ApexEntity {

    private double pounceRadius; // Tầm nhảy vồ mồi

    public Tiger(Vector2D position) {
        // Stats: Nhanh hơn Gấu (7.0), Máu khá (300), Sát thương (85)
        // Mức độ đe dọa: 180.0 (Cao nhất rừng hiện tại, Sói max bầy đàn cũng chỉ 152.0)
        super(position, 9.0, 300.0, 400.0, 7.0, 200.0, 180.0, 85.0, 60, 250);
        
        // Tầm vồ mồi xa gấp 4 lần cơ thể (Không cần áp sát quá gần như đánh thường)
        this.pounceRadius = this.getSize() * 4.0; 

        // --- 1. THỰC ĐƠN CỦA VUA ---
        this.addPreyType(Deer.class);
        this.addPreyType(Boar.class);
        this.addPreyType(Cow.class);
        this.addPreyType(Pig.class);
        this.addPreyType(Wolf.class);
        this.addPreyType(Fox.class);
        this.addPreyType(Hyena.class); // Hổ rất ghét Linh cẩu, thấy là vả!

        // --- 2. LẮP NÃO CHÚA TỂ ---
        SurvivalStrategy passive = new PassiveStrategy();
        SurvivalStrategy hunter = new HunterStrategy(passive);
        
        // Hổ đi lẻ, không cần bầy đàn. Ưu tiên ăn xác nếu có.
        // Mức đe dọa 180.0 nên Hổ không ngán ai, bỏ luôn ScaredStrategy!
        SurvivalStrategy scavenger = new ScavengerStrategy(hunter);
        
        this.setBrain(scavenger);
    }

    // --- KỸ NĂNG: CÚ VỒ CHÍ MẠNG ---
    @Override
    public void performSpecialAbility(Environment env) {
        // Bỏ qua nếu đã chết hoặc chiêu chưa hồi xong
        if (!isAlive() || currentSpAttack > 0) return;

        // Tạo khung chữ nhật bao quanh tầm vồ mồi (Đường kính = pounceRadius * 2)
        Rectangle pounceRange = new Rectangle(
            this.getPosition().getX(), 
            this.getPosition().getY(), 
            this.pounceRadius * 2, 
            this.pounceRadius * 2
        );

        List<Entity> nearbyEntities = env.getQuadTree().query(pounceRange, null);
        Animal target = null;
        double minDistance = Double.MAX_VALUE;

        // Tìm con mồi xấu số GẦN NHẤT trong tầm nhảy
        for (Entity entity : nearbyEntities) {
            if (entity != this && entity.isAlive() && entity instanceof Animal && entity.getClass() != this.getClass()) {
                
                Animal prey = (Animal) entity;
                double distance = this.getPosition().distanceTo(prey.getPosition());

                if (distance <= this.pounceRadius && distance < minDistance) {
                    minDistance = distance;
                    target = prey;
                }
            }
        }

        // THỰC THI SÁT THƯƠNG NẾU TÌM THẤY MỤC TIÊU
        if (target != null) {
            // Sát thương chí mạng: X2 Đam cơ bản (85 * 2 = 170 Đam)
            // Lợn rừng, Sói, Hươu... dính 1 vồ này là "đăng xuất" ngay lập tức!
            double critDamage = this.attackDamage * 2.0;
            target.setHp(target.getHp() - critDamage);

            System.out.println("GÀOOO!! Hổ vồ chí mạng " + target.getClass().getSimpleName() + " (-" + critDamage + " HP)!");

            // Đưa chiêu cuối vào thời gian chờ, đồng thời reset luôn đòn đánh thường
            this.currentSpAttack = this.spAttackCooldown;
            this.currentCooldownTimer = this.attackCooldown; 
        }
    }
}