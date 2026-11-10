package dev.erudites.mods.pvradio.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.erudites.mods.pvradio.client.config.RadioClientConfigStore;
import dev.erudites.mods.pvradio.client.network.PvRadioClientNetworking;
import dev.erudites.mods.pvradio.network.RadioStatePayload;
import dev.erudites.mods.pvradio.network.RadioTalkingPayload;
import dev.erudites.mods.pvradio.network.RequestRadioPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class RadioClientService {

    private final PvRadioClientNetworking networking;
    private final KeyMapping openRadio;
    private final RadioHud hud;

    public RadioClientService(
        final PvRadioClientNetworking networking,
        final KeyMapping.Category category,
        final RadioClientConfigStore config
    ) {
        this.networking = networking;
        this.openRadio = new KeyMapping("key.pvradio.open_radio", InputConstants.KEY_X, category);
        this.hud = new RadioHud(config);
    }

    public KeyMapping openRadioKey() {
        return this.openRadio;
    }

    public RadioHud hud() {
        return this.hud;
    }

    public void tick() {
        this.hud.tick();
        Minecraft client = Minecraft.getInstance();
        while (this.openRadio.consumeClick()) {
            if (client.screen != null) {
                continue;
            }
            if (this.networking.canSend(RequestRadioPayload.TYPE)) {
                RadioState.requestOpen();
                this.networking.send(RequestRadioPayload.INSTANCE);
            } else {
                showUnavailable(client);
            }
        }
    }

    public void onState(final RadioStatePayload payload) {
        Minecraft client = Minecraft.getInstance();
        RadioState.update(payload);
        if (client.screen instanceof RadioScreen screen) {
            screen.refresh();
        } else if (RadioState.consumeOpenRequest()) {
            if (payload.available()) {
                client.setScreen(new RadioListScreen(this.networking));
            } else {
                showUnavailable(client);
            }
        }
    }

    public void onTalking(final RadioTalkingPayload payload) {
        RadioState.talking(payload.player(), payload.talking());
    }

    public void disconnect() {
        RadioState.clear();
    }

    private static void showUnavailable(final Minecraft client) {
        client.gui.setOverlayMessage(Component.translatable("message.pvradio.radio.unavailable"), false);
    }
}
