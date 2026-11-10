package dev.erudites.mods.pvradio.client;

import dev.erudites.mods.pvradio.PvRadioMod;
import dev.erudites.mods.pvradio.client.compat.voice.RadioVoiceClientAddon;
import dev.erudites.mods.pvradio.client.config.RadioClientConfigStore;
import dev.erudites.mods.pvradio.client.network.PvRadioClientNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

import java.nio.file.Path;

public final class PvRadioClientMod {

    public static final Identifier KEY_CATEGORY = PvRadioMod.id("main");
    public static final Identifier HUD_ID = PvRadioMod.id("radio");

    private static final String CONFIG_FILE = PvRadioMod.MODID + "-client.json";

    private PvRadioClientMod() {}

    public static RadioClientService initializeClient(
        final PvRadioClientNetworking networking,
        final KeyMapping.Category category,
        final Path configDir
    ) {
        RadioVolumes.backend(RadioVoiceClientAddon.load());
        return new RadioClientService(networking, category, new RadioClientConfigStore(configDir.resolve(CONFIG_FILE)));
    }
}
