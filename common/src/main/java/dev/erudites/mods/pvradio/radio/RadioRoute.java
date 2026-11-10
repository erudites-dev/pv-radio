package dev.erudites.mods.pvradio.radio;

import java.util.Set;
import java.util.UUID;

public record RadioRoute(
    UUID sender,
    int radio,
    Set<UUID> listeners
) {

    public RadioRoute {
        listeners = Set.copyOf(listeners);
    }
}
