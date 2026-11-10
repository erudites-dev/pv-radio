package dev.erudites.mods.pvradio.network;

import dev.erudites.mods.pvradio.radio.RadioServiceImpl;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public final class RadioPayloadHandler {

    private final RadioServiceImpl radio;

    public RadioPayloadHandler(final RadioServiceImpl radio) {
        this.radio = radio;
    }

    public void handle(final ServerPlayer player, final CustomPacketPayload payload) {
        switch (payload) {
            case RequestRadioPayload _ -> this.radio.sync(player);
            case RadioCreatePayload create -> this.radio.create(player, create.name());
            case RadioJoinPayload join -> this.radio.join(player, join.radio());
            case RadioMemberPayload member -> this.handleMember(player, member);
            case RadioLeavePayload leave -> this.radio.leave(player, leave.radio());
            case RadioMutePayload mute -> this.radio.mute(
                player,
                mute.radio(),
                mute.micEnabled(),
                mute.speakerEnabled()
            );
            default -> {}
        }
    }

    private void handleMember(final ServerPlayer player, final RadioMemberPayload payload) {
        switch (payload.action()) {
            case ADD -> this.radio.add(player, payload.radio(), payload.target());
            case KICK -> this.radio.kick(player, payload.radio(), payload.target());
        }
    }
}
