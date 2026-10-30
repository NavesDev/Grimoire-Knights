package com.navesdev.grimoireknights.core.client.vfx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class VfxEffectTest {
    private static final Path ASSETS = assets();
    private static final Path VFX = ASSETS.resolve("grimoireknights/vfx");

    private static Path assets() {
        try {
            return Path.of(VfxEffectTest.class.getResource("/assets/grimoireknights/vfx").toURI()).getParent().getParent();
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private static JsonObject json(Path file) throws IOException {
        return JsonParser.parseString(Files.readString(file)).getAsJsonObject();
    }

    @Test
    void everyEffectOfTheModLoads() throws IOException {
        List<Path> files;
        try (Stream<Path> list = Files.list(VFX.resolve("particles"))) {
            files = list.toList();
        }
        assertFalse(files.isEmpty());
        for (Path file : files) {
            VfxEffect effect = VfxEffect.parse("grimoireknights", json(file));
            assertEquals(file.getFileName().toString().replace(".json", ""), effect.id().getPath(), "identifier matches the file name");
            assertEquals(true, Files.exists(ASSETS.resolve(effect.texture().getNamespace())
                    .resolve(effect.texture().getPath())), "texture exists for " + file);
        }
    }

    @Test
    void unsupportedComponentFailsTheLoad() throws IOException {
        JsonObject effect = json(VFX.resolve("particles/fireball_explosion_flash.json"));
        effect.getAsJsonObject("particle_effect").getAsJsonObject("components").add("minecraft:emitter_lifetime_looping", new JsonObject());
        assertThrows(IllegalArgumentException.class, () -> VfxEffect.parse("grimoireknights", effect));
    }
}
