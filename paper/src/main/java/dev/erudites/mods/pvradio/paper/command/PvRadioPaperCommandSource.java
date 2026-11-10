package dev.erudites.mods.pvradio.paper.command;

import dev.erudites.mods.pvradio.command.PvRadioCommandSourceAccessor;
import io.papermc.paper.adventure.PaperAdventure;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Optional;

public final class PvRadioPaperCommandSource implements PvRadioCommandSourceAccessor<CommandSourceStack> {

    @Override
    public Optional<ServerPlayer> player(final CommandSourceStack source) {
        return source.getSender() instanceof Player player
            ? Optional.of(((CraftPlayer) player).getHandle())
            : Optional.empty();
    }

    @Override
    public void failure(final CommandSourceStack source, final Component message) {
        source.getSender().sendMessage(PaperAdventure.asAdventure(message).colorIfAbsent(NamedTextColor.RED));
    }
}
