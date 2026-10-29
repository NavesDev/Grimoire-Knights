package com.navesdev.grimoireknights.ability.attachment;

/** Fire burn on any living entity: stacks and ticks left. Not saved; it wears off. */
public record BurnData(int stacks, int ticksLeft) {}
