package dev.erudites.mods.pvradio.fabric.client.network;

import dev.erudites.mods.pvradio.client.network.PvRadioClientNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public final class PvRadioFabricClientNetworking implements PvRadioClientNetworking {

    @Override
    public boolean canSend(final CustomPacketPayload.Type<?> type) {
        return ClientPlayNetworking.canSend(type);
    }

    @Override
    public void send(final CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }
}
