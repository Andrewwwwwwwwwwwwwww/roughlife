package io.github.andrewwwwwwwwwwwwwww.roughlife.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import java.util.List;

/**
 * A drifting box jellyfish that haunts open water in silent swarms. It
 * doesn't chase so much as loom — but brushing against it stings hard
 * (damage + poison), and it slowly herds toward swimmers. Dries out and
 * dies on land. Entity logic is original; textures are BetterEnd's cubozoa
 * (Team BetterX, MIT) — see THIRD-PARTY-NOTICES.md.
 */
public class StingerJelly extends Monster {
    private int stingCooldown;

    public StingerJelly(EntityType<? extends StingerJelly> type, Level level) {
        super(type, level);
        this.moveControl = new FloatMoveControl(this);
        this.xpReward = 3;
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 8.0)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FLYING_SPEED, 0.45)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    /** Deterministic per-entity color variant (blue or sulphur). */
    public boolean isSulphur() {
        return (this.getUUID().getLeastSignificantBits() & 1L) == 0L;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true; // it's a jellyfish
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(4, new LoomTowardTargetGoal(this));
        this.goalSelector.addGoal(8, new DriftGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide() || !this.isAlive()) {
            return;
        }
        // Out of water it sags and dries out.
        if (!this.isInWater()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.04, 0.0));
            if (this.tickCount % 40 == 0 && this.level() instanceof ServerLevel serverLevel) {
                this.hurtServer(serverLevel, this.damageSources().dryOut(), 1.0f);
            }
            return;
        }
        // The sting: anything fleshy pressing against the bell gets zapped.
        if (this.stingCooldown > 0) {
            this.stingCooldown--;
        }
        if (this.stingCooldown <= 0 && this.level() instanceof ServerLevel serverLevel) {
            List<Player> touching = serverLevel.getEntitiesOfClass(Player.class,
                    this.getBoundingBox().inflate(0.25));
            for (Player player : touching) {
                if (player.isCreative() || player.isSpectator()) {
                    continue;
                }
                if (this.doHurtTarget(serverLevel, player)) {
                    player.addEffect(new MobEffectInstance(MobEffects.POISON, 20 * 5, 0));
                    this.stingCooldown = 15;
                }
            }
        }
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null; // silent drifter — that's the scary part
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SLIME_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SLIME_DEATH;
    }

    private static boolean isWaterAt(Level level, double x, double y, double z) {
        return level.getFluidState(BlockPos.containing(x, y, z)).isSource();
    }

    /** Aimless bobbing drift, always staying inside the water column. */
    static class DriftGoal extends Goal {
        private final StingerJelly jelly;

        DriftGoal(StingerJelly jelly) {
            this.jelly = jelly;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return jelly.getTarget() == null && jelly.isInWater()
                    && jelly.getRandom().nextInt(this.adjustedTickDelay(50)) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            for (int attempt = 0; attempt < 6; attempt++) {
                double x = jelly.getX() + (jelly.getRandom().nextDouble() * 2 - 1) * 8.0;
                double y = jelly.getY() + (jelly.getRandom().nextDouble() * 2 - 1) * 3.0;
                double z = jelly.getZ() + (jelly.getRandom().nextDouble() * 2 - 1) * 8.0;
                if (isWaterAt(jelly.level(), x, y, z)) {
                    jelly.getMoveControl().setWantedPosition(x, y, z, 0.8);
                    return;
                }
            }
        }
    }

    /** No chase — a slow, inevitable closing of distance while you swim. */
    static class LoomTowardTargetGoal extends Goal {
        private final StingerJelly jelly;
        private int retargetCooldown;

        LoomTowardTargetGoal(StingerJelly jelly) {
            this.jelly = jelly;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = jelly.getTarget();
            return target != null && target.isAlive() && target.isInWater() && jelly.isInWater();
        }

        @Override
        public void tick() {
            LivingEntity target = jelly.getTarget();
            if (target == null || --this.retargetCooldown > 0) {
                return;
            }
            this.retargetCooldown = this.adjustedTickDelay(12);
            // Aim at a point in water near the target, not their exact feet.
            double x = target.getX();
            double y = target.getY(0.5);
            double z = target.getZ();
            if (isWaterAt(jelly.level(), x, y, z)) {
                jelly.getMoveControl().setWantedPosition(x, y, z, 1.0);
            }
        }
    }
}
