package model.apex;

import model.Vector2D;
import model.environment.Environment;
import model.environment.Rectangle;
import model.strategy.*;
import model.Entity;
import model.Animal;
import model.domestic.*;
import model.herbivore.*;
import model.carnivore.*;

import java.util.List;

public class Bear extends ApexEntity {

    private double aoeRadius;

    public Bear(Vector2D position) {
        // Cân bằng hoàn hảo: Trâu, Đam to, AOE bự, nhưng Chậm.
        super(position, 12.0, 400.0, 500.0, 3.0, 230.0, 150.0, 100.0, 90, 300);
        
        // Tầm vả AOE vươn ra gấp 1.5 lần cơ thể
        this.aoeRadius = this.getSize() * 1.5; 

        // --- 1. LÊN THỰC ĐƠN (GẤU ĂN TẠP) ---
        this.addPreyType(Deer.class);
        this.addPreyType(Boar.class);
        this.addPreyType(Goat.class);
        this.addPreyType(Cow.class);
        this.addPreyType(Pig.class);
        this.addPreyType(Wolf.class); // Gấu vả chết sói để cướp xác!
        this.addPreyType(Fox.class);

        // --- 2. LẮP NÃO BOSS ---
        SurvivalStrategy passive = new PassiveStrategy();               // Đi dạo
        SurvivalStrategy hunter = new HunterStrategy(passive);          // Săn mồi
        SurvivalStrategy scared = new ScavengerStrategy(hunter);     // Ăn xác
        SurvivalStrategy scavenger = new ScaredStrategy(scared);        // Bỏ chạy 
        this.setBrain(scavenger);
    }

    @Override
    public void performSpecialAbility(Environment env) {
        // SỬA LỖI 1: Dùng biến hồi chiêu ĐẶC BIỆT của ApexEntity
        if (!isAlive() || currentSpAttack > 0) return;

        boolean hitSomeone = false;

        // SỬA LỖI 2: Rộng/Cao của hình chữ nhật phải bằng ĐƯỜNG KÍNH (Bán kính x 2)
        Rectangle aoeRange = new Rectangle(
            this.getPosition().getX(), 
            this.getPosition().getY(), 
            this.aoeRadius * 2, 
            this.aoeRadius * 2
        );

        List<Entity> nearbyEntities = env.getQuadTree().query(aoeRange, null);

        // 3. XÉT VA CHẠM (Cú tát "AOE phán xét")
        for (Entity entity : nearbyEntities) {
            // Không tự vả mình, không vả gấu khác (tuỳ bạn), và chỉ vả Động vật
            if (entity != this && entity.isAlive() && entity instanceof Animal && entity.getClass() != this.getClass()) {
                
                Animal prey = (Animal) entity;
                double distance = this.getPosition().distanceTo(prey.getPosition());

                if (distance <= this.aoeRadius) {
                    prey.setHp(prey.getHp() - this.attackDamage);
                    hitSomeone = true;
                    System.out.println("Gấu gầm thét vả AOE trúng " + prey.getClass().getSimpleName() + " (-" + this.attackDamage + " HP)!");
                }
            }
        }

        // Chỉ đưa chiêu vào Cooldown nếu thực sự có vả trúng mục tiêu (tránh lãng phí chiêu)
        if (hitSomeone) {
            this.currentSpAttack = this.spAttackCooldown; // Reset CD chiêu cuối
            this.currentCooldownTimer = this.attackCooldown; // Vả AOE xong cũng phải nghỉ tay một chút mới cắn thường được!
        }
    }
}