package com.navesdev.grimoireknights.ability;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;

/** Spell steps that run a few ticks later (second flame hand, blade volleys, ground fire), on the server thread. */
public final class Delayed {
    private record Task(ServerLevel level, long at, Runnable run) {}

    private static final List<Task> TASKS = new ArrayList<>();

    private Delayed() {}

    public static void run(ServerLevel level, int ticks, Runnable run) {
        TASKS.add(new Task(level, level.getGameTime() + ticks, run));
    }

    /** Drops every pending step, so nothing pins a stopped server's levels. */
    public static void clear() {
        TASKS.clear();
    }

    /** Called at the end of every level tick. */
    public static void tick(ServerLevel level) {
        List<Task> due = new ArrayList<>();
        TASKS.removeIf(task -> task.level() == level && task.at() <= level.getGameTime() && due.add(task));
        due.forEach(task -> task.run().run());
    }
}
