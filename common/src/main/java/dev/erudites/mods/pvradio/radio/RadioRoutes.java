package dev.erudites.mods.pvradio.radio;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

public final class RadioRoutes {

    private RadioRoutes() {}

    public static List<RadioRoute> of(final List<Radio> radios, final Predicate<UUID> reachable) {
        List<RadioRoute> routes = new ArrayList<>();
        Map<UUID, Set<UUID>> heard = new HashMap<>();
        List<Radio> ordered = radios.stream()
            .sorted(Comparator.comparingInt(Radio::id))
            .toList();
        for (Radio radio : ordered) {
            for (RadioMember sender : radio.members()) {
                if (!sender.micEnabled() || !reachable.test(sender.uuid())) {
                    continue;
                }
                Set<UUID> assigned = heard.computeIfAbsent(sender.uuid(), _ -> new HashSet<>());
                Set<UUID> listeners = new HashSet<>();
                for (RadioMember listener : radio.members()) {
                    if (!listener.uuid().equals(sender.uuid())
                        && listener.speakerEnabled()
                        && reachable.test(listener.uuid())
                        && assigned.add(listener.uuid())
                    ) {
                        listeners.add(listener.uuid());
                    }
                }
                routes.add(new RadioRoute(sender.uuid(), radio.id(), listeners));
            }
        }
        return List.copyOf(routes);
    }
}
