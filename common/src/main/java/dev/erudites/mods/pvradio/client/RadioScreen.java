package dev.erudites.mods.pvradio.client;

import dev.erudites.mods.pvradio.client.network.PvRadioClientNetworking;
import dev.erudites.mods.pvradio.network.RadioMutePayload;
import dev.erudites.mods.pvradio.network.RadioStatePayload;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

abstract class RadioScreen extends TranslucentScreen {

    protected static final int CONTENT_WIDTH = 260;
    protected static final int MARGIN = 20;

    private static final int ICON_SIZE = 16;
    private static final int ICON_PADDING = 2;

    protected final PvRadioClientNetworking networking;

    private final List<IconOverlay> icons = new ArrayList<>();

    private int scroll;

    protected RadioScreen(final Component title, final PvRadioClientNetworking networking) {
        super(title);
        this.networking = networking;
    }

    @Override
    public void refresh() {
        this.scroll = Mth.clamp(this.scroll, 0, this.maxScroll());
        super.refresh();
    }

    @Override
    public void extractRenderState(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        for (IconOverlay icon : this.icons) {
            RadioIcons.draw(graphics, icon.icon(), icon.x(), icon.y(), ICON_SIZE);
        }
    }

    @Override
    public boolean mouseScrolled(final double x, final double y, final double scrollX, final double scrollY) {
        if (scrollY == 0.0 || this.maxScroll() == 0) {
            return super.mouseScrolled(x, y, scrollX, scrollY);
        }
        this.scroll = Mth.clamp(this.scroll - (int) Math.signum(scrollY), 0, this.maxScroll());
        this.rebuildWidgets();
        return true;
    }

    @Override
    protected void clearWidgets() {
        super.clearWidgets();
        this.icons.clear();
    }

    protected abstract int rowCount();

    protected abstract int visibleRows();

    protected int scroll() {
        return this.scroll;
    }

    protected void micToggle(
        final RadioStatePayload.RadioView radio,
        final int x,
        final int y,
        final int width
    ) {
        this.self(radio).ifPresent(member -> this.toggle(
            RadioIcons.mic(member.micEnabled()),
            Component.translatable(member.micEnabled() ? "screen.pvradio.radio.mic.on" : "screen.pvradio.radio.mic.off"),
            _ -> this.sendMute(radio.id(), !member.micEnabled(), member.speakerEnabled()),
            x,
            y,
            width
        ));
    }

    protected void speakerToggle(
        final RadioStatePayload.RadioView radio,
        final int x,
        final int y,
        final int width
    ) {
        this.self(radio).ifPresent(member -> this.toggle(
            RadioIcons.speaker(member.speakerEnabled()),
            Component.translatable(member.speakerEnabled() ? "screen.pvradio.radio.speaker.on" : "screen.pvradio.radio.speaker.off"),
            _ -> this.sendMute(radio.id(), member.micEnabled(), !member.speakerEnabled()),
            x,
            y,
            width
        ));
    }

    protected Optional<UUID> selfId() {
        return Optional.ofNullable(this.minecraft.player).map(LocalPlayer::getUUID);
    }

    protected boolean joined(final RadioStatePayload.RadioView radio) {
        return this.selfId()
            .map(radio::contains)
            .orElse(false);
    }

    protected Optional<RadioStatePayload.RadioView> joinedRadio(final int radioId) {
        return RadioState.radio(radioId).filter(this::joined);
    }

    protected void returnToList() {
        this.minecraft.gui.setScreen(new RadioListScreen(this.networking));
    }

    protected int textY(final int rowY) {
        return rowY + (ROW_HEIGHT - this.font.lineHeight) / 2 + 1;
    }

    private void toggle(
        final Identifier icon,
        final Component label,
        final Button.OnPress onPress,
        final int x,
        final int y,
        final int width
    ) {
        boolean iconOnly = width <= SMALL_BUTTON;
        Button button = this.button(iconOnly ? Component.empty() : label, onPress, x, y, width);
        button.setTooltip(Tooltip.create(label));
        int iconX = iconOnly ? x + (width - ICON_SIZE) / 2 : x + ICON_PADDING;
        this.icons.add(new IconOverlay(icon, iconX, y + (ROW_HEIGHT - ICON_SIZE) / 2));
    }

    private Optional<RadioStatePayload.MemberView> self(final RadioStatePayload.RadioView radio) {
        return this.selfId().flatMap(radio::member);
    }

    private void sendMute(
        final int radioId,
        final boolean micEnabled,
        final boolean speakerEnabled
    ) {
        this.networking.send(new RadioMutePayload(radioId, micEnabled, speakerEnabled));
    }

    private int maxScroll() {
        return Math.max(0, this.rowCount() - this.visibleRows());
    }

    private record IconOverlay(Identifier icon, int x, int y) {}
}
