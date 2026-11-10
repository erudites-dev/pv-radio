package dev.erudites.mods.pvradio.fabric.network;

import dev.erudites.mods.pvradio.network.PvRadioNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public final class PvRadioFabricNetworking implements PvRadioNetworking {

    @Override
    public void send(final ServerPlayer player, final CustomPacketPayload payload) {
        if (ServerPlayNetworking.canSend(player, payload.type())) {
            ServerPlayNetworking.send(player, payload);
        }
    }
}
