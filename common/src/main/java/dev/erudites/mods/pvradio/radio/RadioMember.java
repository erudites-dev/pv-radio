package dev.erudites.mods.pvradio.radio;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.erudites.mods.pvradio.api.RadioMemberInfo;
import net.minecraft.core.UUIDUtil;

import java.util.UUID;

public record RadioMember(
    UUID uuid,
    String name,
    boolean micEnabled,
    boolean speakerEnabled
) {

    public static final Codec<RadioMember> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        UUIDUtil.CODEC.fieldOf("uuid").forGetter(RadioMember::uuid),
        Codec.STRING.fieldOf("name").forGetter(RadioMember::name),
        Codec.BOOL.optionalFieldOf("mic_enabled", true).forGetter(RadioMember::micEnabled),
        Codec.BOOL.optionalFieldOf("speaker_enabled", true).forGetter(RadioMember::speakerEnabled)
    ).apply(instance, RadioMember::new));

    public static RadioMember joined(final UUID uuid, final String name) {
        return new RadioMember(uuid, name, true, true);
    }

    public RadioMemberInfo info() {
        return new RadioMemberInfo(this.uuid, this.name, this.micEnabled, this.speakerEnabled);
    }

    public RadioMember withName(final String name) {
        return new RadioMember(this.uuid, name, this.micEnabled, this.speakerEnabled);
    }

    public RadioMember withAudio(final boolean micEnabled, final boolean speakerEnabled) {
        return new RadioMember(this.uuid, this.name, micEnabled, speakerEnabled);
    }
}
