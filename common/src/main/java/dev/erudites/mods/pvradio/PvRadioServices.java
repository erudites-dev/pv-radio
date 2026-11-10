package dev.erudites.mods.pvradio;

import dev.erudites.mods.pvradio.network.PvRadioNetworking;
import dev.erudites.mods.pvradio.network.RadioPayloadHandler;
import dev.erudites.mods.pvradio.radio.RadioAudio;
import dev.erudites.mods.pvradio.radio.RadioServiceImpl;
import net.minecraft.server.MinecraftServer;

public final class PvRadioServices {

    private final RadioServiceImpl radio;
    private final RadioPayloadHandler payloads;

    public PvRadioServices(
        final MinecraftServer server,
        final RadioAudio audio,
        final PvRadioNetworking networking
    ) {
        this.radio = new RadioServiceImpl(server, audio, networking);
        this.payloads = new RadioPayloadHandler(this.radio);
    }

    public RadioServiceImpl radio() {
        return this.radio;
    }

    public RadioPayloadHandler payloads() {
        return this.payloads;
    }

    public void tick() {
        this.radio.tick();
    }

    public void close() {
        this.radio.close();
    }
}
