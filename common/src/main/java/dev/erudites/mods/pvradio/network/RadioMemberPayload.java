package dev.erudites.mods.pvradio.network;

import dev.erudites.mods.pvradio.PvRadioMod;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.UUID;

public record RadioMemberPayload(
    int radio,
    UUID target,
    RadioMemberAction action
) implements CustomPacketPayload {

    public static final Type<RadioMemberPayload> TYPE = new Type<>(PvRadioMod.id("radio_member"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RadioMemberPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT,
        RadioMemberPayload::radio,
        UUIDUtil.STREAM_CODEC,
        RadioMemberPayload::target,
        RadioMemberAction.STREAM_CODEC,
        RadioMemberPayload::action,
        RadioMemberPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
