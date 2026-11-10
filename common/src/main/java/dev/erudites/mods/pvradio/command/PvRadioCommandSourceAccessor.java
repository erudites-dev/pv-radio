package dev.erudites.mods.pvradio.command;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public interface PvRadioCommandSourceAccessor<S> {

    Optional<ServerPlayer> player(final S source);

    void failure(final S source, final Component message);
}
