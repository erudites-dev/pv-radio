package dev.erudites.mods.pvradio.command;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public final class VanillaCommandSource implements PvRadioCommandSourceAccessor<CommandSourceStack> {

    @Override
    public Optional<ServerPlayer> player(final CommandSourceStack source) {
        return Optional.ofNullable(source.getPlayer());
    }

    @Override
    public void failure(final CommandSourceStack source, final Component message) {
        source.sendFailure(message);
    }
}
