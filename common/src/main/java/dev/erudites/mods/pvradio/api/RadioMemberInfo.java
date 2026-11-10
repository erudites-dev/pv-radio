package dev.erudites.mods.pvradio.api;

import java.util.UUID;

/**
 * Immutable snapshot of a radio member.
 *
 * @param uuid           the player's UUID
 * @param name           the player's last known display name
 * @param micEnabled     {@code true} if the member transmits to the radio
 * @param speakerEnabled {@code true} if the member hears the radio
 * @see RadioInfo#members()
 */
public record RadioMemberInfo(
    UUID uuid,
    String name,
    boolean micEnabled,
    boolean speakerEnabled
) {}
