package dev.erudites.mods.pvradio.network;

import dev.erudites.mods.pvradio.PvRadioMod;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.UUID;

public record RadioTalkingPayload(UUID player, boolean talking) implements CustomPacketPayload {

    public static final Type<RadioTalkingPayload> TYPE = new Type<>(PvRadioMod.id("radio_talking"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RadioTalkingPayload> CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC,
        RadioTalkingPayload::player,
        ByteBufCodecs.BOOL,
        RadioTalkingPayload::talking,
        RadioTalkingPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
