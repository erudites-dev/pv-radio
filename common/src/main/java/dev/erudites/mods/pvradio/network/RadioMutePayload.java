package dev.erudites.mods.pvradio.network;

import dev.erudites.mods.pvradio.PvRadioMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record RadioMutePayload(
    int radio,
    boolean micEnabled,
    boolean speakerEnabled
) implements CustomPacketPayload {

    public static final Type<RadioMutePayload> TYPE = new Type<>(PvRadioMod.id("radio_mute"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RadioMutePayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT,
        RadioMutePayload::radio,
        ByteBufCodecs.BOOL,
        RadioMutePayload::micEnabled,
        ByteBufCodecs.BOOL,
        RadioMutePayload::speakerEnabled,
        RadioMutePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
