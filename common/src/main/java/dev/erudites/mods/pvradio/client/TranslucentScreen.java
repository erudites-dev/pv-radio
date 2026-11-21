package dev.erudites.mods.pvradio.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public abstract class TranslucentScreen extends Screen {

    protected static final int ROW_HEIGHT = 20;
    protected static final int GAP = 4;
    protected static final int ROW_PITCH = ROW_HEIGHT + GAP;
    protected static final int SMALL_BUTTON = 20;
    protected static final int TITLE_COLOR = 0xFFFFFFFF;
    protected static final int LABEL_COLOR = 0xFFFFFFFF;
    protected static final int MUTED_COLOR = 0xFFAAAAAA;

    private static final int BACKGROUND = 0x90000000;

    protected TranslucentScreen(final Component title) {
        super(title);
    }

    public void refresh() {
        this.rebuildWidgets();
    }

    @Override
    public void extractBackground(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {
        graphics.fill(0, 0, this.width, this.height, BACKGROUND);
        this.minecraft.gui.hud.extractDeferredSubtitles();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    protected Button button(
        final Component label,
        final Button.OnPress onPress,
        final int x,
        final int y,
        final int width
    ) {
        return this.addRenderableWidget(Button.builder(label, onPress).bounds(x, y, width, ROW_HEIGHT).build());
    }
}
