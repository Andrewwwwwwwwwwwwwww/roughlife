package io.github.andrewwwwwwwwwwwwwww.roughlife.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.EnumSet;

/**
 * A restless skull that haunts the night sky, drifting over the wilderness
 * and swooping at travelers. Burns away in sunlight. Entity logic is
 * original; the texture is from BetterNether (Team BetterX, MIT) — see
 * THIRD-PARTY-NOTICES.md.
 */
public class WailingSkull extends Monster {
    public WailingSkull(EntityType<? extends WailingSkull> type, Level level) {
        super(type, level);
        this.moveControl = new FloatMoveControl(this);
        this.xpReward = 5;
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 14.0)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.30)
                .add(Attributes.FLYING_SPEED, 0.90)
                .add(Attributes.FOLLOW_RANGE, 48.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(4, new SwoopAtTargetGoal(this));
        this.goalSelector.addGoal(8, new RandomDriftGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        // Creatures of the night: the sun sets them alight.
        if (!this.level().isClientSide() && this.isAlive() && this.tickCount % 20 == 0
                && this.level().getSkyDarken() < 4
                && this.level().canSeeSky(this.blockPosition())) {
            this.setRemainingFireTicks(120);
        }
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
        return false; // it floats
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.VEX_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.VEX_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.VEX_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.8f;
    }

    /** Aimless nighttime drifting when there's no one to hunt. */
    static class RandomDriftGoal extends Goal {
        private final WailingSkull skull;

        RandomDriftGoal(WailingSkull skull) {
            this.skull = skull;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return skull.getTarget() == null && skull.getRandom().nextInt(this.adjustedTickDelay(40)) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            double x = skull.getX() + (skull.getRandom().nextDouble() * 2 - 1) * 12.0;
            double y = skull.getY() + (skull.getRandom().nextDouble() * 2 - 1) * 5.0;
            double z = skull.getZ() + (skull.getRandom().nextDouble() * 2 - 1) * 12.0;
            // Don't drift into the ground.
            BlockPos below = BlockPos.containing(x, y, z).below();
            if (!skull.level().getBlockState(below).isAir()) {
                y = skull.getY() + 3.0;
            }
            skull.getMoveControl().setWantedPosition(x, y, z, 1.0);
        }
    }

    /** The signature attack: shriek, dive at the target, bite on contact. */
    static class SwoopAtTargetGoal extends Goal {
        private final WailingSkull skull;
        private int retargetCooldown;

        SwoopAtTargetGoal(WailingSkull skull) {
            this.skull = skull;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = skull.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public void start() {
            skull.playSound(SoundEvents.VEX_CHARGE, 1.0f, 0.7f);
            this.retargetCooldown = 0;
        }

        @Override
        public void tick() {
            LivingEntity target = skull.getTarget();
            if (target == null) {
                return;
            }
            if (--this.retargetCooldown <= 0) {
                this.retargetCooldown = this.adjustedTickDelay(6);
                skull.getMoveControl().setWantedPosition(
                        target.getX(), target.getEyeY(), target.getZ(), 1.6);
            }
            if (skull.getBoundingBox().inflate(0.35).intersects(target.getBoundingBox())
                    && skull.level() instanceof ServerLevel serverLevel) {
                skull.doHurtTarget(serverLevel, target);
                // Peel away after the bite so it swoops instead of face-hugging.
                skull.getMoveControl().setWantedPosition(
                        target.getX() + (skull.getRandom().nextDouble() * 2 - 1) * 8.0,
                        target.getEyeY() + 5.0 + skull.getRandom().nextDouble() * 3.0,
                        target.getZ() + (skull.getRandom().nextDouble() * 2 - 1) * 8.0, 1.2);
                this.retargetCooldown = this.adjustedTickDelay(24);
            }
        }
    }
}
