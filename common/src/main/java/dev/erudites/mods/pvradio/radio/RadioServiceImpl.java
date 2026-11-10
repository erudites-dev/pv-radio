package dev.erudites.mods.pvradio.radio;

import dev.erudites.mods.pvradio.api.RadioInfo;
import dev.erudites.mods.pvradio.api.RadioService;
import dev.erudites.mods.pvradio.command.RadioCommand;
import dev.erudites.mods.pvradio.network.PvRadioNetworking;
import dev.erudites.mods.pvradio.network.RadioStatePayload;
import dev.erudites.mods.pvradio.network.RadioTalkingPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public final class RadioServiceImpl implements RadioService {

    private final MinecraftServer server;
    private final RadioAudio audio;
    private final PvRadioNetworking networking;
    private final RadioData data;
    private final AtomicBoolean dirty = new AtomicBoolean(true);

    private volatile RadioBook book;
    private volatile Set<UUID> talking = Set.of();
    private volatile boolean closed;

    public RadioServiceImpl(
        final MinecraftServer server,
        final RadioAudio audio,
        final PvRadioNetworking networking
    ) {
        this.server = server;
        this.audio = audio;
        this.networking = networking;
        this.data = server.getDataStorage().computeIfAbsent(RadioData.TYPE);
        this.book = this.data.book();
        audio.attach(() -> this.dirty.set(true));
    }

    public void close() {
        this.closed = true;
        this.audio.detach();
    }

    public void tick() {
        if (this.dirty.getAndSet(false)) {
            this.audio.lines(this.lineNames());
            this.audio.routes(RadioRoutes.of(this.book.radios(), this::reachable));
            this.syncAll();
        }
        this.updateTalking();
    }

    public void onJoin(final ServerPlayer player) {
        String name = player.getDisplayName().getString();
        RadioBook book = this.book;
        for (Radio radio : book.joined(player.getUUID())) {
            book = book.withRadio(radio.withMember(player.getUUID(), member -> member.withName(name)));
        }
        this.commit(book);
    }

    public void onLeave() {
        this.dirty.set(true);
    }

    public void sync(final ServerPlayer player) {
        this.networking.send(player, this.state(player));
    }

    @Override
    public boolean voiceAvailable() {
        return this.audio.available();
    }

    @Override
    public boolean voiceConnected(final UUID player) {
        return this.audio.voiceConnected(player);
    }

    @Override
    public List<RadioInfo> radios() {
        return this.book.radios().stream().map(Radio::info).toList();
    }

    @Override
    public Optional<RadioInfo> radio(final int id) {
        return this.book.radio(id).map(Radio::info);
    }

    @Override
    public List<RadioInfo> joinedRadios(final UUID player) {
        return this.book.joined(player).stream().map(Radio::info).toList();
    }

    @Override
    public boolean talking(final UUID player) {
        return this.talking.contains(player);
    }

    @Override
    public Optional<RadioInfo> createRadio(final String name, final UUID owner) {
        this.requireServerThread();
        ServerPlayer player = this.server.getPlayerList().getPlayer(owner);
        if (player == null) {
            return Optional.empty();
        }
        return this.newRadio(player, name).map(Radio::info);
    }

    @Override
    public boolean addMember(final int radioId, final UUID player) {
        this.requireServerThread();
        ServerPlayer online = this.server.getPlayerList().getPlayer(player);
        return online != null && this.insert(radioId, online).isPresent();
    }

    @Override
    public boolean removeMember(final int radioId, final UUID player) {
        this.requireServerThread();
        Optional<Radio> found = this.memberRadio(radioId, player);
        found.ifPresent(radio -> this.detach(radio, player));
        return found.isPresent();
    }

    @Override
    public boolean setOwner(final int radioId, final UUID player) {
        this.requireServerThread();
        Optional<Radio> found = this.memberRadio(radioId, player);
        if (found.isPresent() && !found.get().owner().equals(player)) {
            this.commit(this.book.withRadio(found.get().withOwner(player)));
        }
        return found.isPresent();
    }

    @Override
    public boolean setAudio(final int radioId, final UUID player, final boolean micEnabled, final boolean speakerEnabled) {
        this.requireServerThread();
        Optional<Radio> found = this.memberRadio(radioId, player);
        found.ifPresent(radio -> this.updateAudio(radio, player, micEnabled, speakerEnabled));
        return found.isPresent();
    }

    @Override
    public boolean deleteRadio(final int radioId) {
        this.requireServerThread();
        if (this.book.radio(radioId).isEmpty()) {
            return false;
        }
        this.commit(this.book.withoutRadio(radioId));
        return true;
    }

    public void create(final ServerPlayer player, final String requested) {
        if (!this.audio.available()) {
            return;
        }
        Optional<Radio> radio = this.newRadio(player, requested);
        if (radio.isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.pvradio.radio.invalid_name", Radio.MAX_NAME_LENGTH));
            return;
        }
        player.sendSystemMessage(Component.translatable("message.pvradio.radio.created", radioName(radio.get())));
    }

    public void join(final ServerPlayer player, final int radioId) {
        Optional<Radio> joined = this.insert(radioId, player);
        if (joined.isEmpty()) {
            return;
        }
        Radio radio = joined.get();
        player.sendSystemMessage(Component.translatable("message.pvradio.radio.joined_you", radioName(radio), leaveButton(radio)));
        this.notifyMembers(
            radio,
            Component.translatable("message.pvradio.radio.joined", player.getDisplayName(), radioName(radio)),
            player.getUUID()
        );
    }

    public void add(final ServerPlayer actor, final int radioId, final UUID targetId) {
        ServerPlayer target = this.server.getPlayerList().getPlayer(targetId);
        if (this.joinedRadio(actor, radioId).isEmpty() || target == null) {
            return;
        }
        Optional<Radio> added = this.insert(radioId, target);
        if (added.isEmpty()) {
            return;
        }
        Radio radio = added.get();
        target.sendSystemMessage(Component.translatable(
            "message.pvradio.radio.added_you",
            actor.getDisplayName(),
            radioName(radio),
            leaveButton(radio)
        ));
        this.notifyMembers(
            radio,
            Component.translatable("message.pvradio.radio.added", actor.getDisplayName(), target.getDisplayName(), radioName(radio)),
            targetId
        );
    }

    public void kick(final ServerPlayer actor, final int radioId, final UUID targetId) {
        Optional<Radio> found = this.joinedRadio(actor, radioId);
        if (found.isEmpty() || !this.canManage(actor, found.get()) || targetId.equals(actor.getUUID())) {
            return;
        }
        Optional<RadioMember> member = found.get().member(targetId);
        if (member.isEmpty()) {
            return;
        }
        Component name = this.displayName(member.get());
        Optional<Radio> remaining = this.detach(found.get(), targetId);
        this.announceOwner(found.get(), remaining);

        ServerPlayer target = this.server.getPlayerList().getPlayer(targetId);
        if (target != null) {
            target.sendSystemMessage(Component.translatable("message.pvradio.radio.kicked_you", radioName(found.get())));
        }
        remaining.ifPresent(
            radio -> this.notifyMembers(radio, Component.translatable("message.pvradio.radio.kicked", name, radioName(radio)), null)
        );
    }

    public boolean leave(final ServerPlayer player, final int radioId) {
        Optional<Radio> found = this.joinedRadio(player, radioId);
        if (found.isEmpty()) {
            return false;
        }
        Optional<Radio> remaining = this.detach(found.get(), player.getUUID());
        this.announceOwner(found.get(), remaining);
        player.sendSystemMessage(Component.translatable("message.pvradio.radio.left_you", radioName(found.get())));
        remaining.ifPresent(radio -> this.notifyMembers(
            radio,
            Component.translatable("message.pvradio.radio.left", player.getDisplayName(), radioName(radio)),
            null
        ));
        return true;
    }

    public void mute(
        final ServerPlayer player,
        final int radioId,
        final boolean micEnabled,
        final boolean speakerEnabled
    ) {
        this.joinedRadio(player, radioId)
            .ifPresent(radio -> this.updateAudio(radio, player.getUUID(), micEnabled, speakerEnabled));
    }

    private Optional<Radio> joinedRadio(final ServerPlayer player, final int radioId) {
        return this.memberRadio(radioId, player.getUUID());
    }

    private Optional<Radio> memberRadio(final int radioId, final UUID player) {
        return this.book.radio(radioId).filter(radio -> radio.contains(player));
    }

    private boolean canManage(final ServerPlayer player, final Radio radio) {
        return radio.owner().equals(player.getUUID()) || this.server.getPlayerList().isOp(player.nameAndId());
    }

    private Optional<Radio> newRadio(final ServerPlayer owner, final String requested) {
        String name = requested.strip();
        if (name.isEmpty() || name.length() > Radio.MAX_NAME_LENGTH) {
            return Optional.empty();
        }
        Radio radio = new Radio(
            this.book.nextId(),
            name,
            owner.getUUID(),
            List.of(RadioMember.joined(owner.getUUID(), owner.getDisplayName().getString()))
        );
        this.commit(this.book.withCreated(radio));
        return Optional.of(radio);
    }

    private Optional<Radio> insert(final int radioId, final ServerPlayer player) {
        Optional<Radio> found = this.book.radio(radioId);
        if (found.isEmpty() || found.get().contains(player.getUUID())) {
            return Optional.empty();
        }
        Radio radio = found.get().withMember(RadioMember.joined(player.getUUID(), player.getDisplayName().getString()));
        this.commit(this.book.withRadio(radio));
        return Optional.of(radio);
    }

    private void updateAudio(final Radio radio, final UUID player, final boolean micEnabled, final boolean speakerEnabled) {
        this.commit(this.book.withRadio(radio.withMember(player, member -> member.withAudio(micEnabled, speakerEnabled))));
    }

    private Optional<Radio> detach(final Radio radio, final UUID uuid) {
        Radio remaining = radio.withoutMember(uuid);
        if (remaining.members().isEmpty()) {
            this.commit(this.book.withoutRadio(radio.id()));
            return Optional.empty();
        }
        if (remaining.owner().equals(uuid)) {
            RadioMember owner = remaining.members().stream()
                .filter(member -> this.online(member.uuid()))
                .findFirst()
                .orElse(remaining.members().getFirst());
            remaining = remaining.withOwner(owner.uuid());
        }
        this.commit(this.book.withRadio(remaining));
        return Optional.of(remaining);
    }

    private void announceOwner(final Radio before, final Optional<Radio> after) {
        after.filter(radio -> !radio.owner().equals(before.owner()))
            .ifPresent(radio -> radio.member(radio.owner()).ifPresent(owner -> this.notifyMembers(
                radio,
                Component.translatable("message.pvradio.radio.new_owner", this.displayName(owner), radioName(radio)),
                null
            )));
    }

    private void commit(final RadioBook book) {
        RadioBook previous = this.book;
        this.book = book;
        this.data.book(book);
        this.server.getDataStorage().scheduleSave();
        this.dirty.set(true);
        RadioListeners.changed(previous, book);
    }

    private void requireServerThread() {
        if (this.closed) {
            throw new IllegalStateException("PV Radio API used after the server stopped");
        }
        if (!this.server.isSameThread()) {
            throw new IllegalStateException("PV Radio API changes must be made on the server thread");
        }
    }

    private void notifyMembers(final Radio radio, final Component message, final @Nullable UUID except) {
        for (RadioMember member : radio.members()) {
            ServerPlayer player = this.server.getPlayerList().getPlayer(member.uuid());
            if (player != null && !member.uuid().equals(except)) {
                player.sendSystemMessage(message);
            }
        }
    }

    private Map<Integer, String> lineNames() {
        Map<Integer, String> names = new LinkedHashMap<>();
        for (Radio radio : this.book.radios()) {
            names.put(radio.id(), radio.name());
        }
        return names;
    }

    private boolean reachable(final UUID uuid) {
        return this.online(uuid) && this.audio.voiceConnected(uuid);
    }

    private boolean online(final UUID uuid) {
        return this.server.getPlayerList().getPlayer(uuid) != null;
    }

    private void syncAll() {
        for (ServerPlayer player : this.server.getPlayerList().getPlayers()) {
            this.sync(player);
        }
    }

    private RadioStatePayload state(final ServerPlayer player) {
        if (!this.audio.available()) {
            return RadioStatePayload.UNAVAILABLE;
        }
        List<RadioStatePayload.RadioView> radios = this.book.radios().stream()
            .map(radio -> new RadioStatePayload.RadioView(
                radio.id(),
                radio.name(),
                radio.owner(),
                radio.contains(player.getUUID()) && this.canManage(player, radio),
                radio.members().stream()
                    .map(member -> new RadioStatePayload.MemberView(
                        member.uuid(),
                        this.displayName(member),
                        this.online(member.uuid()),
                        member.micEnabled(),
                        member.speakerEnabled()
                    ))
                    .toList()
            ))
            .toList();
        List<RadioStatePayload.PlayerView> online = this.server.getPlayerList().getPlayers().stream()
            .map(other -> new RadioStatePayload.PlayerView(
                other.getUUID(),
                other.getDisplayName(),
                this.audio.voiceConnected(other.getUUID())
            ))
            .toList();
        return new RadioStatePayload(true, radios, online);
    }

    private void updateTalking() {
        Set<UUID> now = this.audio.talking();
        if (now.equals(this.talking)) {
            return;
        }
        Set<UUID> previous = this.talking;
        this.talking = Set.copyOf(now);
        Set<UUID> changed = new HashSet<>(now);
        changed.addAll(previous);
        for (UUID uuid : changed) {
            boolean talking = now.contains(uuid);
            if (talking != previous.contains(uuid)) {
                this.sendTalking(uuid, talking);
                RadioListeners.talking(uuid, talking);
            }
        }
    }

    private void sendTalking(final UUID speaker, final boolean talking) {
        Set<UUID> receivers = new HashSet<>();
        for (Radio radio : this.book.joined(speaker)) {
            for (RadioMember member : radio.members()) {
                receivers.add(member.uuid());
            }
        }
        RadioTalkingPayload payload = new RadioTalkingPayload(speaker, talking);
        for (UUID receiver : receivers) {
            ServerPlayer player = this.server.getPlayerList().getPlayer(receiver);
            if (player != null) {
                this.networking.send(player, payload);
            }
        }
    }

    private Component displayName(final RadioMember member) {
        ServerPlayer player = this.server.getPlayerList().getPlayer(member.uuid());
        return player != null ? player.getDisplayName() : Component.literal(member.name());
    }

    private static Component radioName(final Radio radio) {
        return Component.literal(radio.name());
    }

    private static Component leaveButton(final Radio radio) {
        MutableComponent button = Component.translatable("message.pvradio.radio.leave_button");
        return button.withStyle(style -> style
            .withColor(ChatFormatting.RED)
            .withClickEvent(new ClickEvent.RunCommand(RadioCommand.leaveCommand(radio.id())))
        );
    }
}
