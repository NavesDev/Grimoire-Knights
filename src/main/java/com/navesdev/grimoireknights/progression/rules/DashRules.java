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

    /** Pose for a dash in {@code dir} while looking along {@code look} (both horizontal): forward, back or to a side. */
    public static String pose(double dirX, double dirZ, double lookX, double lookZ) {
        double forward = dirX * lookX + dirZ * lookZ;
        double right = dirX * -lookZ + dirZ * lookX;
        if (Math.abs(right) > Math.abs(forward)) return right > 0 ? "dash_right" : "dash_left";
        return forward >= 0 ? "dash" : "dash_back";
    }

    /** A roll needs a sneak press inside the window before landing ({@code pressedAt} -1: none since the last roll). */
    public static boolean rollTimed(long pressedAt, long landedAt, int windowTicks) {
        return pressedAt >= 0 && landedAt - pressedAt <= windowTicks;
    }

    public static double speedPerTick(double distance, int ticks) {
        if (ticks < 1 || distance < 0) throw new IllegalArgumentException("invalid dash");
        return distance / ticks;
    }
}
