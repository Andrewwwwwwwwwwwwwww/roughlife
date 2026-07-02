package io.github.andrewwwwwwwwwwwwwww.roughlife;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Server -> client sync of the thirst and temperature values for the HUD. */
public record StatsSyncPayload(int thirst, int temperature) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<StatsSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(RoughLife.MOD_ID, "stats"));

    public static final StreamCodec<RegistryFriendlyByteBuf, StatsSyncPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, StatsSyncPayload::thirst,
            ByteBufCodecs.VAR_INT, StatsSyncPayload::temperature,
            StatsSyncPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
