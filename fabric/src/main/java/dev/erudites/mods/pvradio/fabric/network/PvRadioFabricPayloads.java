package dev.erudites.mods.pvradio.fabric.network;

import dev.erudites.mods.pvradio.PvRadioMod;
import dev.erudites.mods.pvradio.network.RadioCreatePayload;
import dev.erudites.mods.pvradio.network.RadioJoinPayload;
import dev.erudites.mods.pvradio.network.RadioLeavePayload;
import dev.erudites.mods.pvradio.network.RadioMemberPayload;
import dev.erudites.mods.pvradio.network.RadioMutePayload;
import dev.erudites.mods.pvradio.network.RadioStatePayload;
import dev.erudites.mods.pvradio.network.RadioTalkingPayload;
import dev.erudites.mods.pvradio.network.RequestRadioPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public final class PvRadioFabricPayloads {

    private PvRadioFabricPayloads() {}

    public static void registerTypes() {
        PayloadTypeRegistry.clientboundPlay().register(RadioStatePayload.TYPE, RadioStatePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(RadioTalkingPayload.TYPE, RadioTalkingPayload.CODEC);

        PayloadTypeRegistry.serverboundPlay().register(RequestRadioPayload.TYPE, RequestRadioPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(RadioCreatePayload.TYPE, RadioCreatePayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(RadioJoinPayload.TYPE, RadioJoinPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(RadioMemberPayload.TYPE, RadioMemberPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(RadioLeavePayload.TYPE, RadioLeavePayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(RadioMutePayload.TYPE, RadioMutePayload.CODEC);
    }

    public static void registerReceivers() {
        receive(RequestRadioPayload.TYPE);
        receive(RadioCreatePayload.TYPE);
        receive(RadioJoinPayload.TYPE);
        receive(RadioMemberPayload.TYPE);
        receive(RadioLeavePayload.TYPE);
        receive(RadioMutePayload.TYPE);
    }

    private static <T extends CustomPacketPayload> void receive(final CustomPacketPayload.Type<T> type) {
        ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> PvRadioMod.handle(context.player(), payload));
    }
}
