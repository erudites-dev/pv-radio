package dev.erudites.mods.pvradio.client;

import dev.erudites.mods.pvradio.client.network.PvRadioClientNetworking;
import dev.erudites.mods.pvradio.network.RadioJoinPayload;
import dev.erudites.mods.pvradio.network.RadioStatePayload;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.Comparator;
import java.util.List;

public final class RadioListScreen extends RadioScreen {

    private static final int JOIN_WIDTH = 50;

    private int left;
    private int listTop;
    private int listBottom;

    public RadioListScreen(final PvRadioClientNetworking networking) {
        super(Component.translatable("screen.pvradio.radio.title"), networking);
    }

    @Override
    protected void init() {
        this.layout();
        List<RadioStatePayload.RadioView> radios = this.radios();
        int right = this.left + CONTENT_WIDTH;
        int end = Math.min(radios.size(), this.scroll() + this.visibleRows());
        for (int index = this.scroll(); index < end; index++) {
            RadioStatePayload.RadioView radio = radios.get(index);
            int y = this.listTop + (index - this.scroll()) * ROW_PITCH;
            if (!this.joined(radio)) {
                this.button(
                    Component.translatable("screen.pvradio.radio.join"),
                    _ -> this.networking.send(new RadioJoinPayload(radio.id())),
                    right - JOIN_WIDTH,
                    y,
                    JOIN_WIDTH
                );
                continue;
            }
            this.button(
                Component.literal(">"),
                _ -> this.minecraft.setScreen(new RadioDetailScreen(this.networking, this, radio.id())),
                right - SMALL_BUTTON,
                y,
                SMALL_BUTTON
            );
            int speakerX = right - SMALL_BUTTON - GAP - SMALL_BUTTON;
            this.speakerToggle(radio, speakerX, y, SMALL_BUTTON);
            this.micToggle(radio, speakerX - GAP - SMALL_BUTTON, y, SMALL_BUTTON);
        }

        int bottomY = this.height - MARGIN - ROW_HEIGHT;
        int halfWidth = (CONTENT_WIDTH - GAP) / 2;
        this.button(
            Component.translatable("screen.pvradio.radio.create"),
            _ -> this.minecraft.setScreen(new RadioCreateScreen(this.networking, this)),
            this.left,
            bottomY,
            halfWidth
        );
        this.button(CommonComponents.GUI_DONE, _ -> this.onClose(), this.left + halfWidth + GAP, bottomY, halfWidth);
    }

    @Override
    public void extractRenderState(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {
        graphics.centeredText(this.font, this.title, this.width / 2, MARGIN, TITLE_COLOR);
        List<RadioStatePayload.RadioView> radios = this.radios();
        if (radios.isEmpty()) {
            graphics.centeredText(this.font, Component.translatable("screen.pvradio.radio.empty"), this.width / 2, this.listTop, MUTED_COLOR);
        }
        int end = Math.min(radios.size(), this.scroll() + this.visibleRows());
        for (int index = this.scroll(); index < end; index++) {
            RadioStatePayload.RadioView radio = radios.get(index);
            int y = this.listTop + (index - this.scroll()) * ROW_PITCH;
            long online = radio.members().stream()
                .filter(RadioStatePayload.MemberView::online)
                .count();
            Component label = Component.translatable("screen.pvradio.radio.entry", radio.name(), online, radio.members().size());
            graphics.text(this.font, label, this.left, this.textY(y), this.joined(radio) ? LABEL_COLOR : MUTED_COLOR);
        }
        super.extractRenderState(graphics, mouseX, mouseY, a);
    }

    @Override
    protected int rowCount() {
        return this.radios().size();
    }

    @Override
    protected int visibleRows() {
        return Math.max(1, (this.listBottom - this.listTop) / ROW_PITCH);
    }

    private void layout() {
        this.left = (this.width - CONTENT_WIDTH) / 2;
        this.listTop = MARGIN + ROW_PITCH;
        this.listBottom = this.height - MARGIN - ROW_PITCH;
    }

    private List<RadioStatePayload.RadioView> radios() {
        return RadioState.state()
            .map(RadioStatePayload::radios)
            .orElse(List.of())
            .stream()
            .sorted(Comparator.comparing((RadioStatePayload.RadioView radio) -> !this.joined(radio)))
            .toList();
    }
}
