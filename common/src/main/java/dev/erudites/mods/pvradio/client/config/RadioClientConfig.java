package dev.erudites.mods.pvradio.client.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record RadioClientConfig(int hudX, int hudY) {

    public static final int DEFAULT_HUD_X = 6;
    public static final int DEFAULT_HUD_Y = 6;

    public static final RadioClientConfig DEFAULT = new RadioClientConfig(DEFAULT_HUD_X, DEFAULT_HUD_Y);

    public static final Codec<RadioClientConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.optionalFieldOf("hud_x", DEFAULT_HUD_X).forGetter(RadioClientConfig::hudX),
        Codec.INT.optionalFieldOf("hud_y", DEFAULT_HUD_Y).forGetter(RadioClientConfig::hudY)
    ).apply(instance, RadioClientConfig::new));

    public RadioClientConfig withHud(final int hudX, final int hudY) {
        return new RadioClientConfig(hudX, hudY);
    }
}
