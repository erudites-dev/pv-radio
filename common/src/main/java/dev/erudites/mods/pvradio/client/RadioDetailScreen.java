package dev.erudites.mods.pvradio.client;

import dev.erudites.mods.pvradio.client.network.PvRadioClientNetworking;
import dev.erudites.mods.pvradio.network.RadioLeavePayload;
import dev.erudites.mods.pvradio.network.RadioMemberAction;
import dev.erudites.mods.pvradio.network.RadioMemberPayload;
import dev.erudites.mods.pvradio.network.RadioStatePayload;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class RadioDetailScreen extends RadioScreen {

    private static final int KICK_WIDTH = 50;
    private static final int OWNER_COLOR = 0xFFFFD700;

    private final Screen parent;
    private final int radioId;

    private int left;
    private int controlsTop;
    private int listTop;
    private int listBottom;

    public RadioDetailScreen(
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
        Optional<RadioStatePayload.RadioView> found = this.radio();
        if (found.isEmpty()) {
            return;
        }
        RadioStatePayload.RadioView radio = found.get();
        this.layout();
        int halfWidth = (CONTENT_WIDTH - GAP) / 2;

        int y = this.controlsTop;
        this.micToggle(radio, this.left, y, halfWidth);
        this.speakerToggle(radio, this.left + halfWidth + GAP, y, halfWidth);
        if (RadioVolumes.available()) {
            y += ROW_PITCH;
            this.addRenderableWidget(new VolumeSlider(this.left, y, CONTENT_WIDTH, this.radioId));
        }

        Optional<UUID> selfId = this.selfId();
        List<RadioStatePayload.MemberView> members = radio.members();
        int end = Math.min(members.size(), this.scroll() + this.visibleRows());
        for (int index = this.scroll(); index < end; index++) {
            RadioStatePayload.MemberView member = members.get(index);
            boolean selfRow = selfId.map(member.uuid()::equals).orElse(false);
            if (radio.canManage() && !selfRow) {
                this.button(
                    Component.translatable("screen.pvradio.radio.kick"),
                    _ -> this.networking.send(new RadioMemberPayload(this.radioId, member.uuid(), RadioMemberAction.KICK)),
                    this.left + CONTENT_WIDTH - KICK_WIDTH,
                    this.listTop + (index - this.scroll()) * ROW_PITCH,
                    KICK_WIDTH
                );
            }
        }

        int bottomY = this.height - MARGIN - ROW_HEIGHT;
        int thirdWidth = (CONTENT_WIDTH - GAP * 2) / 3;
        this.button(
            Component.translatable("screen.pvradio.radio.add"),
            _ -> this.minecraft.setScreen(new RadioAddScreen(this.networking, this, this.radioId)),
            this.left,
            bottomY,
            thirdWidth
        );
        this.button(
            Component.translatable("screen.pvradio.radio.leave"),
            _ -> this.confirmLeave(radio),
            this.left + thirdWidth + GAP,
            bottomY,
            thirdWidth
        );
        this.button(CommonComponents.GUI_BACK, _ -> this.onClose(), this.left + (thirdWidth + GAP) * 2, bottomY, thirdWidth);
    }

    @Override
    public void extractRenderState(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {
        Optional<RadioStatePayload.RadioView> found = this.radio();
        if (found.isPresent()) {
            RadioStatePayload.RadioView radio = found.get();
            graphics.centeredText(this.font, Component.literal(radio.name()), this.width / 2, MARGIN, TITLE_COLOR);
            List<RadioStatePayload.MemberView> members = radio.members();
            int end = Math.min(members.size(), this.scroll() + this.visibleRows());
            for (int index = this.scroll(); index < end; index++) {
                RadioStatePayload.MemberView member = members.get(index);
                int y = this.textY(this.listTop + (index - this.scroll()) * ROW_PITCH);
                graphics.text(this.font, this.memberLabel(radio, member), this.left, y, member.online() ? LABEL_COLOR : MUTED_COLOR);
            }
        }
        super.extractRenderState(graphics, mouseX, mouseY, a);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    protected int rowCount() {
        return this.radio()
            .map(radio -> radio.members().size())
            .orElse(0);
    }

    @Override
    protected int visibleRows() {
        return Math.max(1, (this.listBottom - this.listTop) / ROW_PITCH);
    }

    private Optional<RadioStatePayload.RadioView> radio() {
        return this.joinedRadio(this.radioId);
    }

    private void layout() {
        this.left = (this.width - CONTENT_WIDTH) / 2;
        this.controlsTop = MARGIN + ROW_PITCH;
        int controlRows = RadioVolumes.available() ? 2 : 1;
        this.listTop = this.controlsTop + ROW_PITCH * controlRows + GAP;
        this.listBottom = this.height - MARGIN - ROW_PITCH;
    }

    private Component memberLabel(final RadioStatePayload.RadioView radio, final RadioStatePayload.MemberView member) {
        Component name = member.online()
            ? member.name()
            : Component.translatable("screen.pvradio.radio.offline", member.name());
        if (!member.uuid().equals(radio.owner())) {
            return name;
        }
        MutableComponent owner = Component.translatable("screen.pvradio.radio.owner", name);
        return member.online() ? owner.withStyle(style -> style.withColor(OWNER_COLOR)) : owner;
    }

    private void confirmLeave(final RadioStatePayload.RadioView radio) {
        this.minecraft.setScreen(new ConfirmScreen(
            confirmed -> {
                if (confirmed) {
                    this.networking.send(new RadioLeavePayload(this.radioId));
                    this.minecraft.setScreen(this.parent);
                } else if (this.radio().isPresent()) {
                    this.minecraft.setScreen(this);
                } else {
                    this.returnToList();
                }
            },
            Component.translatable("screen.pvradio.radio.leave.title", radio.name()),
            CommonComponents.EMPTY
        ));
    }

    private static final class VolumeSlider extends AbstractSliderButton {

        private final int radioId;

        private VolumeSlider(
            final int x,
            final int y,
            final int width,
            final int radioId
        ) {
            super(x, y, width, ROW_HEIGHT, Component.empty(), RadioVolumes.volume(radioId) / RadioVolumes.MAX_VOLUME);
            this.radioId = radioId;
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            long percent = Math.round(this.value * RadioVolumes.MAX_VOLUME * 100.0);
            this.setMessage(Component.translatable("screen.pvradio.radio.volume", percent));
        }

        @Override
        protected void applyValue() {
            RadioVolumes.volume(this.radioId, this.value * RadioVolumes.MAX_VOLUME);
        }
    }
}
