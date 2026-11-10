package dev.erudites.mods.pvradio.api;

import java.util.UUID;

/**
 * Receives radio changes.
 *
 * <p>Every method has an empty default implementation, so override only the ones you need.
 *
 * <p>Callbacks run on the server thread after the change is applied, whether it came from a player,
 * a command or another API caller. Changes made from inside a callback fire their own events.
 * Exceptions thrown by a listener are logged and do not affect other listeners.
 *
 * @see PvRadioApi#addListener(RadioListener)
 */
public interface RadioListener {

    /**
     * Called when a radio is created.
     *
     * <p>The initial members do not get separate {@link #onMemberJoined} calls.
     *
     * @param radio the new radio
     */
    default void onRadioCreated(final RadioInfo radio) {}

    /**
     * Called when a radio is deleted, either explicitly or because its last member left.
     *
     * <p>The remaining members do not get separate {@link #onMemberLeft} calls.
     *
     * @param radio the radio as it was right before deletion
     */
    default void onRadioDeleted(final RadioInfo radio) {}

    /**
     * Called when a player joins or is added to a radio.
     *
     * @param radio  the radio after the change, including {@code member}
     * @param member the new member
     */
    default void onMemberJoined(final RadioInfo radio, final RadioMemberInfo member) {}

    /**
     * Called when a player leaves or is removed from a radio that still has other members.
     *
     * @param radio  the radio after the change, no longer including {@code member}
     * @param member the member who left
     */
    default void onMemberLeft(final RadioInfo radio, final RadioMemberInfo member) {}

    /**
     * Called when ownership of a radio moves to another member.
     *
     * @param radio         the radio after the change; {@link RadioInfo#owner()} is the new owner
     * @param previousOwner the UUID of the previous owner
     */
    default void onOwnerChanged(final RadioInfo radio, final UUID previousOwner) {}

    /**
     * Called when a member turns their mic or speaker on or off.
     *
     * @param radio  the radio after the change
     * @param member the member with their new settings
     */
    default void onMemberAudioChanged(final RadioInfo radio, final RadioMemberInfo member) {}

    /**
     * Called when a player starts or stops transmitting over their radios.
     *
     * @param player  the player's UUID
     * @param talking {@code true} if the player started transmitting, {@code false} if they stopped
     */
    default void onTalkingChanged(final UUID player, final boolean talking) {}
}
