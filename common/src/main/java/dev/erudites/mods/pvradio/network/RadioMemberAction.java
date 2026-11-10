package dev.erudites.mods.pvradio.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;

import java.util.function.IntFunction;

public enum RadioMemberAction {
    ADD,
    KICK;

    private static final IntFunction<RadioMemberAction> BY_ID = ByIdMap.continuous(
        RadioMemberAction::ordinal,
        values(),
        ByIdMap.OutOfBoundsStrategy.ZERO
    );

    public static final StreamCodec<ByteBuf, RadioMemberAction> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, RadioMemberAction::ordinal);
}
