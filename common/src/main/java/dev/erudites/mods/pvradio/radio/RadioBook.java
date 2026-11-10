package dev.erudites.mods.pvradio.radio;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record RadioBook(int nextId, List<Radio> radios) {

    public static final RadioBook EMPTY = new RadioBook(1, List.of());

    public static final Codec<RadioBook> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.fieldOf("next_id").forGetter(RadioBook::nextId),
        Radio.CODEC.listOf().fieldOf("radios").forGetter(RadioBook::radios)
    ).apply(instance, RadioBook::new));

    public RadioBook {
        radios = List.copyOf(radios);
    }

    public Optional<Radio> radio(final int id) {
        return this.radios.stream()
            .filter(radio -> radio.id() == id)
            .findFirst();
    }

    public List<Radio> joined(final UUID uuid) {
        return this.radios.stream()
            .filter(radio -> radio.contains(uuid))
            .toList();
    }

    public RadioBook withCreated(final Radio radio) {
        List<Radio> radios = new ArrayList<>(this.radios);
        radios.add(radio);
        return new RadioBook(Math.max(this.nextId, radio.id() + 1), radios);
    }

    public RadioBook withRadio(final Radio radio) {
        List<Radio> radios = this.radios.stream()
            .map(existing -> existing.id() == radio.id() ? radio : existing)
            .toList();
        return new RadioBook(this.nextId, radios);
    }

    public RadioBook withoutRadio(final int id) {
        List<Radio> radios = this.radios.stream()
            .filter(radio -> radio.id() != id)
            .toList();
        return new RadioBook(this.nextId, radios);
    }
}
