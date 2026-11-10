package dev.erudites.mods.pvradio.radio;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.erudites.mods.pvradio.api.RadioInfo;
import dev.erudites.mods.pvradio.api.RadioService;
import net.minecraft.core.UUIDUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;

public record Radio(
    int id,
    String name,
    UUID owner,
    List<RadioMember> members
) {

    public static final int MAX_NAME_LENGTH = RadioService.MAX_NAME_LENGTH;

    public static final Codec<Radio> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.fieldOf("id").forGetter(Radio::id),
        Codec.STRING.fieldOf("name").forGetter(Radio::name),
        UUIDUtil.CODEC.fieldOf("owner").forGetter(Radio::owner),
        RadioMember.CODEC.listOf().fieldOf("members").forGetter(Radio::members)
    ).apply(instance, Radio::new));

    private static final String LINE_PREFIX = "radio_";

    public Radio {
        members = List.copyOf(members);
    }

    public static String lineName(final int id) {
        return LINE_PREFIX + id;
    }

    public String lineName() {
        return lineName(this.id);
    }

    public RadioInfo info() {
        return new RadioInfo(
            this.id,
            this.name,
            this.owner,
            this.members.stream()
                .map(RadioMember::info)
                .toList()
        );
    }

    public Optional<RadioMember> member(final UUID uuid) {
        return this.members.stream()
            .filter(member -> member.uuid().equals(uuid))
            .findFirst();
    }

    public boolean contains(final UUID uuid) {
        return this.member(uuid).isPresent();
    }

    public Radio withMember(final RadioMember member) {
        List<RadioMember> members = new ArrayList<>(this.members);
        members.add(member);
        return new Radio(this.id, this.name, this.owner, members);
    }

    public Radio withMember(final UUID uuid, final UnaryOperator<RadioMember> update) {
        List<RadioMember> members = this.members.stream()
            .map(member -> member.uuid().equals(uuid) ? update.apply(member) : member)
            .toList();
        return new Radio(this.id, this.name, this.owner, members);
    }

    public Radio withoutMember(final UUID uuid) {
        List<RadioMember> members = this.members.stream()
            .filter(member -> !member.uuid().equals(uuid))
            .toList();
        return new Radio(this.id, this.name, this.owner, members);
    }

    public Radio withOwner(final UUID owner) {
        return new Radio(this.id, this.name, owner, this.members);
    }
}
