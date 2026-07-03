package io.github.andrewwwwwwwwwwwwwww.roughlife.entity;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.Vec3;

/** Direct-velocity flight, ghast-style: drift toward the wanted point. */
public class FloatMoveControl extends MoveControl {
    private final Mob flyer;

    public FloatMoveControl(Mob flyer) {
        super(flyer);
        this.flyer = flyer;
    }

    @Override
    public void tick() {
        if (this.operation != Operation.MOVE_TO) {
            return;
        }
        Vec3 toTarget = new Vec3(this.getWantedX() - flyer.getX(),
                this.getWantedY() - flyer.getY(), this.getWantedZ() - flyer.getZ());
        double distance = toTarget.length();
        if (distance < 0.4) {
            this.operation = Operation.WAIT;
            flyer.setDeltaMovement(flyer.getDeltaMovement().scale(0.6));
            return;
        }
        double accel = 0.08 * this.getSpeedModifier();
        flyer.setDeltaMovement(flyer.getDeltaMovement().scale(0.85)
                .add(toTarget.scale(accel / distance)));
        // Don't buzz against tree trunks and walls forever: climb over.
        if (flyer.horizontalCollision) {
            flyer.setDeltaMovement(flyer.getDeltaMovement().add(0.0, 0.06, 0.0));
        }
        // Face where we're going.
        Vec3 motion = flyer.getDeltaMovement();
        if (motion.horizontalDistanceSqr() > 1.0E-4) {
            flyer.setYRot((float) (Math.atan2(motion.z, motion.x) * (180.0 / Math.PI)) - 90.0f);
            flyer.yBodyRot = flyer.getYRot();
        }
    }
}
