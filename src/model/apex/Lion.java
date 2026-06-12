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

public class Lion extends ApexEntity {

    private double roarRadius;
    private double roarDamage; // Sát thương từ uy áp tiếng gầm

    public Lion(Vector2D position) {
        // Stats: Máu trâu (350), Thể lực dồi dào (450), Đe dọa cơ bản (130)
        super(position, 11.0, 350.0, 450.0, 5.8, 200.0, 130.0, 90.0, 75, 350);
        
        // Tiếng gầm vang xa gấp 2.5 lần tầm nhìn (AOE cực rộng)
        this.roarRadius = this.getVisionRadius() * 2.5; 
        
        // Lượng máu mất đi khi nghe tiếng gầm. Dễ dàng tinh chỉnh ở đây!
        this.roarDamage = 45.0; 

        // --- 1. THỰC ĐƠN CỦA CHÚA SƠN LÂM ---
        this.addPreyType(Deer.class);
        this.addPreyType(Boar.class);
        this.addPreyType(Cow.class);
        this.addPreyType(Pig.class);
        this.addPreyType(Wolf.class);
        this.addPreyType(Hyena.class);

        // --- 2. LẮP NÃO BẦY ĐÀN (PRIDE) ---
        SurvivalStrategy passive = new PassiveStrategy(); // Rảnh rỗi đi dạo
        SurvivalStrategy packLogic = new PackFlockingStrategy(passive, 0.30, 2);
        SurvivalStrategy hunter = new HunterStrategy(packLogic); 
        SurvivalStrategy scared = new ScaredStrategy(hunter);
        SurvivalStrategy scavenger = new ScavengerStrategy(scared);
        
        this.setBrain(scavenger);
    }

    // --- KỸ NĂNG: SÓNG ÂM CHẤN NHIẾP ---
    @Override
    public void performSpecialAbility(Environment env) {
        if (!isAlive() || currentSpAttack > 0) return;

        // Dùng ĐƯỜNG KÍNH (Radius * 2) để quét QuadTree chuẩn xác
        Rectangle roarRange = new Rectangle(
            this.getPosition().getX(), 
            this.getPosition().getY(), 
            this.roarRadius * 2, 
            this.roarRadius * 2
        );

        List<Entity> nearbyEntities = env.getQuadTree().query(roarRange, null);
        boolean roared = false;

        for (Entity entity : nearbyEntities) {
            // Chỉ tác dụng lên Động vật và không tác dụng lên các Boss Apex khác
            if (entity != this && entity.isAlive() && entity instanceof Animal && !(entity instanceof ApexEntity)) {
                
                Animal prey = (Animal) entity;
                double distance = this.getPosition().distanceTo(prey.getPosition());

                if (distance <= this.roarRadius) {
                    
                    // Trừ thẳng HP bằng sức ép của tiếng gầm
                    prey.setHp(prey.getHp() - this.roarDamage);
                    roared = true;
                    
                    System.out.println("GÀOOO!! Sư tử gầm chấn động! " + prey.getClass().getSimpleName() 
                        + " thổ huyết hoảng loạn (-" + this.roarDamage + " HP)!");
                }
            }
        }

        // Đưa chiêu gầm vào thời gian chờ
        if (roared) {
            this.currentSpAttack = this.spAttackCooldown;
            this.currentCooldownTimer = this.attackCooldown;
        }
    }
}