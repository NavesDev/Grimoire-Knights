package com.navesdev.grimoireknights.core.client.vfx;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

/**
 * Loads the Blockbench effects: emitters from {@code assets/<ns>/vfx/particles/*.json} and sequences (which emitters
 * fire when) from the particle keyframes of {@code assets/<ns>/vfx/animations/*.animation.json}, one animation
 * {@code animation.vfx.<name>} per sequence {@code <ns>:<name>}. A broken file fails the load with its name.
 */
final class VfxLibrary extends SimplePreparableReloadListener<VfxLibrary.Loaded> {
    /** One emitter firing {@code time} seconds into a sequence. */
    record Cue(double time, VfxEffect effect) {}

    record Loaded(Map<ResourceLocation, List<Cue>> sequences) {}

    private static final FileToIdConverter PARTICLES = FileToIdConverter.json("vfx/particles");
    private static final FileToIdConverter ANIMATIONS = new FileToIdConverter("vfx/animations", ".animation.json");
    private static final String PREFIX = "animation.vfx.";

    private static Map<ResourceLocation, List<Cue>> sequences = Map.of();

    static List<Cue> sequence(ResourceLocation id) {
        return sequences.get(id);
    }

    @Override
    protected Loaded prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, VfxEffect> effects = new HashMap<>();
        PARTICLES.listMatchingResources(manager).forEach((file, resource) -> {
            VfxEffect effect = load(file, resource, json -> VfxEffect.parse(file.getNamespace(), json));
            effects.put(effect.id(), effect);
        });
        Map<ResourceLocation, List<Cue>> loaded = new HashMap<>();
        ANIMATIONS.listMatchingResources(manager).forEach((file, resource) -> load(file, resource, json -> {
            json.getAsJsonObject("animations").entrySet().forEach(anim -> {
                if (!anim.getKey().startsWith(PREFIX)) throw new IllegalArgumentException("animation " + anim.getKey() + " is not " + PREFIX + "<name>");
                ResourceLocation id = ResourceLocation.fromNamespaceAndPath(file.getNamespace(), anim.getKey().substring(PREFIX.length()));
                loaded.put(id, cues(file.getNamespace(), anim.getValue().getAsJsonObject(), effects));
            });
            return null;
        }));
        return new Loaded(Map.copyOf(loaded));
    }

    @Override
    protected void apply(Loaded loaded, ResourceManager manager, ProfilerFiller profiler) {
        sequences = loaded.sequences();
    }

    private static List<Cue> cues(String namespace, JsonObject animation, Map<ResourceLocation, VfxEffect> effects) {
        List<Cue> cues = new ArrayList<>();
        JsonObject keyframes = animation.has("particle_effects") ? animation.getAsJsonObject("particle_effects") : new JsonObject();
        keyframes.entrySet().forEach(key -> {
            List<JsonElement> list = key.getValue().isJsonArray() ? key.getValue().getAsJsonArray().asList() : List.of(key.getValue());
            for (JsonElement e : list) {
                // Blockbench names the keyframe effect by its short name; the identifier is <namespace>:<name>.
                ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, e.getAsJsonObject().get("effect").getAsString());
                VfxEffect effect = effects.get(id);
                if (effect == null) throw new IllegalArgumentException("no effect " + id + " in vfx/particles");
                cues.add(new Cue(Double.parseDouble(key.getKey()), effect));
            }
        });
        return List.copyOf(cues);
    }

    private static <T> T load(ResourceLocation file, Resource resource, java.util.function.Function<JsonObject, T> parse) {
        try (Reader reader = resource.openAsReader()) {
            return parse.apply(JsonParser.parseReader(reader).getAsJsonObject());
        } catch (Exception e) {
            throw new IllegalStateException("Bad VFX file " + file + ": " + e.getMessage(), e);
        }
    }
}
