package dev.erudites.mods.pvradio.client.compat.voice;

import dev.erudites.mods.pvradio.client.RadioVolumes;
import org.jspecify.annotations.Nullable;
import su.plo.voice.api.addon.AddonInitializer;
import su.plo.voice.api.addon.AddonLoaderScope;
import su.plo.voice.api.addon.InjectPlasmoVoice;
import su.plo.voice.api.addon.annotation.Addon;
import su.plo.voice.api.client.PlasmoVoiceClient;

@Addon(
    id = "pvradio_client",
    name = "PV Radio",
    scope = AddonLoaderScope.CLIENT,
    version = "1.0.0",
    authors = "shihyeon"
)
public final class RadioVoiceClientAddon implements AddonInitializer, RadioVolumes.Backend {

    @InjectPlasmoVoice
    private @Nullable PlasmoVoiceClient voiceClient;

    private RadioVoiceClientAddon() {}

    public static RadioVolumes.Backend load() {
        RadioVoiceClientAddon addon = new RadioVoiceClientAddon();
        PlasmoVoiceClient.getAddonsLoader().load(addon);
        return addon;
    }

    @Override
    public void onAddonInitialize() {}

    @Override
    public void onAddonShutdown() {}

    @Override
    public boolean ready() {
        return this.voiceClient != null;
    }

    @Override
    public double volume(final String lineName) {
        PlasmoVoiceClient client = this.voiceClient;
        if (client == null) {
            return RadioVolumes.DEFAULT_VOLUME;
        }
        return client.getConfig()
            .getVoice()
            .getVolumes()
            .getVolume(lineName)
            .value();
    }

    @Override
    public void volume(final String lineName, final double volume) {
        PlasmoVoiceClient client = this.voiceClient;
        if (client != null) {
            client.getConfig()
                .getVoice()
                .getVolumes()
                .setVolume(lineName, volume);
        }
    }
}
