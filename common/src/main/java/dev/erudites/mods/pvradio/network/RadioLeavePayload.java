package dev.erudites.mods.pvradio.network;

import dev.erudites.mods.pvradio.PvRadioMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record RadioLeavePayload(int radio) implements CustomPacketPayload {

    public static final Type<RadioLeavePayload> TYPE = new Type<>(PvRadioMod.id("radio_leave"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RadioLeavePayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT,
        RadioLeavePayload::radio,
        RadioLeavePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
