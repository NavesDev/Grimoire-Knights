package com.navesdev.grimoireknights.core.client.vfx;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.navesdev.grimoireknights.core.vfx.molang.Molang;
import com.navesdev.grimoireknights.core.vfx.molang.Molang.Expr;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/** One running {@link VfxEffect}: spawns its particles, moves them in seconds (Bedrock units) and draws them. */
final class VfxEmitter implements Molang.Scope {
    private final VfxEffect effect;
    private final Vec3 origin;
    private final Map<String, Float> variables;
    private final Random random = new Random();
    private final double[] randoms = {random.nextDouble(), random.nextDouble(), random.nextDouble(), random.nextDouble()};
    private final List<Particle> particles = new ArrayList<>();
    private final double activeTime;
    private double age;
    private double steady;
    private boolean started;

    VfxEmitter(VfxEffect effect, Vec3 origin, Map<String, Float> variables) {
        this.effect = effect;
        this.origin = origin;
        this.variables = variables;
        this.activeTime = effect.activeTime().eval(this);
    }

    @Override
    public double get(String name) {
        return switch (name) {
            case "variable.emitter_age" -> age;
            case "variable.emitter_lifetime" -> activeTime;
            case "variable.emitter_random_1" -> randoms[0];
            case "variable.emitter_random_2" -> randoms[1];
            case "variable.emitter_random_3" -> randoms[2];
            case "variable.emitter_random_4" -> randoms[3];
            default -> {
                Float value = variables.get(name.substring(name.indexOf('.') + 1));
                yield value == null ? 0 : value;
            }
        };
    }

    @Override
    public double random() {
        return random.nextDouble();
    }

    boolean done() {
        return started && age >= activeTime && particles.isEmpty();
    }

    void update(double dt) {
        if (!started) {
            started = true;
            for (int i = (int) effect.burst().eval(this); i > 0; i--) spawn();
        }
        age += dt;
        if (age <= activeTime) {
            steady += effect.rate().eval(this) * dt;
            int max = (int) effect.maxParticles().eval(this);
            for (; steady >= 1; steady--) if (particles.size() < max) spawn();
        }
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            p.age += dt;
            if (p.age >= p.lifetime) {
                it.remove();
                continue;
            }
            double drag = effect.drag().eval(p);
            Expr[] a = effect.acceleration();
            p.vx += (a[0].eval(p) - drag * p.vx) * dt;
            p.vy += (a[1].eval(p) - drag * p.vy) * dt;
            p.vz += (a[2].eval(p) - drag * p.vz) * dt;
            p.x += p.vx * dt;
            p.y += p.vy * dt;
            p.z += p.vz * dt;
            p.rotation += p.rotationRate * dt;
        }
    }

    private void spawn() {
        Particle p = new Particle();
        p.lifetime = effect.lifetime().eval(p);
        Vector3f at = shapePoint(p);
        Expr[] offset = effect.offset();
        Vector3f dir = switch (effect.direction()) {
            case OUTWARDS -> at.lengthSquared() > 1e-8 ? new Vector3f(at).normalize() : randomUnit();
            case INWARDS -> at.lengthSquared() > 1e-8 ? new Vector3f(at).normalize().negate() : randomUnit();
            case VECTOR -> {
                Expr[] v = effect.directionVector();
                Vector3f d = new Vector3f((float) v[0].eval(p), (float) v[1].eval(p), (float) v[2].eval(p));
                yield d.lengthSquared() > 1e-8 ? d.normalize() : d;
            }
        };
        float speed = (float) effect.speed().eval(p);
        p.x = origin.x + at.x + offset[0].eval(p);
        p.y = origin.y + at.y + offset[1].eval(p);
        p.z = origin.z + at.z + offset[2].eval(p);
        p.vx = dir.x * speed;
        p.vy = dir.y * speed;
        p.vz = dir.z * speed;
        p.rotation = effect.spin().eval(p);
        p.rotationRate = effect.spinRate().eval(p);
        particles.add(p);
    }

    private Vector3f shapePoint(Particle p) {
        double r = effect.radius().eval(p);
        return switch (effect.shape()) {
            case POINT -> new Vector3f();
            case SPHERE -> randomUnit().mul((float) (effect.surfaceOnly() ? r : r * Math.cbrt(random.nextDouble())));
            case DISC -> {
                Expr[] n = effect.planeNormal();
                Vector3f normal = new Vector3f((float) n[0].eval(p), (float) n[1].eval(p), (float) n[2].eval(p));
                if (normal.lengthSquared() < 1e-8) normal.set(0, 1, 0);
                normal.normalize();
                Vector3f a = Math.abs(normal.y) < 0.99f ? new Vector3f(0, 1, 0).cross(normal).normalize() : new Vector3f(1, 0, 0);
                Vector3f b = new Vector3f(normal).cross(a);
                double angle = random.nextDouble() * Math.PI * 2;
                float d = (float) (effect.surfaceOnly() ? r : r * Math.sqrt(random.nextDouble()));
                yield a.mul((float) Math.cos(angle) * d).add(b.mul((float) Math.sin(angle) * d));
            }
            case BOX -> {
                Expr[] h = effect.halfDimensions();
                float[] half = {(float) h[0].eval(p), (float) h[1].eval(p), (float) h[2].eval(p)};
                float[] v = new float[3];
                for (int i = 0; i < 3; i++) v[i] = (float) ((random.nextDouble() * 2 - 1) * half[i]);
                if (effect.surfaceOnly()) {
                    int face = random.nextInt(3);
                    v[face] = random.nextBoolean() ? half[face] : -half[face];
                }
                yield new Vector3f(v[0], v[1], v[2]);
            }
        };
    }

    private Vector3f randomUnit() {
        double z = random.nextDouble() * 2 - 1, angle = random.nextDouble() * Math.PI * 2, s = Math.sqrt(1 - z * z);
        return new Vector3f((float) (s * Math.cos(angle)), (float) z, (float) (s * Math.sin(angle)));
    }

    void render(PoseStack.Pose pose, VertexConsumer buffer, Camera camera, ClientLevel level) {
        Vec3 cam = camera.getPosition();
        VfxEffect.Uv uv = effect.uv();
        for (Particle p : particles) {
            float sx = (float) effect.size()[0].eval(p), sy = (float) effect.size()[1].eval(p);
            if (sx <= 0 && sy <= 0) continue;
            Vector3f[] axes = axes(p, camera, cam);
            Vector3f right = axes[0], up = axes[1];
            if (p.rotation != 0) {
                float c = (float) Math.cos(Math.toRadians(p.rotation)), s = (float) Math.sin(Math.toRadians(p.rotation));
                Vector3f r = new Vector3f(right).mul(c).add(new Vector3f(up).mul(s));
                up = new Vector3f(up).mul(c).sub(new Vector3f(right).mul(s));
                right = r;
            }
            right.mul(sx);
            up.mul(sy);

            double frame = 0;
            if (uv.step() != null) {
                int frames = Math.max(1, (int) uv.maxFrame().eval(p));
                frame = uv.stretchToLifetime() ? Math.floor(p.age / p.lifetime * frames) : Math.floor(p.age * uv.fps());
                frame = uv.loop() ? frame % frames : Math.min(frame, frames - 1);
            }
            float u0 = (float) ((uv.base()[0].eval(p) + (uv.step() == null ? 0 : uv.step()[0].eval(p) * frame)) / uv.textureWidth());
            float v0 = (float) ((uv.base()[1].eval(p) + (uv.step() == null ? 0 : uv.step()[1].eval(p) * frame)) / uv.textureHeight());
            float u1 = u0 + (float) (uv.size()[0].eval(p) / uv.textureWidth());
            float v1 = v0 + (float) (uv.size()[1].eval(p) / uv.textureHeight());

            float[] color = color(p);
            int light = effect.lighting() ? LevelRenderer.getLightColor(level, BlockPos.containing(p.x, p.y, p.z)) : LightTexture.FULL_BRIGHT;
            float x = (float) (p.x - cam.x), y = (float) (p.y - cam.y), z = (float) (p.z - cam.z);
            float[][] corners = {{-1, -1, u0, v1}, {1, -1, u1, v1}, {1, 1, u1, v0}, {-1, 1, u0, v0}};
            // Additive quads go through a culled render type, so they get a back face too.
            int sides = effect.material() == VfxEffect.Material.ADD ? 2 : 1;
            for (int side = 0; side < sides; side++) {
                for (int i = 0; i < 4; i++) {
                    float[] k = corners[side == 0 ? i : 3 - i];
                    buffer.addVertex(pose, x + right.x * k[0] + up.x * k[1], y + right.y * k[0] + up.y * k[1], z + right.z * k[0] + up.z * k[1])
                            .setColor(color[0], color[1], color[2], color[3])
                            .setUv(k[2], k[3])
                            .setOverlay(OverlayTexture.NO_OVERLAY)
                            .setLight(light)
                            .setNormal(pose, 0, 1, 0);
                }
            }
        }
    }

    /** Right and up of the quad (unit length) for the effect's facing mode; emitter_transform_* as Blockbench draws them. */
    private Vector3f[] axes(Particle p, Camera camera, Vec3 cam) {
        Vector3f toCamera = new Vector3f((float) (cam.x - p.x), (float) (cam.y - p.y), (float) (cam.z - p.z));
        return switch (effect.facing()) {
            case LOOKAT_XYZ -> new Vector3f[] {new Vector3f(camera.getLeftVector()).negate(), new Vector3f(camera.getUpVector())};
            case LOOKAT_Y -> {
                Vector3f right = new Vector3f(0, 1, 0).cross(toCamera);
                yield new Vector3f[] {right.lengthSquared() > 1e-8 ? right.normalize() : new Vector3f(1, 0, 0), new Vector3f(0, 1, 0)};
            }
            case LOOKAT_DIRECTION -> {
                Vector3f velocity = new Vector3f((float) p.vx, (float) p.vy, (float) p.vz);
                if (velocity.length() < effect.minSpeedThreshold()) {
                    yield new Vector3f[] {new Vector3f(camera.getLeftVector()).negate(), new Vector3f(camera.getUpVector())};
                }
                Vector3f right = velocity.normalize();
                Vector3f up = new Vector3f(toCamera).cross(right);
                yield new Vector3f[] {right, up.lengthSquared() > 1e-8 ? up.normalize() : new Vector3f(camera.getUpVector())};
            }
            case EMITTER_TRANSFORM_XY -> new Vector3f[] {new Vector3f(1, 0, 0), new Vector3f(0, 1, 0)};
            case EMITTER_TRANSFORM_XZ -> new Vector3f[] {new Vector3f(1, 0, 0), new Vector3f(0, 0, -1)};
            case EMITTER_TRANSFORM_YZ -> new Vector3f[] {new Vector3f(0, 0, -1), new Vector3f(0, 1, 0)};
        };
    }

    private float[] color(Particle p) {
        VfxEffect.Tint tint = effect.tint();
        if (tint == null) return new float[] {1, 1, 1, 1};
        if (tint.rgba() != null) {
            Expr[] e = tint.rgba();
            return new float[] {clamp(e[0].eval(p)), clamp(e[1].eval(p)), clamp(e[2].eval(p)), clamp(e[3].eval(p))};
        }
        double t = tint.interpolant().eval(p);
        Entry<Double, float[]> lo = tint.gradient().floorEntry(t), hi = tint.gradient().ceilingEntry(t);
        if (lo == null) return hi.getValue();
        if (hi == null || hi.getKey().equals(lo.getKey())) return lo.getValue();
        float k = (float) ((t - lo.getKey()) / (hi.getKey() - lo.getKey()));
        float[] a = lo.getValue(), b = hi.getValue(), out = new float[4];
        for (int i = 0; i < 4; i++) out[i] = a[i] + (b[i] - a[i]) * k;
        return out;
    }

    private static float clamp(double v) {
        return (float) Math.max(0, Math.min(1, v));
    }

    VfxEffect effect() {
        return effect;
    }

    private final class Particle implements Molang.Scope {
        final double[] randoms = {random.nextDouble(), random.nextDouble(), random.nextDouble(), random.nextDouble()};
        double x, y, z, vx, vy, vz, age, lifetime = 1, rotation, rotationRate;

        @Override
        public double get(String name) {
            return switch (name) {
                case "variable.particle_age" -> age;
                case "variable.particle_lifetime" -> lifetime;
                case "variable.particle_random_1" -> randoms[0];
                case "variable.particle_random_2" -> randoms[1];
                case "variable.particle_random_3" -> randoms[2];
                case "variable.particle_random_4" -> randoms[3];
                default -> VfxEmitter.this.get(name);
            };
        }

        @Override
        public double random() {
            return random.nextDouble();
        }
    }
}
