package dev.erudites.mods.pvradio.fabric.client;

import dev.erudites.mods.pvradio.client.PvRadioClientMod;
import dev.erudites.mods.pvradio.client.RadioClientService;
import dev.erudites.mods.pvradio.fabric.client.network.PvRadioFabricClientNetworking;
import dev.erudites.mods.pvradio.network.RadioStatePayload;
import dev.erudites.mods.pvradio.network.RadioTalkingPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;

public class PvRadioClientFabricMod implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        RadioClientService radio = PvRadioClientMod.initializeClient(
            new PvRadioFabricClientNetworking(),
            KeyMapping.Category.register(PvRadioClientMod.KEY_CATEGORY),
            FabricLoader.getInstance().getConfigDir()
        );
        KeyMappingHelper.registerKeyMapping(radio.openRadioKey());
        HudElementRegistry.addLast(PvRadioClientMod.HUD_ID, radio.hud()::extractRenderState);
        ScreenEvents.AFTER_INIT.register((_, screen, _, _) -> {
            ScreenMouseEvents.allowMouseClick(screen).register((current, event) -> !radio.hud().mouseClicked(current, event));
            ScreenMouseEvents.allowMouseDrag(screen).register((_, event, _, _) -> !radio.hud().mouseDragged(event));
            ScreenMouseEvents.allowMouseRelease(screen).register((_, _) -> !radio.hud().mouseReleased());
        });

        ClientPlayNetworking.registerGlobalReceiver(RadioStatePayload.TYPE, (payload, _) -> radio.onState(payload));
        ClientPlayNetworking.registerGlobalReceiver(RadioTalkingPayload.TYPE, (payload, _) -> radio.onTalking(payload));
        ClientPlayConnectionEvents.DISCONNECT.register((_, _) -> radio.disconnect());
        ClientTickEvents.END_CLIENT_TICK.register(_ -> radio.tick());
    }
}
