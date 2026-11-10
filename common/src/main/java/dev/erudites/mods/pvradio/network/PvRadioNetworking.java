package dev.erudites.mods.pvradio.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public interface PvRadioNetworking {

    void send(final ServerPlayer player, final CustomPacketPayload payload);
}
