package dev.erudites.mods.pvradio.client.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface PvRadioClientNetworking {

    boolean canSend(final CustomPacketPayload.Type<?> type);

    void send(final CustomPacketPayload payload);
}
