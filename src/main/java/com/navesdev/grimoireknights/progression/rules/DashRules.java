package com.navesdev.grimoireknights.progression.rules;

/** Dash direction and speed: toward the movement keys, or the look when standing still. */
public final class DashRules {
    private DashRules() {}

    /** Horizontal unit vector {x, z}; {0, 0} when neither movement nor look has a direction. */
    public static double[] direction(double moveX, double moveZ, double lookX, double lookZ) {
        double length = Math.hypot(moveX, moveZ);
        if (length < 1e-6) {
            moveX = lookX;
            moveZ = lookZ;
            length = Math.hypot(moveX, moveZ);
        }
        if (length < 1e-6) return new double[] {0, 0};
        return new double[] {moveX / length, moveZ / length};
    }

    public static double speedPerTick(double distance, int ticks) {
        if (ticks < 1 || distance < 0) throw new IllegalArgumentException("invalid dash");
        return distance / ticks;
    }
}
