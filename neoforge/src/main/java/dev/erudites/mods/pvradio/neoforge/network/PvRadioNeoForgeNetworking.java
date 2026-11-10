package dev.erudites.mods.pvradio.neoforge.network;

import dev.erudites.mods.pvradio.network.PvRadioNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public final class PvRadioNeoForgeNetworking implements PvRadioNetworking {

    @Override
    public void send(final ServerPlayer player, final CustomPacketPayload payload) {
        if (player.connection.hasChannel(payload)) {
            PacketDistributor.sendToPlayer(player, payload);
        }
    }
}
