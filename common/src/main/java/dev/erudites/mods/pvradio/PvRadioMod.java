package dev.erudites.mods.pvradio;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.erudites.mods.pvradio.command.PvRadioCommandSourceAccessor;
import dev.erudites.mods.pvradio.command.RadioCommand;
import dev.erudites.mods.pvradio.api.RadioService;
import dev.erudites.mods.pvradio.compat.voice.RadioVoiceAddon;
import dev.erudites.mods.pvradio.network.PvRadioNetworking;
import dev.erudites.mods.pvradio.radio.RadioAudio;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

public final class PvRadioMod {

    public static final String MODID = "pvradio";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public static final String PLASMO_VOICE_ID = "plasmovoice";

    private static @Nullable PvRadioNetworking networking;
    private static @Nullable RadioAudio audio;
    private static volatile @Nullable PvRadioServices services;

    private PvRadioMod() {}

    public static void initialize(final PvRadioNetworking networking) {
        PvRadioMod.networking = networking;
        PvRadioMod.audio = RadioVoiceAddon.load();
    }

    public static Identifier id(final String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    public static void start(final MinecraftServer server) {
        services = new PvRadioServices(
            server,
            Objects.requireNonNull(audio, "initialize(..) must run before start(..)"),
            Objects.requireNonNull(networking, "initialize(..) must run before start(..)")
        );
    }

    public static void stop() {
        if (services != null) {
            try {
                services.close();
            } finally {
                services = null;
            }
        }
    }

    public static void tick() {
        services().ifPresent(PvRadioServices::tick);
    }

    public static void playerJoined(final ServerPlayer player) {
        services().ifPresent(present -> present.radio().onJoin(player));
    }

    public static void playerLeft() {
        services().ifPresent(present -> present.radio().onLeave());
    }

    public static void channelsRegistered(final ServerPlayer player) {
        services().ifPresent(present -> present.radio().sync(player));
    }

    public static <S> LiteralArgumentBuilder<S> command(final PvRadioCommandSourceAccessor<S> sources) {
        return RadioCommand.build(sources, () -> services().map(PvRadioServices::radio));
    }

    public static void handle(final ServerPlayer player, final CustomPacketPayload payload) {
        services().ifPresent(present -> present.payloads().handle(player, payload));
    }

    public static Optional<RadioService> radioService() {
        return services().map(PvRadioServices::radio);
    }

    private static Optional<PvRadioServices> services() {
        return Optional.ofNullable(services);
    }
}
