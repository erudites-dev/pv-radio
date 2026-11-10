package dev.erudites.mods.pvradio.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.erudites.mods.pvradio.PvRadioMod;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.UnaryOperator;

public final class RadioClientConfigStore {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path path;

    private RadioClientConfig config;

    public RadioClientConfigStore(final Path path) {
        this.path = path;
        this.config = this.load();
    }

    public RadioClientConfig config() {
        return this.config;
    }

    public void update(final UnaryOperator<RadioClientConfig> update) {
        this.config = update.apply(this.config);
        this.save();
    }

    private RadioClientConfig load() {
        if (!Files.exists(this.path)) {
            return RadioClientConfig.DEFAULT;
        }
        try (Reader reader = Files.newBufferedReader(this.path)) {
            JsonElement json = JsonParser.parseReader(reader);
            return RadioClientConfig.CODEC.parse(JsonOps.INSTANCE, json)
                .resultOrPartial(error -> PvRadioMod.LOGGER.warn("Invalid config '{}': {}", this.path, error))
                .orElse(RadioClientConfig.DEFAULT);
        } catch (IOException | JsonParseException exception) {
            PvRadioMod.LOGGER.warn("Failed to load config '{}', using defaults", this.path, exception);
            return RadioClientConfig.DEFAULT;
        }
    }

    private void save() {
        JsonElement json = RadioClientConfig.CODEC.encodeStart(JsonOps.INSTANCE, this.config).getOrThrow();
        try {
            Files.createDirectories(this.path.getParent());
            Files.writeString(this.path, GSON.toJson(json));
        } catch (IOException exception) {
            PvRadioMod.LOGGER.warn("Failed to save config '{}'", this.path, exception);
        }
    }
}
