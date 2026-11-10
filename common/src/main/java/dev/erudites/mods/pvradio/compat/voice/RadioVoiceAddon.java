package dev.erudites.mods.pvradio.compat.voice;

import dev.erudites.mods.pvradio.PvRadioMod;
import dev.erudites.mods.pvradio.radio.Radio;
import dev.erudites.mods.pvradio.radio.RadioAudio;
import dev.erudites.mods.pvradio.radio.RadioRoute;
import org.jspecify.annotations.Nullable;
import su.plo.slib.api.permission.PermissionDefault;
import su.plo.voice.api.addon.AddonInitializer;
import su.plo.voice.api.addon.AddonLoaderScope;
import su.plo.voice.api.addon.InjectPlasmoVoice;
import su.plo.voice.api.addon.annotation.Addon;
import su.plo.voice.api.event.EventPriority;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.audio.capture.PlayerActivationInfo;
import su.plo.voice.api.server.audio.capture.ServerActivation;
import su.plo.voice.api.server.audio.line.ServerSourceLine;
import su.plo.voice.api.server.audio.source.ServerBroadcastSource;
import su.plo.voice.api.server.event.connection.UdpClientConnectedEvent;
import su.plo.voice.api.server.event.connection.UdpClientDisconnectedEvent;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.api.server.player.VoiceServerPlayer;
import su.plo.voice.proto.packets.tcp.clientbound.SourceLineRegisterPacket;
import su.plo.voice.proto.packets.tcp.serverbound.PlayerAudioEndPacket;
import su.plo.voice.proto.packets.udp.serverbound.PlayerAudioPacket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Addon(
    id = "pvradio",
    name = "PV Radio",
    scope = AddonLoaderScope.SERVER,
    version = "1.0.0",
    authors = "shihyeon"
)
public final class RadioVoiceAddon implements AddonInitializer, RadioAudio {

    private static final String ACTIVATION_NAME = "radio";
    private static final String ACTIVATION_TRANSLATION = "pv.activation.radio";
    private static final String ACTIVATION_PERMISSION = "pv.activation.radio";
    private static final String ACTIVATION_ICON = "plasmovoice:textures/icons/microphone_radio.png";
    private static final String LINE_ICON = "plasmovoice:textures/icons/speaker_radio.png";
    private static final int WEIGHT = 10;
    private static final long TALKING_TIMEOUT_NANOS = TimeUnit.MILLISECONDS.toNanos(500);

    private final Map<Integer, ServerSourceLine> lines = new HashMap<>();
    private final Map<SourceKey, ServerBroadcastSource> sources = new HashMap<>();
    private final Map<UUID, Long> lastPackets = new ConcurrentHashMap<>();

    @InjectPlasmoVoice
    private @Nullable PlasmoVoiceServer voiceServer;
    private volatile @Nullable ServerActivation activation;
    private volatile @Nullable Runnable onChange;
    private volatile Map<UUID, List<ServerBroadcastSource>> sourcesBySender = Map.of();
    private volatile Set<UUID> transmitters = Set.of();

    private RadioVoiceAddon() {}

    public static RadioAudio load() {
        RadioVoiceAddon addon = new RadioVoiceAddon();
        PlasmoVoiceServer.getAddonsLoader().load(addon);
        return addon;
    }

    @Override
    public void onAddonInitialize() {
        PlasmoVoiceServer voice = this.voiceServer;
        if (voice == null) {
            PvRadioMod.LOGGER.warn("Plasmo Voice server was not injected, radio is disabled");
            return;
        }
        ServerActivation created = voice.getActivationManager()
            .createBuilder(this, ACTIVATION_NAME, ACTIVATION_TRANSLATION, ACTIVATION_ICON, ACTIVATION_PERMISSION, WEIGHT)
            .setProximity(false)
            .setTransitive(true)
            .setStereoSupported(false)
            .setPermissionDefault(PermissionDefault.TRUE)
            .build();
        created.onPlayerActivation(this::onActivation);
        created.onPlayerActivationEnd(this::onActivationEnd);
        voice.getEventBus().register(this, UdpClientConnectedEvent.class, EventPriority.NORMAL, _ -> this.changed());
        voice.getEventBus().register(this, UdpClientDisconnectedEvent.class, EventPriority.NORMAL, event -> {
            this.lastPackets.remove(event.getConnection().getPlayer().getInstance().getUuid());
            this.changed();
        });
        this.activation = created;
        this.changed();
    }

    @Override
    public void onAddonShutdown() {
        PlasmoVoiceServer voice = this.voiceServer;
        ServerActivation current = this.activation;
        this.activation = null;
        this.sourcesBySender = Map.of();
        this.transmitters = Set.of();
        this.lastPackets.clear();
        if (voice != null) {
            voice.getEventBus().unregister(this);
            if (current != null) {
                voice.getActivationManager().unregister(current.getId());
            }
            for (ServerSourceLine line : this.lines.values()) {
                voice.getSourceLineManager().unregister(line.getId());
            }
        }
        this.sources.clear();
        this.lines.clear();
        this.changed();
    }

    @Override
    public boolean available() {
        return this.activation != null;
    }

    @Override
    public boolean voiceConnected(final UUID player) {
        return this.voicePlayer(player)
            .map(VoicePlayer::hasVoiceChat)
            .orElse(false);
    }

    @Override
    public Set<UUID> talking() {
        long now = System.nanoTime();
        this.lastPackets.values().removeIf(time -> now - time > TALKING_TIMEOUT_NANOS);
        return Set.copyOf(this.lastPackets.keySet());
    }

    @Override
    public void attach(final Runnable onChange) {
        this.onChange = onChange;
    }

    @Override
    public void detach() {
        this.onChange = null;
    }

    @Override
    public void lines(final Map<Integer, String> names) {
        PlasmoVoiceServer voice = this.voiceServer;
        if (voice == null || this.activation == null) {
            return;
        }
        Iterator<Map.Entry<Integer, ServerSourceLine>> iterator = this.lines.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, ServerSourceLine> entry = iterator.next();
            if (!names.containsKey(entry.getKey())) {
                this.removeSources(entry.getKey());
                voice.getSourceLineManager().unregister(entry.getValue().getId());
                iterator.remove();
            }
        }
        names.forEach((id, name) -> {
            if (!this.lines.containsKey(id)) {
                ServerSourceLine line = voice.getSourceLineManager()
                    .createBuilder(this, Radio.lineName(id), name, LINE_ICON, WEIGHT)
                    .withPlayers(false)
                    .build();
                this.lines.put(id, line);
                announce(voice, line);
            }
        });
    }

    @Override
    public void routes(final List<RadioRoute> routes) {
        if (this.voiceServer == null || this.activation == null) {
            return;
        }
        Map<SourceKey, ServerBroadcastSource> next = new HashMap<>();
        Map<UUID, List<ServerBroadcastSource>> bySender = new HashMap<>();
        Set<UUID> senders = new HashSet<>();
        for (RadioRoute route : routes) {
            ServerSourceLine line = this.lines.get(route.radio());
            Optional<VoiceServerPlayer> sender = this.voicePlayer(route.sender());
            if (line == null || sender.isEmpty()) {
                continue;
            }
            senders.add(route.sender());
            List<VoicePlayer> listeners = new ArrayList<>();
            for (UUID listener : route.listeners()) {
                this.voicePlayer(listener).ifPresent(listeners::add);
            }
            if (listeners.isEmpty()) {
                continue;
            }
            SourceKey key = new SourceKey(route.sender(), route.radio());
            ServerBroadcastSource source = this.sources.remove(key);
            if (source == null) {
                source = line.createBroadcastSource(false);
            }
            source.setSender(sender.get());
            source.setPlayers(listeners);
            next.put(key, source);
            bySender.computeIfAbsent(route.sender(), _ -> new ArrayList<>()).add(source);
        }
        for (ServerBroadcastSource stale : this.sources.values()) {
            stale.remove();
        }
        this.sources.clear();
        this.sources.putAll(next);

        Map<UUID, List<ServerBroadcastSource>> snapshot = HashMap.newHashMap(bySender.size());
        bySender.forEach((sender, list) -> snapshot.put(sender, List.copyOf(list)));
        this.sourcesBySender = Map.copyOf(snapshot);
        this.transmitters = Set.copyOf(senders);
    }

    private ServerActivation.Result onActivation(final VoicePlayer player, final PlayerAudioPacket packet) {
        UUID uuid = player.getInstance().getUuid();
        if (!this.transmitters.contains(uuid)) {
            return ServerActivation.Result.IGNORED;
        }
        this.lastPackets.put(uuid, System.nanoTime());
        boolean sent = false;
        for (ServerBroadcastSource source : this.sourcesBySender.getOrDefault(uuid, List.of())) {
            sent |= source.sendAudioFrame(packet.getData(), packet.getSequenceNumber(), new PlayerActivationInfo(player, packet));
        }
        return sent ? ServerActivation.Result.HANDLED : ServerActivation.Result.IGNORED;
    }

    private ServerActivation.Result onActivationEnd(final VoicePlayer player, final PlayerAudioEndPacket packet) {
        UUID uuid = player.getInstance().getUuid();
        this.lastPackets.remove(uuid);
        boolean sent = false;
        for (ServerBroadcastSource source : this.sourcesBySender.getOrDefault(uuid, List.of())) {
            sent |= source.sendAudioEnd(packet.getSequenceNumber());
        }
        return sent ? ServerActivation.Result.HANDLED : ServerActivation.Result.IGNORED;
    }

    private static void announce(final PlasmoVoiceServer voice, final ServerSourceLine line) {
        for (VoiceServerPlayer player : voice.getPlayerManager().getPlayers()) {
            if (player.hasVoiceChat()) {
                player.sendPacket(new SourceLineRegisterPacket(line.getSourceLineForPlayer(player)));
            }
        }
    }

    private Optional<VoiceServerPlayer> voicePlayer(final UUID uuid) {
        PlasmoVoiceServer voice = this.voiceServer;
        if (voice == null) {
            return Optional.empty();
        }
        return voice.getPlayerManager().getPlayerById(uuid);
    }

    private void removeSources(final int radioId) {
        Iterator<Map.Entry<SourceKey, ServerBroadcastSource>> iterator = this.sources.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<SourceKey, ServerBroadcastSource> entry = iterator.next();
            if (entry.getKey().radio() == radioId) {
                entry.getValue().remove();
                iterator.remove();
            }
        }
    }

    private void changed() {
        Runnable callback = this.onChange;
        if (callback != null) {
            callback.run();
        }
    }

    private record SourceKey(UUID sender, int radio) {}
}
