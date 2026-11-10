package dev.erudites.mods.pvradio.fabric;

import dev.erudites.mods.pvradio.PvRadioMod;
import dev.erudites.mods.pvradio.command.VanillaCommandSource;
import dev.erudites.mods.pvradio.fabric.network.PvRadioFabricNetworking;
import dev.erudites.mods.pvradio.fabric.network.PvRadioFabricPayloads;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class PvRadioFabricMod implements ModInitializer {

    @Override
    public void onInitialize() {
        PvRadioFabricPayloads.registerTypes();
        PvRadioMod.initialize(new PvRadioFabricNetworking());
        PvRadioFabricPayloads.registerReceivers();

        CommandRegistrationCallback.EVENT.register(
            (dispatcher, _, _) -> dispatcher.register(PvRadioMod.command(new VanillaCommandSource()))
        );
        ServerLifecycleEvents.SERVER_STARTED.register(PvRadioMod::start);
        ServerLifecycleEvents.SERVER_STOPPING.register(_ -> PvRadioMod.stop());
        ServerTickEvents.END_SERVER_TICK.register(_ -> PvRadioMod.tick());
        ServerPlayConnectionEvents.JOIN.register((handler, _, _) -> PvRadioMod.playerJoined(handler.player));
        ServerPlayConnectionEvents.DISCONNECT.register((_, _) -> PvRadioMod.playerLeft());
    }
}
