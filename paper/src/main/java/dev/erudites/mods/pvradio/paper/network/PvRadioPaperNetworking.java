package dev.erudites.mods.pvradio.paper.network;

import dev.erudites.mods.pvradio.network.PvRadioNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class PvRadioPaperNetworking implements PvRadioNetworking {

    private final Plugin plugin;
    private final PvRadioPaperPayloads payloads;

    public PvRadioPaperNetworking(final Plugin plugin, final PvRadioPaperPayloads payloads) {
        this.plugin = plugin;
        this.payloads = payloads;
    }

    @Override
    public void send(final ServerPlayer player, final CustomPacketPayload payload) {
        String channel = payload.type().id().toString();
        Player bukkitPlayer = player.getBukkitEntity();
        if (bukkitPlayer.getListeningPluginChannels().contains(channel)) {
            bukkitPlayer.sendPluginMessage(this.plugin, channel, this.payloads.encode(player.registryAccess(), payload));
        }
    }
}
