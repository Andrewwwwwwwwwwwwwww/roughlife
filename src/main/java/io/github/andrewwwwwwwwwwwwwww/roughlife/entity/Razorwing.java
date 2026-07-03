package io.github.andrewwwwwwwwwwwwwww.roughlife.entity;

import io.github.andrewwwwwwwwwwwwwww.roughlife.RLEffects;
import io.github.andrewwwwwwwwwwwwwww.roughlife.RLSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
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
 * A giant predatory dragonfly that patrols the daytime sky and dive-bombs
 * travelers, its wing edges slicing like knives (hits cause Bleeding).
 * Entity logic is original; texture and buzz sounds are from BetterEnd
 * (Team BetterX, MIT) — see THIRD-PARTY-NOTICES.md.
 */
public class Razorwing extends Monster {
    /** How far from its roost a razorwing defends. Visible, avoidable danger. */
    public static final double TERRITORY_RADIUS = 24.0;

    private BlockPos anchor;

    public Razorwing(EntityType<? extends Razorwing> type, Level level) {
        super(type, level);
        this.moveControl = new FloatMoveControl(this);
        this.xpReward = 5;
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.MOVEMENT_SPEED, 0.30)
                .add(Attributes.FLYING_SPEED, 1.10)
                .add(Attributes.FOLLOW_RANGE, 44.0);
    }

    public BlockPos anchor() {
        return this.anchor != null ? this.anchor : this.blockPosition();
    }

    /** Territorial, not a hunter: only players inside its airspace count. */
    public boolean withinTerritory(net.minecraft.world.entity.Entity target) {
        BlockPos home = this.anchor();
        double dx = target.getX() - (home.getX() + 0.5);
        double dz = target.getZ() - (home.getZ() + 0.5);
        return dx * dx + dz * dz <= TERRITORY_RADIUS * TERRITORY_RADIUS;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) {
            return;
        }
        if (this.anchor == null) {
            this.anchor = this.blockPosition(); // claim the roost where it spawned
        }
        // Drop aggro the moment the intruder leaves the territory.
        LivingEntity target = this.getTarget();
        if (target != null && !this.withinTerritory(target)) {
            this.setTarget(null);
        }
    }

    @Override
    protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (this.anchor != null) {
            output.putInt("roughlife_anchor_x", this.anchor.getX());
            output.putInt("roughlife_anchor_y", this.anchor.getY());
            output.putInt("roughlife_anchor_z", this.anchor.getZ());
        }
    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput input) {
        super.readAdditionalSaveData(input);
        if (input.getInt("roughlife_anchor_x").isPresent()) {
            this.anchor = new BlockPos(
                    input.getIntOr("roughlife_anchor_x", 0),
                    input.getIntOr("roughlife_anchor_y", 64),
                    input.getIntOr("roughlife_anchor_z", 0));
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(4, new DiveAttackGoal(this));
        this.goalSelector.addGoal(8, new PatrolSkyGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return RLSounds.RAZORWING_BUZZ;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PHANTOM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PHANTOM_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.9f;
    }

    /** Lazy high circling between hunts. */
    static class PatrolSkyGoal extends Goal {
        private final Razorwing razorwing;

        PatrolSkyGoal(Razorwing razorwing) {
            this.razorwing = razorwing;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return razorwing.getTarget() == null
                    && razorwing.getRandom().nextInt(this.adjustedTickDelay(30)) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            // Circle the roost, high enough to read as a silhouette from afar.
            BlockPos home = razorwing.anchor();
            double x = home.getX() + (razorwing.getRandom().nextDouble() * 2 - 1) * TERRITORY_RADIUS * 0.8;
            double y = home.getY() + 8.0 + razorwing.getRandom().nextDouble() * 8.0;
            double z = home.getZ() + (razorwing.getRandom().nextDouble() * 2 - 1) * TERRITORY_RADIUS * 0.8;
            BlockPos below = BlockPos.containing(x, y, z).below();
            if (!razorwing.level().getBlockState(below).isAir()) {
                y = razorwing.getY() + 4.0;
            }
            razorwing.getMoveControl().setWantedPosition(x, y, z, 1.0);
        }
    }

    /** Dive at the target; wing edges cut (Bleeding), then climb away. */
    static class DiveAttackGoal extends Goal {
        private final Razorwing razorwing;
        private int retargetCooldown;

        DiveAttackGoal(Razorwing razorwing) {
            this.razorwing = razorwing;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = razorwing.getTarget();
            return target != null && target.isAlive() && razorwing.withinTerritory(target);
        }

        @Override
        public void tick() {
            LivingEntity target = razorwing.getTarget();
            if (target == null) {
                return;
            }
            if (--this.retargetCooldown <= 0) {
                this.retargetCooldown = this.adjustedTickDelay(5);
                razorwing.getMoveControl().setWantedPosition(
                        target.getX(), target.getEyeY() + 0.25, target.getZ(), 1.7);
            }
            if (razorwing.getBoundingBox().inflate(0.4).intersects(target.getBoundingBox())
                    && razorwing.level() instanceof ServerLevel serverLevel) {
                if (razorwing.doHurtTarget(serverLevel, target) && target instanceof Player player) {
                    player.addEffect(new MobEffectInstance(RLEffects.BLEEDING, 20 * 4, 0));
                }
                // Climb hard after the pass and take a long breath before the
                // next run — the player gets a real window to react or leave.
                razorwing.getMoveControl().setWantedPosition(
                        target.getX() + (razorwing.getRandom().nextDouble() * 2 - 1) * 12.0,
                        target.getEyeY() + 8.0 + razorwing.getRandom().nextDouble() * 4.0,
                        target.getZ() + (razorwing.getRandom().nextDouble() * 2 - 1) * 12.0, 1.3);
                this.retargetCooldown = this.adjustedTickDelay(60);
            }
        }
    }
}
