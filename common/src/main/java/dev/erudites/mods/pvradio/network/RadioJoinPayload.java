package dev.erudites.mods.pvradio.network;

import dev.erudites.mods.pvradio.PvRadioMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record RadioJoinPayload(int radio) implements CustomPacketPayload {

    public static final Type<RadioJoinPayload> TYPE = new Type<>(PvRadioMod.id("radio_join"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RadioJoinPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT,
        RadioJoinPayload::radio,
        RadioJoinPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
