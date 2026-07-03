package io.github.andrewwwwwwwwwwwwwww.roughlife;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Blood moons: some nights the horde does not thin out. Whether a given night
 * is a blood moon is derived deterministically from the world seed and the day
 * number, so it survives restarts and needs no saved state. During one,
 * Dangerous World spawns twice as fast with a higher cap, and the spawned
 * mobs are faster and hit harder.
 */
public final class BloodMoon {
    private BloodMoon() {}

    private static long announcedDay = -1;
    private static final Set<UUID> ANNOUNCED = new HashSet<>();
    /** The day roll is memoized: this runs per player per tick. */
    private static long rolledDay = Long.MIN_VALUE;
    private static boolean rolledResult;

    public static boolean isBloodMoonNight(ServerLevel level) {
        if (!RLConfig.get().bloodMoonEnabled) {
            return false;
        }
        long clock = level.getOverworldClockTime();
        long timeOfDay = clock % 24000L;
        if (timeOfDay < 13000L || timeOfDay >= 23000L) {
            return false;
        }
        long day = clock / 24000L;
        if (day != rolledDay) {
            rolledDay = day;
            RandomSource roll = RandomSource.create(level.getSeed() ^ (day * 0x9E3779B97F4A7C15L));
            rolledResult = roll.nextFloat() < (float) RLConfig.get().bloodMoonChance;
        }
        return rolledResult;
    }

    /** Shows the blood-moon title to each player once per blood-moon night. */
    public static void announce(ServerPlayer player) {
        long day = player.level().getOverworldClockTime() / 24000L;
        if (day != announcedDay) {
            announcedDay = day;
            ANNOUNCED.clear();
        }
        if (ANNOUNCED.add(player.getUUID())) {
            player.connection.send(new ClientboundSetTitleTextPacket(
                    Component.literal("Blood Moon").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD)));
            player.connection.send(new ClientboundSetSubtitleTextPacket(
                    Component.literal("The horde is restless tonight...").withStyle(ChatFormatting.RED)));
            player.level().playSound(null, player.blockPosition(),
                    SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 0.5f, 0.5f);
        }
    }
}
