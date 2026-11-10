package dev.erudites.mods.pvradio.network;

import dev.erudites.mods.pvradio.PvRadioMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record RequestRadioPayload() implements CustomPacketPayload {

    public static final RequestRadioPayload INSTANCE = new RequestRadioPayload();

    public static final Type<RequestRadioPayload> TYPE = new Type<>(PvRadioMod.id("request_radio"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestRadioPayload> CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
