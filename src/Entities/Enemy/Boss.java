package Entities.Enemy;

import Entities.Ball;
import Entities.Paddle;
import Utils.Config;
import Utils.SoundManager;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * Boss cho level 5 - Di chuyển và tấn công player
 */
public class Boss extends Enemy {
    private enum BossState {
        MOVING, ATTACKING, SPECIAL_ATTACK, DAMAGED
    }

    private BossState currentState;
    private double moveSpeed;
    private double attackCooldown;
    private double specialAttackCooldown;
    private double stateTimer;

    // Attack patterns
    private List<Ball> bossBalls;
    private boolean isMovingRight;

    // Visual effects
    private Color bossColor;
    private double flashTimer;

    public Boss(double x, double y) {
        super("Final Boss", x, y, 200, 80);

        // Boss stats
        this.health = 500;
        this.maxHealth = 500;
        this.scoreValue = 1000;
        this.moveSpeed = 2.0;

        // Boss state
        this.currentState = BossState.MOVING;
        this.attackCooldown = 3.0;
        this.specialAttackCooldown = 10.0;
        this.stateTimer = 0;
        this.isMovingRight = true;

        // Visual
        this.bossColor = Color.DARKRED;
        this.bossBalls = new ArrayList<>();

        System.out.println("👹 BOSS SPAWNED! Health: " + health);
    }

    @Override
    public void start() {
        System.out.println("🎮 Boss initialized at position: " + position);
    }

    @Override
    public void update(double deltaTime) {
        if (!isActive) return;

        stateTimer += deltaTime;
        flashTimer = Math.max(0, flashTimer - deltaTime);

        switch (currentState) {
            case MOVING:
                updateMovement(deltaTime);
                break;
            case ATTACKING:
                updateAttacking(deltaTime);
                break;
            case SPECIAL_ATTACK:
                updateSpecialAttack(deltaTime);
                break;
            case DAMAGED:
                updateDamaged(deltaTime);
                break;
        }

        updateBossBalls(deltaTime);
        checkStateTransitions();
    }

    private void updateMovement(double deltaTime) {
        // Di chuyển qua lại
        if (isMovingRight) {
            position.x += moveSpeed;
            if (position.x + width > Config.SCREEN_WIDTH - Config.INSET) {
                isMovingRight = false;
            }
        } else {
            position.x -= moveSpeed;
            if (position.x < Config.INSET) {
                isMovingRight = true;
            }
        }
    }

    private void updateAttacking(double deltaTime) {
        // Tấn công bằng cách bắn balls
        attackCooldown -= deltaTime;
        if (attackCooldown <= 0) {
            shootBalls();
            attackCooldown = 2.0 + Math.random() * 2.0; // Random cooldown
        }
    }

    private void updateSpecialAttack(double deltaTime) {
        // Tấn công đặc biệt - di chuyển nhanh và bắn nhiều
        moveSpeed = 4.0;
        updateMovement(deltaTime);

        specialAttackCooldown -= deltaTime;
        if (specialAttackCooldown <= 0) {
            rapidFire();
            specialAttackCooldown = 8.0;
        }
    }

    private void updateDamaged(double deltaTime) {
        // Hiệu ứng khi bị damage
        stateTimer += deltaTime;
        if (stateTimer >= 1.0) {
            currentState = BossState.MOVING;
            stateTimer = 0;
        }
    }

    private void updateBossBalls(double deltaTime) {
        // Update boss balls
        bossBalls.removeIf(ball -> {
            ball.update(deltaTime);

            // Remove if out of screen
            if (ball.getPosition().y > Config.SCREEN_HEIGHT) {
                return true;
            }

            return false;
        });
    }

    private void checkStateTransitions() {
        if (currentState == BossState.MOVING && stateTimer >= 4.0) {
            currentState = BossState.ATTACKING;
            stateTimer = 0;
        } else if (currentState == BossState.ATTACKING && stateTimer >= 6.0) {
            currentState = health < maxHealth / 2 ? BossState.SPECIAL_ATTACK : BossState.MOVING;
            stateTimer = 0;
        } else if (currentState == BossState.SPECIAL_ATTACK && stateTimer >= 5.0) {
            currentState = BossState.MOVING;
            moveSpeed = 2.0; // Reset speed
            stateTimer = 0;
        }
    }

    private void shootBalls() {
        // Bắn 3 balls theo các hướng khác nhau
        for (int i = -1; i <= 1; i++) {
            Ball bossBall = new Ball(position.x + width / 2, position.y + height);
            bossBall.setActive(true);
            bossBall.setVelocity(i * 2.0, 3.0); // Góc khác nhau
            bossBall.setColor(Color.ORANGERED);
            bossBall.setPierce(true); // Boss balls có thể xuyên qua

            bossBalls.add(bossBall);
        }

        SoundManager.getInstance().playSound("boss_attack");
        System.out.println("🔥 Boss shooting balls!");
    }

    private void rapidFire() {
        // Bắn nhanh nhiều balls
        for (int i = 0; i < 8; i++) {
            double angle = (i * Math.PI / 4); // 8 hướng
            Ball bossBall = new Ball(position.x + width / 2, position.y + height);
            bossBall.setActive(true);
            bossBall.setVelocity(Math.cos(angle) * 3.0, Math.sin(angle) * 3.0);
            bossBall.setColor(Color.GOLD);

            bossBalls.add(bossBall);
        }

        SoundManager.getInstance().playSound("boss_special");
        System.out.println("💥 BOSS SPECIAL ATTACK!");
    }

    @Override
    public void onHit(int damage) {
        if (!isActive) return;

        health -= damage;
        flashTimer = 0.3; // Flash effect

        SoundManager.getInstance().playSound("boss_hit");
        System.out.println("💢 Boss hit! Health: " + health + "/" + maxHealth);

        // Chuyển sang trạng thái damaged
        currentState = BossState.DAMAGED;
        stateTimer = 0;

        if (health <= 0) {
            onDefeat();
        }
    }

    @Override
    public void attack() {
        // Attack logic handled in update
    }

    private void onDefeat() {
        isActive = false;
        SoundManager.getInstance().playSound("boss_defeat");
        System.out.println("🎉 BOSS DEFEATED! +" + scoreValue + " points");
    }

    // === RENDER METHOD ===
    public void render(javafx.scene.canvas.GraphicsContext gc) {
        if (!isActive) return;

        // Flash effect khi bị damage
        Color renderColor = bossColor;
        if (flashTimer > 0) {
            renderColor = (System.currentTimeMillis() % 200 < 100) ? Color.WHITE : Color.RED;
        }

        // Vẽ boss body
        gc.setFill(renderColor);
        gc.fillRoundRect(position.x, position.y, width, height, 20, 20);

        // Vẽ health bar
        drawHealthBar(gc);

        // Vẽ boss balls
        for (Ball ball : bossBalls) {
            ball.render(gc);
        }
    }

    private void drawHealthBar(javafx.scene.canvas.GraphicsContext gc) {
        double barWidth = width;
        double barHeight = 10;
        double barX = position.x;
        double barY = position.y - 15;

        // Background
        gc.setFill(Color.DARKGRAY);
        gc.fillRect(barX, barY, barWidth, barHeight);

        // Health
        double healthPercent = (double) health / maxHealth;
        gc.setFill(healthPercent > 0.5 ? Color.LIMEGREEN :
                healthPercent > 0.25 ? Color.ORANGE : Color.RED);
        gc.fillRect(barX, barY, barWidth * healthPercent, barHeight);

        // Border
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1);
        gc.strokeRect(barX, barY, barWidth, barHeight);
    }

    // === GETTERS ===
    public List<Ball> getBossBalls() {
        return bossBalls;
    }

    public BossState getCurrentState() {
        return currentState;
    }
}