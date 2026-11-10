package dev.erudites.mods.pvradio.client;

import dev.erudites.mods.pvradio.network.RadioStatePayload;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class RadioState {

    private static final Set<UUID> TALKING = new HashSet<>();

    private static @Nullable RadioStatePayload state;
    private static boolean openRequested;

    private RadioState() {}

    public static Optional<RadioStatePayload> state() {
        return Optional.ofNullable(state);
    }

    public static Optional<RadioStatePayload.RadioView> radio(final int id) {
        return state().flatMap(payload -> payload.radio(id));
    }

    public static void update(final RadioStatePayload payload) {
        state = payload;
    }

    public static boolean talking(final UUID player) {
        return TALKING.contains(player);
    }

    public static void talking(final UUID player, final boolean talking) {
        if (talking) {
            TALKING.add(player);
        } else {
            TALKING.remove(player);
        }
    }

    public static void requestOpen() {
        openRequested = true;
    }

    public static boolean consumeOpenRequest() {
        boolean requested = openRequested;
        openRequested = false;
        return requested;
    }

    public static void clear() {
        state = null;
        openRequested = false;
        TALKING.clear();
    }
}
