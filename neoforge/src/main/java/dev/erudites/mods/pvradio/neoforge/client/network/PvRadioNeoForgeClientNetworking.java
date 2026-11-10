package dev.erudites.mods.pvradio.neoforge.client.network;

import dev.erudites.mods.pvradio.client.network.PvRadioClientNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public final class PvRadioNeoForgeClientNetworking implements PvRadioClientNetworking {

    @Override
    public boolean canSend(final CustomPacketPayload.Type<?> type) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        return connection != null && connection.hasChannel(type);
    }

    @Override
    public void send(final CustomPacketPayload payload) {
        ClientPacketDistributor.sendToServer(payload);
    }
}
