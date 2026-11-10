package dev.erudites.mods.pvradio.paper.network;

import dev.erudites.mods.pvradio.PvRadioMod;
import dev.erudites.mods.pvradio.network.RadioCreatePayload;
import dev.erudites.mods.pvradio.network.RadioJoinPayload;
import dev.erudites.mods.pvradio.network.RadioLeavePayload;
import dev.erudites.mods.pvradio.network.RadioMemberPayload;
import dev.erudites.mods.pvradio.network.RadioMutePayload;
import dev.erudites.mods.pvradio.network.RadioStatePayload;
import dev.erudites.mods.pvradio.network.RadioTalkingPayload;
import dev.erudites.mods.pvradio.network.RequestRadioPayload;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.Messenger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PvRadioPaperPayloads {

    private static final List<CustomPacketPayload.Type<?>> CLIENTBOUND = List.of(
        RadioStatePayload.TYPE,
        RadioTalkingPayload.TYPE
    );
    private static final List<CustomPacketPayload.Type<?>> SERVERBOUND = List.of(
        RequestRadioPayload.TYPE,
        RadioCreatePayload.TYPE,
        RadioJoinPayload.TYPE,
        RadioMemberPayload.TYPE,
        RadioLeavePayload.TYPE,
        RadioMutePayload.TYPE
    );

    private final Map<Identifier, StreamCodec<RegistryFriendlyByteBuf, CustomPacketPayload>> codecs = new HashMap<>();

    public PvRadioPaperPayloads() {
        this.put(RadioStatePayload.TYPE, RadioStatePayload.CODEC);
        this.put(RadioTalkingPayload.TYPE, RadioTalkingPayload.CODEC);
        this.put(RequestRadioPayload.TYPE, RequestRadioPayload.CODEC);
        this.put(RadioCreatePayload.TYPE, RadioCreatePayload.CODEC);
        this.put(RadioJoinPayload.TYPE, RadioJoinPayload.CODEC);
        this.put(RadioMemberPayload.TYPE, RadioMemberPayload.CODEC);
        this.put(RadioLeavePayload.TYPE, RadioLeavePayload.CODEC);
        this.put(RadioMutePayload.TYPE, RadioMutePayload.CODEC);
    }

    public void register(final Plugin plugin) {
        Messenger messenger = plugin.getServer().getMessenger();
        for (CustomPacketPayload.Type<?> type : CLIENTBOUND) {
            messenger.registerOutgoingPluginChannel(plugin, type.id().toString());
        }
        for (CustomPacketPayload.Type<?> type : SERVERBOUND) {
            messenger.registerIncomingPluginChannel(plugin, type.id().toString(), this::receive);
        }
    }

    public byte[] encode(final RegistryAccess registries, final CustomPacketPayload payload) {
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries);
        try {
            this.codecs.get(payload.type().id()).encode(buffer, payload);
            byte[] bytes = new byte[buffer.readableBytes()];
            buffer.readBytes(bytes);
            return bytes;
        } finally {
            buffer.release();
        }
    }

    private void receive(
        final String channel,
        final Player bukkitPlayer,
        final byte[] message
    ) {
        StreamCodec<RegistryFriendlyByteBuf, CustomPacketPayload> codec = this.codecs.get(Identifier.parse(channel));
        if (codec == null) {
            return;
        }
        ServerPlayer player = ((CraftPlayer) bukkitPlayer).getHandle();
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(message), player.registryAccess());
        try {
            PvRadioMod.handle(player, codec.decode(buffer));
        } finally {
            buffer.release();
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends CustomPacketPayload> void put(
        final CustomPacketPayload.Type<T> type,
        final StreamCodec<RegistryFriendlyByteBuf, T> codec
    ) {
        this.codecs.put(type.id(), (StreamCodec<RegistryFriendlyByteBuf, CustomPacketPayload>) (StreamCodec<?, ?>) codec);
    }
}
