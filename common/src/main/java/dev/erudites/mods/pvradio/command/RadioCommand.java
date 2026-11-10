package dev.erudites.mods.pvradio.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.erudites.mods.pvradio.radio.RadioServiceImpl;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;
import java.util.function.Supplier;

public final class RadioCommand {

    private static final String NAME = "radio";
    private static final String LEAVE = "leave";
    private static final String RADIO_ARGUMENT = "id";

    private RadioCommand() {}

    public static <S> LiteralArgumentBuilder<S> build(
        final PvRadioCommandSourceAccessor<S> sources,
        final Supplier<Optional<RadioServiceImpl>> radio
    ) {
        return LiteralArgumentBuilder.<S>literal(NAME)
            .then(LiteralArgumentBuilder.<S>literal(LEAVE)
                .then(RequiredArgumentBuilder.<S, Integer>argument(RADIO_ARGUMENT, IntegerArgumentType.integer(1))
                    .executes(context -> leave(context, sources, radio))
                )
            );
    }

    public static String leaveCommand(final int radioId) {
        return "/" + NAME + " " + LEAVE + " " + radioId;
    }

    private static <S> int leave(
        final CommandContext<S> context,
        final PvRadioCommandSourceAccessor<S> sources,
        final Supplier<Optional<RadioServiceImpl>> radio
    ) {
        Optional<ServerPlayer> player = sources.player(context.getSource());
        if (player.isEmpty()) {
            sources.failure(context.getSource(), Component.translatable("permissions.requires.player"));
            return 0;
        }
        int radioId = IntegerArgumentType.getInteger(context, RADIO_ARGUMENT);
        boolean left = radio.get()
            .map(present -> present.leave(player.get(), radioId))
            .orElse(false);
        if (!left) {
            sources.failure(context.getSource(), Component.translatable("command.pvradio.radio.not_member"));
            return 0;
        }
        return 1;
    }
}
