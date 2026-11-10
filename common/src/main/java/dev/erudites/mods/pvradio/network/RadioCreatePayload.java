package dev.erudites.mods.pvradio.network;

import dev.erudites.mods.pvradio.PvRadioMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record RadioCreatePayload(String name) implements CustomPacketPayload {

    private static final int MAX_WIRE_LENGTH = 64;

    public static final Type<RadioCreatePayload> TYPE = new Type<>(PvRadioMod.id("radio_create"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RadioCreatePayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.stringUtf8(MAX_WIRE_LENGTH),
        RadioCreatePayload::name,
        RadioCreatePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
