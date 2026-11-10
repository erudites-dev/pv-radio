package dev.erudites.mods.pvradio.client;

import dev.erudites.mods.pvradio.client.network.PvRadioClientNetworking;
import dev.erudites.mods.pvradio.network.RadioMemberAction;
import dev.erudites.mods.pvradio.network.RadioMemberPayload;
import dev.erudites.mods.pvradio.network.RadioStatePayload;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;

public final class RadioAddScreen extends RadioScreen {

    private static final int ADD_WIDTH = 50;

    private final Screen parent;
    private final int radioId;

    private int left;
    private int listTop;
    private int listBottom;

    public RadioAddScreen(
        final PvRadioClientNetworking networking,
        final Screen parent,
        final int radioId
    ) {
        super(Component.translatable("screen.pvradio.radio.title"), networking);
        this.parent = parent;
        this.radioId = radioId;
    }

    @Override
    public void refresh() {
        if (this.radio().isEmpty()) {
            this.returnToList();
            return;
        }
        super.refresh();
    }

    @Override
    protected void init() {
        this.left = (this.width - CONTENT_WIDTH) / 2;
        this.listTop = MARGIN + ROW_PITCH;
        this.listBottom = this.height - MARGIN - ROW_PITCH;

        List<RadioStatePayload.PlayerView> candidates = this.candidates();
        int end = Math.min(candidates.size(), this.scroll() + this.visibleRows());
        for (int index = this.scroll(); index < end; index++) {
            RadioStatePayload.PlayerView candidate = candidates.get(index);
            this.button(
                Component.translatable("screen.pvradio.radio.add.confirm"),
                _ -> this.networking.send(new RadioMemberPayload(this.radioId, candidate.uuid(), RadioMemberAction.ADD)),
                this.left + CONTENT_WIDTH - ADD_WIDTH,
                this.listTop + (index - this.scroll()) * ROW_PITCH,
                ADD_WIDTH
            );
        }

        int backWidth = CONTENT_WIDTH / 2;
        this.button(CommonComponents.GUI_BACK, _ -> this.onClose(), (this.width - backWidth) / 2, this.height - MARGIN - ROW_HEIGHT, backWidth);
    }

    @Override
    public void extractRenderState(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {
        Component header = this.radio()
            .map(radio -> Component.translatable("screen.pvradio.radio.add.title", radio.name()))
            .orElseGet(() -> Component.translatable("screen.pvradio.radio.title"));
        graphics.centeredText(this.font, header, this.width / 2, MARGIN, TITLE_COLOR);

        List<RadioStatePayload.PlayerView> candidates = this.candidates();
        if (candidates.isEmpty()) {
            graphics.centeredText(this.font, Component.translatable("screen.pvradio.radio.add.empty"), this.width / 2, this.listTop, MUTED_COLOR);
        }
        int end = Math.min(candidates.size(), this.scroll() + this.visibleRows());
        for (int index = this.scroll(); index < end; index++) {
            RadioStatePayload.PlayerView candidate = candidates.get(index);
            Component label = candidate.voiceConnected()
                ? candidate.name()
                : Component.translatable("screen.pvradio.radio.no_voice", candidate.name());
            int y = this.textY(this.listTop + (index - this.scroll()) * ROW_PITCH);
            graphics.text(this.font, label, this.left, y, candidate.voiceConnected() ? LABEL_COLOR : MUTED_COLOR);
        }
        super.extractRenderState(graphics, mouseX, mouseY, a);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    protected int rowCount() {
        return this.candidates().size();
    }

    @Override
    protected int visibleRows() {
        return Math.max(1, (this.listBottom - this.listTop) / ROW_PITCH);
    }

    private Optional<RadioStatePayload.RadioView> radio() {
        return this.joinedRadio(this.radioId);
    }

    private List<RadioStatePayload.PlayerView> candidates() {
        Optional<RadioStatePayload.RadioView> radio = this.radio();
        return radio.map(radioView -> RadioState.state()
            .map(RadioStatePayload::online)
            .orElse(List.of())
            .stream()
            .filter(player -> !radioView.contains(player.uuid()))
            .toList()
        ).orElseGet(List::of);
    }
}
