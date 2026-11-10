package dev.erudites.mods.pvradio.client;

import dev.erudites.mods.pvradio.client.network.PvRadioClientNetworking;
import dev.erudites.mods.pvradio.network.RadioCreatePayload;
import dev.erudites.mods.pvradio.radio.Radio;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public final class RadioCreateScreen extends TranslucentScreen {

    private static final int CONTENT_WIDTH = 200;
    private static final int CONTENT_HEIGHT = 20 + ROW_PITCH * 2 - GAP;

    private final PvRadioClientNetworking networking;
    private final Screen parent;

    private @Nullable EditBox name;
    private int left;
    private int top;

    public RadioCreateScreen(final PvRadioClientNetworking networking, final Screen parent) {
        super(Component.translatable("screen.pvradio.radio.create.title"));
        this.networking = networking;
        this.parent = parent;
    }

    @Override
    public void refresh() {}

    @Override
    protected void init() {
        this.left = (this.width - CONTENT_WIDTH) / 2;
        this.top = (this.height - CONTENT_HEIGHT) / 2;
        int y = this.top + 20;
        EditBox box = new EditBox(this.font, this.left, y, CONTENT_WIDTH, ROW_HEIGHT, this.name, this.title);
        box.setMaxLength(Radio.MAX_NAME_LENGTH);
        box.setHint(Component.translatable("screen.pvradio.radio.create.hint", Radio.MAX_NAME_LENGTH));
        this.name = this.addRenderableWidget(box);
        this.setInitialFocus(box);

        y += ROW_PITCH;
        int halfWidth = (CONTENT_WIDTH - GAP) / 2;
        this.button(Component.translatable("screen.pvradio.radio.create.confirm"), _ -> this.confirm(), this.left, y, halfWidth);
        this.button(CommonComponents.GUI_CANCEL, _ -> this.onClose(), this.left + halfWidth + GAP, y, halfWidth);
    }

    @Override
    public void extractRenderState(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {
        graphics.centeredText(this.font, this.title, this.width / 2, this.top, TITLE_COLOR);
        super.extractRenderState(graphics, mouseX, mouseY, a);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    private void confirm() {
        if (this.name != null && !this.name.getValue().isBlank()) {
            this.networking.send(new RadioCreatePayload(this.name.getValue()));
        }
        this.onClose();
    }
}
