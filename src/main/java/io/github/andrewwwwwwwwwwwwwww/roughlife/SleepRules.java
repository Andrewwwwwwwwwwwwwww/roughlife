package io.github.andrewwwwwwwwwwwwwww.roughlife;

import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

/**
 * Sleeping through the night is a privilege, not a right: any hostile in a
 * wide radius keeps you awake (much wider than vanilla's 8 blocks — with the
 * Dangerous World topping up night spawns, an unsecured village bed won't
 * cut it), and you can't nod off parched or starving.
 */
public final class SleepRules {
    private SleepRules() {}

    public static void init() {
        EntitySleepEvents.ALLOW_SLEEPING.register((player, sleepingPos) -> {
            if (!(player instanceof ServerPlayer serverPlayer)) {
                return null;
            }
            ServerLevel level = serverPlayer.level();
            int radius = Math.max(8, RLConfig.get().sleepHostileRadius);
            AABB box = new AABB(sleepingPos).inflate(radius, radius / 2.0, radius);
            if (!level.getEntitiesOfClass(Monster.class, box).isEmpty()) {
                return Player.BedSleepingProblem.NOT_SAFE;
            }
            if (RLConfig.get().sleepNeedsComfort && player instanceof RLPlayerData data
                    && (data.roughlife$getThirst() < 10 || serverPlayer.getFoodData().getFoodLevel() < 10)) {
                return new Player.BedSleepingProblem(Component.literal(
                        "You're too parched and hungry to sleep — eat and drink first"));
            }
            return null;
        });
    }
}
