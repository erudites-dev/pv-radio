package dev.erudites.mods.pvradio.client;

import dev.erudites.mods.pvradio.radio.Radio;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public final class RadioVolumes {

    public static final double DEFAULT_VOLUME = 1.0;
    public static final double MAX_VOLUME = 2.0;

    private static @Nullable Backend backend;

    private RadioVolumes() {}

    public static void backend(final Backend backend) {
        RadioVolumes.backend = backend;
    }

    public static boolean available() {
        return ready().isPresent();
    }

    public static double volume(final int radioId) {
        return ready()
            .map(present -> present.volume(Radio.lineName(radioId)))
            .orElse(DEFAULT_VOLUME);
    }

    public static void volume(final int radioId, final double volume) {
        ready().ifPresent(present -> present.volume(Radio.lineName(radioId), Math.clamp(volume, 0.0, MAX_VOLUME)));
    }

    private static Optional<Backend> ready() {
        return Optional.ofNullable(backend).filter(Backend::ready);
    }

    public interface Backend {

        boolean ready();

        double volume(final String lineName);

        void volume(final String lineName, final double volume);
    }
}
