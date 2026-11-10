package dev.erudites.mods.pvradio.neoforge;

import dev.erudites.mods.pvradio.PvRadioMod;
import dev.erudites.mods.pvradio.command.VanillaCommandSource;
import dev.erudites.mods.pvradio.neoforge.network.PvRadioNeoForgeNetworking;
import dev.erudites.mods.pvradio.neoforge.network.PvRadioNeoForgePayloads;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Mod(PvRadioMod.MODID)
public class PvRadioNeoForgeMod {

    public PvRadioNeoForgeMod(final IEventBus bus) {
        PvRadioMod.initialize(new PvRadioNeoForgeNetworking());
        bus.addListener(PvRadioNeoForgePayloads::register);

        NeoForge.EVENT_BUS.addListener(
            (RegisterCommandsEvent event) -> event.getDispatcher().register(PvRadioMod.command(new VanillaCommandSource()))
        );
        NeoForge.EVENT_BUS.addListener((ServerStartedEvent event) -> PvRadioMod.start(event.getServer()));
        NeoForge.EVENT_BUS.addListener((ServerStoppingEvent _) -> PvRadioMod.stop());
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post _) -> PvRadioMod.tick());
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                PvRadioMod.playerJoined(player);
            }
        });
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent _) -> PvRadioMod.playerLeft());
    }
}
