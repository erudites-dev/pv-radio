package dev.erudites.mods.pvradio.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Immutable snapshot of a radio.
 *
 * <p>A snapshot does not follow later changes to the radio.
 * Call {@link RadioService#radio(int)} again to get the latest state.
 *
 * @param id      the radio id, unique within a world
 * @param name    the radio name
 * @param owner   the owner's UUID, always one of {@code members}
 * @param members the members in join order
 */
public record RadioInfo(
    int id,
    String name,
    UUID owner,
    List<RadioMemberInfo> members
) {

    /**
     * Creates a snapshot, copying {@code members} into an unmodifiable list.
     */
    public RadioInfo {
        members = List.copyOf(members);
    }

    /**
     * Finds a member of this radio.
     *
     * @param uuid the player's UUID
     * @return the member, or an empty {@code Optional} if the player is not a member
     */
    public Optional<RadioMemberInfo> member(final UUID uuid) {
        return this.members.stream()
            .filter(member -> member.uuid().equals(uuid))
            .findFirst();
    }

    /**
     * Checks whether a player is a member of this radio.
     *
     * @param uuid the player's UUID
     * @return {@code true} if the player is a member
     */
    public boolean contains(final UUID uuid) {
        return this.member(uuid).isPresent();
    }
}
