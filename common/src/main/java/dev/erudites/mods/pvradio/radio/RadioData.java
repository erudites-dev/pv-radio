package dev.erudites.mods.pvradio.radio;

import dev.erudites.mods.pvradio.PvRadioMod;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public final class RadioData extends SavedData {

    public static final SavedDataType<RadioData> TYPE = new SavedDataType<>(
        PvRadioMod.id("radios"),
        () -> new RadioData(RadioBook.EMPTY),
        RadioBook.CODEC.xmap(RadioData::new, RadioData::book),
        DataFixTypes.SAVED_DATA_MAP_DATA
    );

    private RadioBook book;

    private RadioData(final RadioBook book) {
        this.book = book;
    }

    public RadioBook book() {
        return this.book;
    }

    public void book(final RadioBook book) {
        this.book = book;
        this.setDirty();
    }
}
