package dev.erudites.mods.pvradio.api;

import dev.erudites.mods.pvradio.PvRadioMod;
import dev.erudites.mods.pvradio.radio.RadioListeners;

import java.util.Optional;

/**
 * Entry point of the PV Radio API for other mods and plugins.
 *
 * <pre>{@code
 * PvRadioApi.addListener(new RadioListener() {
 *     @Override
 *     public void onMemberJoined(RadioInfo radio, RadioMemberInfo member) {
 *         // ...
 *     }
 * });
 *
 * PvRadioApi.service().ifPresent(radios -> radios.createRadio("Team", player.getUUID()));
 * }</pre>
 */
public final class PvRadioApi {

    private PvRadioApi() {}

    /**
     * Returns the radio service of the running server.
     *
     * @return the radio service, or an empty {@code Optional} while no server is running
     */
    public static Optional<RadioService> service() {
        return PvRadioMod.radioService();
    }

    /**
     * Registers a listener.
     *
     * <p>Listeners stay registered across server restarts, so this can be called during mod
     * initialization. Registering the same listener twice has no effect.
     *
     * @param listener the listener to register
     * @throws NullPointerException if {@code listener} is {@code null}
     */
    public static void addListener(final RadioListener listener) {
        RadioListeners.add(listener);
    }

    /**
     * Unregisters a listener. Does nothing if the listener is not registered.
     *
     * @param listener the listener to unregister
     */
    public static void removeListener(final RadioListener listener) {
        RadioListeners.remove(listener);
    }
}
