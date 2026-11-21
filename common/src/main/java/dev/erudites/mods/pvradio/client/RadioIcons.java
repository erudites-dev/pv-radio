package dev.erudites.mods.pvradio.client;

import dev.erudites.mods.pvradio.PvRadioMod;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

final class RadioIcons {

    private static final Identifier MIC = plasmoVoice("microphone");
    private static final Identifier MIC_DISABLED = plasmoVoice("microphone_disabled");
    private static final Identifier SPEAKER = plasmoVoice("speaker");
    private static final Identifier SPEAKER_DISABLED = plasmoVoice("speaker_disabled");

    private RadioIcons() {}

    static Identifier mic(final boolean enabled) {
        return enabled ? MIC : MIC_DISABLED;
    }

    static Identifier speaker(final boolean enabled) {
        return enabled ? SPEAKER : SPEAKER_DISABLED;
    }

    static void draw(
        final GuiGraphicsExtractor graphics,
        final Identifier icon,
        final int x,
        final int y,
        final int size
    ) {
        graphics.blit(icon, x, y, x + size, y + size, 0.0F, 1.0F, 0.0F, 1.0F);
    }

    private static Identifier plasmoVoice(final String name) {
        return Identifier.fromNamespaceAndPath(PvRadioMod.PLASMO_VOICE_ID, "textures/icons/" + name + ".png");
    }
}
