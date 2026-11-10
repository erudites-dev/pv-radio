package dev.erudites.mods.pvradio.paper;

import dev.erudites.mods.pvradio.PvRadioMod;
import dev.erudites.mods.pvradio.paper.command.PvRadioPaperCommandSource;
import dev.erudites.mods.pvradio.paper.network.PvRadioPaperNetworking;
import dev.erudites.mods.pvradio.paper.network.PvRadioPaperPayloads;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.plugin.java.JavaPlugin;

public class PvRadioPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        PvRadioPaperPayloads payloads = new PvRadioPaperPayloads();
        PvRadioMod.initialize(new PvRadioPaperNetworking(this, payloads));
        PvRadioMod.start(((CraftServer) this.getServer()).getServer());

        payloads.register(this);
        this.getLifecycleManager().registerEventHandler(
            LifecycleEvents.COMMANDS,
            event -> event.registrar().register(PvRadioMod.command(new PvRadioPaperCommandSource()).build())
        );
        this.getServer().getPluginManager().registerEvents(new PvRadioListener(), this);
    }

    @Override
    public void onDisable() {
        PvRadioMod.stop();
    }
}
