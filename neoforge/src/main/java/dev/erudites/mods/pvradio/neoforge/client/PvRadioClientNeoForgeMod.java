package dev.erudites.mods.pvradio.neoforge.client;

import dev.erudites.mods.pvradio.PvRadioMod;
import dev.erudites.mods.pvradio.client.PvRadioClientMod;
import dev.erudites.mods.pvradio.client.RadioClientService;
import dev.erudites.mods.pvradio.neoforge.client.network.PvRadioNeoForgeClientNetworking;
import dev.erudites.mods.pvradio.network.RadioStatePayload;
import dev.erudites.mods.pvradio.network.RadioTalkingPayload;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = PvRadioMod.MODID, dist = Dist.CLIENT)
public class PvRadioClientNeoForgeMod {

    public PvRadioClientNeoForgeMod(final IEventBus bus) {
        KeyMapping.Category category = new KeyMapping.Category(PvRadioClientMod.KEY_CATEGORY);
        RadioClientService radio = PvRadioClientMod.initializeClient(
            new PvRadioNeoForgeClientNetworking(),
            category,
            FMLPaths.CONFIGDIR.get()
        );

        bus.addListener((RegisterKeyMappingsEvent event) -> {
            event.registerCategory(category);
            event.register(radio.openRadioKey());
        });
        bus.addListener((RegisterGuiLayersEvent event) -> event.registerAboveAll(
            PvRadioClientMod.HUD_ID,
            radio.hud()::extractRenderState
        ));
        bus.addListener((RegisterClientPayloadHandlersEvent event) -> {
            event.register(RadioStatePayload.TYPE, (payload, _) -> radio.onState(payload));
            event.register(RadioTalkingPayload.TYPE, (payload, _) -> radio.onTalking(payload));
        });

        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post _) -> radio.tick());
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut _) -> radio.disconnect());
        NeoForge.EVENT_BUS.addListener((ScreenEvent.MouseButtonPressed.Pre event) -> {
            if (radio.hud().mouseClicked(event.getScreen(), event.getMouseButtonEvent())) {
                event.setCanceled(true);
            }
        });
        NeoForge.EVENT_BUS.addListener((ScreenEvent.MouseDragged.Pre event) -> {
            if (radio.hud().mouseDragged(event.getMouseButtonEvent())) {
                event.setCanceled(true);
            }
        });
        NeoForge.EVENT_BUS.addListener((ScreenEvent.MouseButtonReleased.Pre event) -> {
            if (radio.hud().mouseReleased()) {
                event.setCanceled(true);
            }
        });
    }
}
