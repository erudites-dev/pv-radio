package dev.erudites.mods.pvradio.neoforge.network;

import dev.erudites.mods.pvradio.PvRadioMod;
import dev.erudites.mods.pvradio.network.RadioCreatePayload;
import dev.erudites.mods.pvradio.network.RadioJoinPayload;
import dev.erudites.mods.pvradio.network.RadioLeavePayload;
import dev.erudites.mods.pvradio.network.RadioMemberPayload;
import dev.erudites.mods.pvradio.network.RadioMutePayload;
import dev.erudites.mods.pvradio.network.RadioStatePayload;
import dev.erudites.mods.pvradio.network.RadioTalkingPayload;
import dev.erudites.mods.pvradio.network.RequestRadioPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class PvRadioNeoForgePayloads {

    private static final String VERSION = "1";

    private PvRadioNeoForgePayloads() {}

    public static void register(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(VERSION).optional();

        registrar.playToClient(RadioStatePayload.TYPE, RadioStatePayload.CODEC);
        registrar.playToClient(RadioTalkingPayload.TYPE, RadioTalkingPayload.CODEC);

        registrar.playToServer(RequestRadioPayload.TYPE, RequestRadioPayload.CODEC, receiver());
        registrar.playToServer(RadioCreatePayload.TYPE, RadioCreatePayload.CODEC, receiver());
        registrar.playToServer(RadioJoinPayload.TYPE, RadioJoinPayload.CODEC, receiver());
        registrar.playToServer(RadioMemberPayload.TYPE, RadioMemberPayload.CODEC, receiver());
        registrar.playToServer(RadioLeavePayload.TYPE, RadioLeavePayload.CODEC, receiver());
        registrar.playToServer(RadioMutePayload.TYPE, RadioMutePayload.CODEC, receiver());
    }

    private static <T extends CustomPacketPayload> IPayloadHandler<T> receiver() {
        return (payload, context) -> PvRadioMod.handle((ServerPlayer) context.player(), payload);
    }
}
