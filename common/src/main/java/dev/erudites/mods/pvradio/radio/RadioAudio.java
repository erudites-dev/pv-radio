package dev.erudites.mods.pvradio.radio;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface RadioAudio {

    boolean available();

    boolean voiceConnected(final UUID player);

    Set<UUID> talking();

    void attach(final Runnable onChange);

    void detach();

    void lines(final Map<Integer, String> names);

    void routes(final List<RadioRoute> routes);
}
