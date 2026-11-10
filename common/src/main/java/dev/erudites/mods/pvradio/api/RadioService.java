package dev.erudites.mods.pvradio.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Server-side access to radios.
 *
 * <p>Query methods can be called from any thread. Methods that change radios must be called on the
 * server thread. Do not keep a reference to this object across server restarts; get it again with
 * {@link PvRadioApi#service()}.
 *
 * <p>Changes made through this interface are saved, synced to clients and applied to voice routing
 * just like player actions, but no chat messages are sent to players.
 *
 * @see PvRadioApi#service()
 */
public interface RadioService {

    /**
     * The maximum length of a radio name.
     */
    int MAX_NAME_LENGTH = 16;

    /**
     * Checks whether Plasmo Voice is loaded and radio audio works.
     *
     * <p>Radios can still be managed when this returns {@code false}.
     *
     * @return {@code true} if radio audio is available
     */
    boolean voiceAvailable();

    /**
     * Checks whether a player is online with a working Plasmo Voice connection.
     *
     * @param player the player's UUID
     * @return {@code true} if the player is connected to voice chat
     */
    boolean voiceConnected(UUID player);

    /**
     * Returns all radios.
     *
     * @return an unmodifiable list of radios, ordered by creation
     */
    List<RadioInfo> radios();

    /**
     * Finds a radio by id.
     *
     * @param id the radio id
     * @return the radio, or an empty {@code Optional} if it does not exist
     */
    Optional<RadioInfo> radio(int id);

    /**
     * Returns the radios a player is a member of.
     *
     * @param player the player's UUID
     * @return an unmodifiable list of radios, ordered by creation
     */
    List<RadioInfo> joinedRadios(UUID player);

    /**
     * Checks whether a player is currently transmitting over a radio.
     *
     * @param player the player's UUID
     * @return {@code true} if the player is talking
     */
    boolean talking(UUID player);

    /**
     * Creates a radio. The owner becomes its first member.
     *
     * @param name  the radio name; leading and trailing whitespace is removed
     * @param owner the UUID of the owner, who must be online
     * @return the new radio, or an empty {@code Optional} if the owner is offline or the name is empty
     *     or longer than {@link #MAX_NAME_LENGTH}
     * @throws IllegalStateException if not called on the server thread, or after the server stopped
     */
    Optional<RadioInfo> createRadio(String name, UUID owner);

    /**
     * Adds a player to a radio.
     *
     * @param radioId the radio id
     * @param player  the UUID of the player to add, who must be online
     * @return {@code true} if the player was added; {@code false} if the radio does not exist,
     *     the player is offline or already a member
     * @throws IllegalStateException if not called on the server thread, or after the server stopped
     */
    boolean addMember(int radioId, UUID player);

    /**
     * Removes a player from a radio.
     *
     * <p>If the player was the owner, ownership moves to another member, preferring online members.
     * If the player was the last member, the radio is deleted.
     *
     * @param radioId the radio id
     * @param player  the UUID of the player to remove
     * @return {@code true} if the player was removed; {@code false} if the radio does not exist
     *     or the player is not a member
     * @throws IllegalStateException if not called on the server thread, or after the server stopped
     */
    boolean removeMember(int radioId, UUID player);

    /**
     * Makes a member the owner of a radio.
     *
     * @param radioId the radio id
     * @param player  the UUID of the new owner
     * @return {@code true} if the player is now the owner; {@code false} if the radio does not exist
     *     or the player is not a member
     * @throws IllegalStateException if not called on the server thread, or after the server stopped
     */
    boolean setOwner(int radioId, UUID player);

    /**
     * Sets whether a member transmits to and hears a radio.
     *
     * @param radioId        the radio id
     * @param player         the member's UUID
     * @param micEnabled     {@code true} to let the member transmit
     * @param speakerEnabled {@code true} to let the member hear the radio
     * @return {@code true} if the settings were applied; {@code false} if the radio does not exist
     *     or the player is not a member
     * @throws IllegalStateException if not called on the server thread, or after the server stopped
     */
    boolean setAudio(int radioId, UUID player, boolean micEnabled, boolean speakerEnabled);

    /**
     * Deletes a radio and removes all its members.
     *
     * @param radioId the radio id
     * @return {@code true} if the radio was deleted; {@code false} if it does not exist
     * @throws IllegalStateException if not called on the server thread, or after the server stopped
     */
    boolean deleteRadio(int radioId);
}
