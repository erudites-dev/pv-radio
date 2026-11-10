package dev.erudites.mods.pvradio.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import dev.erudites.mods.pvradio.client.config.RadioClientConfig;
import dev.erudites.mods.pvradio.client.config.RadioClientConfigStore;
import dev.erudites.mods.pvradio.network.RadioStatePayload;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

import java.util.List;

public final class RadioHud {

    private static final int PADDING = 4;
    private static final int BACKGROUND = 0x90000000;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final float SCALE = 0.5F;
    private static final int ICON_SIZE = 9;
    private static final int ICON_GAP = 2;
    private static final int NAME_GAP = 4;
    private static final int INDENT = 6;
    private static final int LINE_GAP = 2;
    private static final int SECTION_GAP = 4;
    private static final int QUIET_COLOR = 0x80FFFFFF;
    private static final int BORDER_COLOR = 0xFFFFFFFF;
    private static final int BORDER_WIDTH = 1;

    private final RadioClientConfigStore config;

    private int x;
    private int y;
    private @Nullable Bounds bounds;
    private @Nullable Grab grab;

    public RadioHud(final RadioClientConfigStore config) {
        this.config = config;
        this.x = config.config().hudX();
        this.y = config.config().hudY();
    }

    public void extractRenderState(final GuiGraphicsExtractor graphics, final DeltaTracker deltaTracker) {
        this.bounds = null;
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }
        List<RadioStatePayload.RadioView> radios = RadioState.state()
            .map(RadioStatePayload::radios)
            .orElse(List.of())
            .stream()
            .filter(radio -> radio.contains(player.getUUID()))
            .toList();
        if (radios.isEmpty()) {
            return;
        }
        Font font = minecraft.font;
        int lineHeight = font.lineHeight + LINE_GAP;

        int width = 0;
        int height = -SECTION_GAP;
        for (RadioStatePayload.RadioView radio : radios) {
            width = Math.max(width, this.headerWidth(font, radio, radio.member(player.getUUID()).orElse(null)));
            height += SECTION_GAP + lineHeight;
            for (RadioStatePayload.MemberView member : radio.members()) {
                if (member.online()) {
                    width = Math.max(width, INDENT + font.width(member.name()));
                    height += lineHeight;
                }
            }
        }

        int contentWidth = PADDING * 2 + width;
        int contentHeight = PADDING * 2 + height - LINE_GAP;
        int boxWidth = Mth.ceil(contentWidth * SCALE);
        int boxHeight = Mth.ceil(contentHeight * SCALE);
        int left = Mth.clamp(this.x, 0, Math.max(0, graphics.guiWidth() - boxWidth));
        int top = Mth.clamp(this.y, 0, Math.max(0, graphics.guiHeight() - boxHeight));
        this.bounds = new Bounds(left, top, boxWidth, boxHeight);

        graphics.pose().pushMatrix();
        graphics.pose().translate(left, top);
        graphics.pose().scale(SCALE);
        graphics.fill(0, 0, contentWidth, contentHeight, BACKGROUND);

        int textX = PADDING;
        int textY = PADDING;
        for (RadioStatePayload.RadioView radio : radios) {
            this.drawHeader(graphics, font, radio, radio.member(player.getUUID()).orElse(null), textX, textY);
            textY += lineHeight;
            for (RadioStatePayload.MemberView member : radio.members()) {
                if (!member.online()) {
                    continue;
                }
                boolean audible = member.micEnabled() && RadioState.talking(member.uuid());
                graphics.text(font, member.name(), textX + INDENT, textY, audible ? TEXT_COLOR : QUIET_COLOR);
                textY += lineHeight;
            }
            textY += SECTION_GAP;
        }
        graphics.pose().popMatrix();

        if (this.grab != null) {
            graphics.outline(
                left - BORDER_WIDTH,
                top - BORDER_WIDTH,
                boxWidth + BORDER_WIDTH * 2,
                boxHeight + BORDER_WIDTH * 2,
                BORDER_COLOR
            );
        }
    }

    public boolean mouseClicked(final Screen screen, final MouseButtonEvent event) {
        Bounds current = this.bounds;
        if (!(screen instanceof ChatScreen) || current == null || !current.contains(event.x(), event.y())) {
            return false;
        }
        switch (event.button()) {
            case InputConstants.MOUSE_BUTTON_LEFT -> {
                this.x = current.x();
                this.y = current.y();
                this.grab = new Grab(event.x() - current.x(), event.y() - current.y(), current.width(), current.height());
                return true;
            }
            case InputConstants.MOUSE_BUTTON_RIGHT -> {
                this.x = RadioClientConfig.DEFAULT_HUD_X;
                this.y = RadioClientConfig.DEFAULT_HUD_Y;
                this.drop();
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    public boolean mouseDragged(final MouseButtonEvent event) {
        Grab current = this.grab;
        if (current == null) {
            return false;
        }
        Window window = Minecraft.getInstance().getWindow();
        int maxX = Math.max(0, window.getGuiScaledWidth() - current.width());
        int maxY = Math.max(0, window.getGuiScaledHeight() - current.height());
        this.x = Mth.clamp((int) Math.round(event.x() - current.offsetX()), 0, maxX);
        this.y = Mth.clamp((int) Math.round(event.y() - current.offsetY()), 0, maxY);
        return true;
    }

    public boolean mouseReleased() {
        if (this.grab == null) {
            return false;
        }
        this.drop();
        return true;
    }

    public void tick() {
        if (this.grab != null && !(Minecraft.getInstance().screen instanceof ChatScreen)) {
            this.drop();
        }
    }

    private void drop() {
        this.grab = null;
        int droppedX = this.x;
        int droppedY = this.y;
        this.config.update(config -> config.withHud(droppedX, droppedY));
    }

    private int headerWidth(
        final Font font,
        final RadioStatePayload.RadioView radio,
        final RadioStatePayload.@Nullable MemberView self
    ) {
        int width = font.width(radio.name());
        if (self == null) {
            return width;
        }
        return width + NAME_GAP + ICON_SIZE * 2 + ICON_GAP;
    }

    private void drawHeader(
        final GuiGraphicsExtractor graphics,
        final Font font,
        final RadioStatePayload.RadioView radio,
        final RadioStatePayload.@Nullable MemberView self,
        final int x,
        final int y
    ) {
        Component name = Component.literal(radio.name());
        graphics.text(font, name, x, y, TEXT_COLOR);
        if (self == null) {
            return;
        }
        int iconX = x + font.width(name) + NAME_GAP;
        int iconY = y + (font.lineHeight - ICON_SIZE) / 2 - 1;
        RadioIcons.draw(graphics, RadioIcons.mic(self.micEnabled()), iconX, iconY, ICON_SIZE);
        RadioIcons.draw(graphics, RadioIcons.speaker(self.speakerEnabled()), iconX + ICON_SIZE + ICON_GAP, iconY, ICON_SIZE);
    }

    private record Bounds(
        int x,
        int y,
        int width,
        int height
    ) {

        private boolean contains(final double pointX, final double pointY) {
            return pointX >= this.x
                && pointX < this.x + this.width
                && pointY >= this.y
                && pointY < this.y + this.height;
        }
    }

    private record Grab(
        double offsetX,
        double offsetY,
        int width,
        int height
    ) {}
}
