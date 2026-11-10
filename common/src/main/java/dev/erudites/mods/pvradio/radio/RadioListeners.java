package dev.erudites.mods.pvradio.radio;

import dev.erudites.mods.pvradio.PvRadioMod;
import dev.erudites.mods.pvradio.api.RadioInfo;
import dev.erudites.mods.pvradio.api.RadioListener;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class RadioListeners {

    private static final CopyOnWriteArrayList<RadioListener> LISTENERS = new CopyOnWriteArrayList<>();

    private RadioListeners() {}

    public static void add(final RadioListener listener) {
        LISTENERS.addIfAbsent(Objects.requireNonNull(listener, "listener"));
    }

    public static void remove(final RadioListener listener) {
        LISTENERS.remove(listener);
    }

    static void talking(final UUID player, final boolean talking) {
        dispatch(listener -> listener.onTalkingChanged(player, talking));
    }

    static void changed(final RadioBook before, final RadioBook after) {
        if (LISTENERS.isEmpty() || before.radios().equals(after.radios())) {
            return;
        }
        for (Radio old : before.radios()) {
            if (after.radio(old.id()).isEmpty()) {
                RadioInfo info = old.info();
                dispatch(listener -> listener.onRadioDeleted(info));
            }
        }
        for (Radio radio : after.radios()) {
            Optional<Radio> old = before.radio(radio.id());
            if (old.isEmpty()) {
                RadioInfo info = radio.info();
                dispatch(listener -> listener.onRadioCreated(info));
            } else if (!old.get().equals(radio)) {
                changed(old.get(), radio);
            }
        }
    }

    private static void changed(final Radio before, final Radio after) {
        RadioInfo info = after.info();
        for (RadioMember old : before.members()) {
            if (!after.contains(old.uuid())) {
                dispatch(listener -> listener.onMemberLeft(info, old.info()));
            }
        }
        for (RadioMember member : after.members()) {
            Optional<RadioMember> old = before.member(member.uuid());
            if (old.isEmpty()) {
                dispatch(listener -> listener.onMemberJoined(info, member.info()));
            } else if (old.get().micEnabled() != member.micEnabled()
                || old.get().speakerEnabled() != member.speakerEnabled()
            ) {
                dispatch(listener -> listener.onMemberAudioChanged(info, member.info()));
            }
        }
        if (!before.owner().equals(after.owner())) {
            dispatch(listener -> listener.onOwnerChanged(info, before.owner()));
        }
    }

    private static void dispatch(final Consumer<RadioListener> event) {
        for (RadioListener listener : LISTENERS) {
            try {
                event.accept(listener);
            } catch (RuntimeException exception) {
                PvRadioMod.LOGGER.error("Radio listener {} failed", listener.getClass().getName(), exception);
            }
        }
    }
}
