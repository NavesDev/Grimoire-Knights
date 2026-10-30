package com.navesdev.grimoireknights.core.client.vfx;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.navesdev.grimoireknights.core.vfx.molang.Molang;
import com.navesdev.grimoireknights.core.vfx.molang.Molang.Expr;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import net.minecraft.resources.ResourceLocation;

/**
 * One Bedrock particle effect (Snowstorm format, made in Blockbench), parsed into what {@link VfxEmitter} plays.
 * Only the components the mod's effects use are supported; any other one fails the load (docs/adr/0004-bedrock-particles.md).
 */
record VfxEffect(
        ResourceLocation id, ResourceLocation texture, Material material, boolean lighting,
        Expr burst, Expr rate, Expr maxParticles, Expr activeTime,
        Shape shape, Expr[] offset, Expr radius, Expr[] halfDimensions, boolean surfaceOnly, Expr[] planeNormal,
        Direction direction, Expr[] directionVector,
        Expr lifetime, Expr speed, Expr spin, Expr spinRate, Expr[] acceleration, Expr drag,
        Expr[] size, Facing facing, double minSpeedThreshold, Uv uv, Tint tint) {

    enum Material { ALPHA, BLEND, ADD }

    enum Shape { POINT, SPHERE, DISC, BOX }

    enum Direction { OUTWARDS, INWARDS, VECTOR }

    enum Facing { LOOKAT_XYZ, LOOKAT_Y, LOOKAT_DIRECTION, EMITTER_TRANSFORM_XY, EMITTER_TRANSFORM_XZ, EMITTER_TRANSFORM_YZ }

    /** Texture region in pixels; a flipbook steps {@code step} per frame. */
    record Uv(float textureWidth, float textureHeight, Expr[] base, Expr[] size, Expr[] step, double fps, Expr maxFrame,
              boolean stretchToLifetime, boolean loop) {}

    /** Color: a gradient over {@code interpolant} (keys in its units), or one RGBA expression each. */
    record Tint(TreeMap<Double, float[]> gradient, Expr interpolant, Expr[] rgba) {}

    private static final Set<String> SUPPORTED = Set.of(
            "minecraft:emitter_rate_instant", "minecraft:emitter_rate_steady", "minecraft:emitter_lifetime_once",
            "minecraft:emitter_shape_point", "minecraft:emitter_shape_sphere", "minecraft:emitter_shape_disc",
            "minecraft:emitter_shape_box", "minecraft:emitter_local_space", "minecraft:particle_lifetime_expression",
            "minecraft:particle_initial_speed", "minecraft:particle_initial_spin", "minecraft:particle_motion_dynamic",
            "minecraft:particle_appearance_billboard", "minecraft:particle_appearance_tinting",
            "minecraft:particle_appearance_lighting");
    private static final Expr ZERO = Molang.constant(0);
    private static final Expr ONE = Molang.constant(1);

    /** {@code namespace} is the resource pack's; Bedrock texture paths ("textures/particle/x") live under its {@code vfx/}. */
    static VfxEffect parse(String namespace, JsonObject root) {
        JsonObject effect = root.getAsJsonObject("particle_effect");
        JsonObject description = effect.getAsJsonObject("description");
        ResourceLocation id = ResourceLocation.parse(description.get("identifier").getAsString());
        JsonObject render = description.getAsJsonObject("basic_render_parameters");
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(namespace, "vfx/" + render.get("texture").getAsString() + ".png");
        Material material = switch (render.get("material").getAsString()) {
            case "particles_alpha", "particles_opaque" -> Material.ALPHA;
            case "particles_blend" -> Material.BLEND;
            case "particles_add" -> Material.ADD;
            default -> throw new IllegalArgumentException("unsupported material " + render.get("material"));
        };
        JsonObject c = effect.getAsJsonObject("components");
        for (String key : c.keySet()) {
            if (!SUPPORTED.contains(key)) throw new IllegalArgumentException("unsupported component " + key);
        }

        JsonObject instant = obj(c, "minecraft:emitter_rate_instant"), steady = obj(c, "minecraft:emitter_rate_steady");
        JsonObject once = obj(c, "minecraft:emitter_lifetime_once");
        if (once == null) throw new IllegalArgumentException("needs minecraft:emitter_lifetime_once (looping emitters are not supported)");

        Shape shape = Shape.POINT;
        JsonObject shapeJson = obj(c, "minecraft:emitter_shape_point");
        for (Shape s : Shape.values()) {
            JsonObject o = obj(c, "minecraft:emitter_shape_" + s.name().toLowerCase(Locale.ROOT));
            if (o != null) {
                shape = s;
                shapeJson = o;
            }
        }
        if (shapeJson == null) shapeJson = new JsonObject();
        Direction direction = Direction.OUTWARDS;
        Expr[] directionVector = null;
        JsonElement dir = shapeJson.get("direction");
        if (dir != null && dir.isJsonArray()) {
            direction = Direction.VECTOR;
            directionVector = vec(dir, 3, 0);
        } else if (dir != null && dir.getAsString().equals("inwards")) {
            direction = Direction.INWARDS;
        }

        JsonObject spin = obj(c, "minecraft:particle_initial_spin"), motion = obj(c, "minecraft:particle_motion_dynamic");
        JsonObject lifetime = obj(c, "minecraft:particle_lifetime_expression");
        JsonObject billboard = obj(c, "minecraft:particle_appearance_billboard");
        if (billboard == null || lifetime == null) throw new IllegalArgumentException("needs particle_appearance_billboard and particle_lifetime_expression");
        JsonObject directionJson = billboard.has("direction") ? billboard.getAsJsonObject("direction") : new JsonObject();

        return new VfxEffect(id, texture, material, c.has("minecraft:particle_appearance_lighting"),
                instant == null ? ZERO : expr(instant, "num_particles", 10),
                steady == null ? ZERO : expr(steady, "spawn_rate", 1),
                steady == null ? ZERO : expr(steady, "max_particles", 50),
                expr(once, "active_time", 10),
                shape, vec(shapeJson.get("offset"), 3, 0), expr(shapeJson, "radius", 1), vec(shapeJson.get("half_dimensions"), 3, 0),
                shapeJson.has("surface_only") && shapeJson.get("surface_only").getAsBoolean(),
                shapeJson.has("plane_normal") ? planeNormal(shapeJson.get("plane_normal")) : vec(null, 3, 0),
                direction, directionVector,
                expr(lifetime, "max_lifetime", 1),
                c.has("minecraft:particle_initial_speed") ? expr(c.get("minecraft:particle_initial_speed")) : ZERO,
                spin == null ? ZERO : expr(spin, "rotation", 0),
                spin == null ? ZERO : expr(spin, "rotation_rate", 0),
                motion == null ? vec(null, 3, 0) : vec(motion.get("linear_acceleration"), 3, 0),
                motion == null ? ZERO : expr(motion, "linear_drag_coefficient", 0),
                vec(billboard.get("size"), 2, 0.1),
                Facing.valueOf(billboard.has("facing_camera_mode") ? billboard.get("facing_camera_mode").getAsString().toUpperCase(Locale.ROOT) : "LOOKAT_XYZ"),
                directionJson.has("min_speed_threshold") ? directionJson.get("min_speed_threshold").getAsDouble() : 0.01,
                uv(billboard.getAsJsonObject("uv")),
                tint(obj(c, "minecraft:particle_appearance_tinting")));
    }

    private static Uv uv(JsonObject uv) {
        if (uv == null) throw new IllegalArgumentException("billboard needs uv");
        float w = uv.get("texture_width").getAsFloat(), h = uv.get("texture_height").getAsFloat();
        JsonObject flip = uv.has("flipbook") ? uv.getAsJsonObject("flipbook") : null;
        if (flip == null) return new Uv(w, h, vec(uv.get("uv"), 2, 0), vec(uv.get("uv_size"), 2, 0), null, 0, ONE, false, false);
        return new Uv(w, h, vec(flip.get("base_UV"), 2, 0), vec(flip.get("size_UV"), 2, 0), vec(flip.get("step_UV"), 2, 0),
                flip.has("frames_per_second") ? flip.get("frames_per_second").getAsDouble() : 8, expr(flip, "max_frame", 1),
                flip.has("stretch_to_lifetime") && flip.get("stretch_to_lifetime").getAsBoolean(),
                flip.has("loop") && flip.get("loop").getAsBoolean());
    }

    private static Tint tint(JsonObject tinting) {
        if (tinting == null) return null;
        JsonElement color = tinting.get("color");
        if (color.isJsonArray()) {
            Expr[] rgba = vec(color, 4, 1);
            return new Tint(null, null, rgba);
        }
        if (color.isJsonPrimitive()) return new Tint(null, null, constants(hex(color.getAsString())));
        JsonObject gradient = color.getAsJsonObject();
        TreeMap<Double, float[]> keys = new TreeMap<>();
        JsonElement g = gradient.get("gradient");
        if (g.isJsonArray()) {
            JsonArray list = g.getAsJsonArray();
            for (int i = 0; i < list.size(); i++) keys.put(list.size() == 1 ? 0 : (double) i / (list.size() - 1), color(list.get(i)));
        } else {
            for (Map.Entry<String, JsonElement> e : g.getAsJsonObject().entrySet()) keys.put(Double.parseDouble(e.getKey()), color(e.getValue()));
        }
        return new Tint(keys, expr(gradient, "interpolant", 0), null);
    }

    private static float[] color(JsonElement e) {
        if (e.isJsonPrimitive()) return hex(e.getAsString());
        JsonArray a = e.getAsJsonArray();
        return new float[] {a.get(0).getAsFloat(), a.get(1).getAsFloat(), a.get(2).getAsFloat(), a.size() > 3 ? a.get(3).getAsFloat() : 1};
    }

    /** #RRGGBB or #AARRGGBB, as Bedrock writes them. */
    private static float[] hex(String s) {
        String h = s.startsWith("#") ? s.substring(1) : s;
        long v = Long.parseLong(h, 16);
        float a = h.length() == 8 ? (v >> 24 & 255) / 255f : 1;
        return new float[] {(v >> 16 & 255) / 255f, (v >> 8 & 255) / 255f, (v & 255) / 255f, a};
    }

    private static Expr[] constants(float[] values) {
        Expr[] out = new Expr[values.length];
        for (int i = 0; i < values.length; i++) out[i] = Molang.constant(values[i]);
        return out;
    }

    private static Expr[] planeNormal(JsonElement e) {
        if (e.isJsonArray()) return vec(e, 3, 0);
        return switch (e.getAsString()) {
            case "x" -> constants(new float[] {1, 0, 0});
            case "z" -> constants(new float[] {0, 0, 1});
            default -> constants(new float[] {0, 1, 0});
        };
    }

    private static JsonObject obj(JsonObject parent, String key) {
        return parent.has(key) ? parent.getAsJsonObject(key) : null;
    }

    private static Expr expr(JsonObject parent, String key, double fallback) {
        return parent.has(key) ? expr(parent.get(key)) : Molang.constant(fallback);
    }

    private static Expr expr(JsonElement e) {
        if (e.isJsonPrimitive() && e.getAsJsonPrimitive().isNumber()) return Molang.constant(e.getAsDouble());
        if (e.isJsonPrimitive() && e.getAsJsonPrimitive().isBoolean()) return Molang.constant(e.getAsBoolean() ? 1 : 0);
        return Molang.parse(e.getAsString());
    }

    private static Expr[] vec(JsonElement e, int n, double fallback) {
        Expr[] out = new Expr[n];
        for (int i = 0; i < n; i++) out[i] = e != null && e.isJsonArray() && i < e.getAsJsonArray().size()
                ? expr(e.getAsJsonArray().get(i)) : Molang.constant(fallback);
        return out;
    }
}
