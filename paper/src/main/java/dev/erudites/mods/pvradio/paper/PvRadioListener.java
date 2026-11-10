package dev.erudites.mods.pvradio.paper;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import dev.erudites.mods.pvradio.PvRadioMod;
import dev.erudites.mods.pvradio.network.RadioStatePayload;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRegisterChannelEvent;

public final class PvRadioListener implements Listener {

    private static final String STATE_CHANNEL = RadioStatePayload.TYPE.id().toString();

    @EventHandler
    public void onTickEnd(final ServerTickEndEvent event) {
        PvRadioMod.tick();
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        PvRadioMod.playerJoined(((CraftPlayer) event.getPlayer()).getHandle());
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        PvRadioMod.playerLeft();
    }

    @EventHandler
    public void onRegisterChannel(final PlayerRegisterChannelEvent event) {
        if (event.getChannel().equals(STATE_CHANNEL)) {
            PvRadioMod.channelsRegistered(((CraftPlayer) event.getPlayer()).getHandle());
        }
    }
}
