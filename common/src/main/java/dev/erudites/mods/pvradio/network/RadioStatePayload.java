package dev.erudites.mods.pvradio.network;

import dev.erudites.mods.pvradio.PvRadioMod;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record RadioStatePayload(
    boolean available,
    List<RadioView> radios,
    List<PlayerView> online
) implements CustomPacketPayload {

    public static final RadioStatePayload UNAVAILABLE = new RadioStatePayload(false, List.of(), List.of());

    public static final Type<RadioStatePayload> TYPE = new Type<>(PvRadioMod.id("radio_state"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RadioStatePayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL,
        RadioStatePayload::available,
        RadioView.STREAM_CODEC.apply(ByteBufCodecs.list()),
        RadioStatePayload::radios,
        PlayerView.STREAM_CODEC.apply(ByteBufCodecs.list()),
        RadioStatePayload::online,
        RadioStatePayload::new
    );

    public RadioStatePayload {
        radios = List.copyOf(radios);
        online = List.copyOf(online);
    }

    public Optional<RadioView> radio(final int id) {
        return this.radios.stream()
            .filter(radio -> radio.id() == id)
            .findFirst();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public record RadioView(
        int id,
        String name,
        UUID owner,
        boolean canManage,
        List<MemberView> members
    ) {

        public static final StreamCodec<RegistryFriendlyByteBuf, RadioView> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            RadioView::id,
            ByteBufCodecs.STRING_UTF8,
            RadioView::name,
            UUIDUtil.STREAM_CODEC,
            RadioView::owner,
            ByteBufCodecs.BOOL,
            RadioView::canManage,
            MemberView.STREAM_CODEC.apply(ByteBufCodecs.list()),
            RadioView::members,
            RadioView::new
        );

        public RadioView {
            members = List.copyOf(members);
        }

        public Optional<MemberView> member(final UUID uuid) {
            return this.members.stream()
                .filter(member -> member.uuid().equals(uuid))
                .findFirst();
        }

        public boolean contains(final UUID uuid) {
            return this.member(uuid).isPresent();
        }
    }

    public record MemberView(
        UUID uuid,
        Component name,
        boolean online,
        boolean micEnabled,
        boolean speakerEnabled
    ) {

        public static final StreamCodec<RegistryFriendlyByteBuf, MemberView> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            MemberView::uuid,
            ComponentSerialization.STREAM_CODEC,
            MemberView::name,
            ByteBufCodecs.BOOL,
            MemberView::online,
            ByteBufCodecs.BOOL,
            MemberView::micEnabled,
            ByteBufCodecs.BOOL,
            MemberView::speakerEnabled,
            MemberView::new
        );
    }

    public record PlayerView(
        UUID uuid,
        Component name,
        boolean voiceConnected
    ) {

        public static final StreamCodec<RegistryFriendlyByteBuf, PlayerView> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            PlayerView::uuid,
            ComponentSerialization.STREAM_CODEC,
            PlayerView::name,
            ByteBufCodecs.BOOL,
            PlayerView::voiceConnected,
            PlayerView::new
        );
    }
}
