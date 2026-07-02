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
