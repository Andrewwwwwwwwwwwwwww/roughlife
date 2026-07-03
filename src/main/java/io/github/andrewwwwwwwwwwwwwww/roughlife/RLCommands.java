package io.github.andrewwwwwwwwwwwwwww.roughlife;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class RLCommands {
    private RLCommands() {}

    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("roughlife")
                    .then(Commands.literal("guide").executes(ctx -> {
                        String[] lines = {
                                "=== Rough Life: how to survive ===",
                                "1. Sneak + right-click dirt/stone/sand with an EMPTY hand to gather Rocks.",
                                "2. Right-click stone holding Flint (60%) or a Rock (40%) to knap Flint Shards. Gravel drops flint; 3 rocks also craft into flint.",
                                "3. Break grass for Plant Fiber (3 = string); punch leaves for sticks.",
                                "4. Shard + stick = Flint Knife. Shard + fiber + stick = Flint Hatchet — punching logs bare-handed destroys them but drops NOTHING.",
                                "4b. Chopping a log with any axe fells the whole tree (sneak to take a single log).",
                                "5. Thirst: drink ANY water bottle (risky), loot Dirty Water from chests and COOK it in a furnace/campfire into Purified Water (safe, big restore), or craft a Leather Canteen (4 leather, 2 string, bottle) to sip on the go. A Charcoal Filter also cleans bottles in the crafting grid.",
                                "6. Temperature: watch the thermometer. Cold = vanilla freezing (leather armor blocks it, campfires warm you). Heat = heatstroke (shade, water, night).",
                                "7. Injuries: Bandage stops Bleeding, Splint fixes Fractured Bones from falls.",
                                "8. Health barely regenerates — food, bandages and beds are your healers.",
                                "9. The dark is HUNTING you: at night and underground, monsters keep coming. Light and walls are survival, not decoration.",
                                "10. Daylight isn't safe: pillager packs roam the land and drowned stalk open water. Shelter (a roof!) keeps you warm; hostiles within 40 blocks (or an empty stomach) keep you from sleeping.",
                                "11. Death scatters you to a strange new place — unless you've claimed a bed.",
                                "10. If the sky announces a BLOOD MOON, get inside: the horde comes twice as fast, stronger and quicker, until dawn.",
                        };
                        for (String line : lines) {
                            ctx.getSource().sendSuccess(() -> Component.literal(line), false);
                        }
                        return 1;
                    }))
                    .then(Commands.literal("status").executes(ctx -> {
                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                        RLPlayerData data = (RLPlayerData) player;
                        ctx.getSource().sendSuccess(() -> Component.literal(String.format(
                                "Thirst: %d/20 | Body temperature: %.1f (20 is comfortable)",
                                data.roughlife$getThirst(), data.roughlife$getTemperature())), false);
                        return 1;
                    }))
                    .then(Commands.literal("reload")
                            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                            .executes(ctx -> {
                                RLConfig.load();
                                ctx.getSource().sendSuccess(
                                        () -> Component.literal("Rough Life config reloaded."), true);
                                return 1;
                            }))
                    .then(Commands.literal("thirst")
                            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                            .then(Commands.argument("value", IntegerArgumentType.integer(0, 20))
                                    .executes(ctx -> {
                                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                                        int value = IntegerArgumentType.getInteger(ctx, "value");
                                        ((RLPlayerData) player).roughlife$setThirst(value);
                                        ctx.getSource().sendSuccess(
                                                () -> Component.literal("Thirst set to " + value + "."), true);
                                        return 1;
                                    }))));
        });
    }
}
